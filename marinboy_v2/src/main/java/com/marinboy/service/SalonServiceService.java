package com.marinboy.service;

import com.marinboy.dao.SalonServiceDao;
import com.marinboy.dto.ServiceDto;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// 고객 화면에 표시할 시술 메뉴와 이미지 데이터를 조립하는 서비스입니다.
@Service
public class SalonServiceService {

    private final SalonServiceDao salonServiceDao;

    public SalonServiceService(SalonServiceDao salonServiceDao) {
        // 시술 메뉴 조회 SQL은 DAO와 mapper XML에 위임합니다.
        this.salonServiceDao = salonServiceDao;
    }

    public List<ServiceDto> getServices() {
        // 서비스 기본 정보와 이미지 행을 따로 조회한 뒤 화면 DTO로 합칩니다.
        List<ServiceDto> services = salonServiceDao.findAllServices();
        Map<Long, List<ServiceDto>> imagesByServiceId = salonServiceDao.findAllServiceImages()
                .stream()
                .collect(Collectors.groupingBy(ServiceDto::getServiceId));

        for (ServiceDto service : services) {
            // SQLPlus 인코딩 문제로 깨진 초기 샘플 데이터는 화면 표시 전에 한글로 보정합니다.
            normalizeSeedServiceText(service);
            List<ServiceDto> images = imagesByServiceId.getOrDefault(service.getId(), List.of());
            service.setImageUrl(findRepresentativeImage(images));
            service.setAdditionalImageUrls(findDetailImages(images));
        }

        return services;
    }

    public Integer getDurationMinutes(Long serviceId) {
        // 예약 가능 시간 계산에서 시술별 소요 시간을 확인합니다.
        return salonServiceDao.findDurationMinutesById(serviceId);
    }

    @Transactional
    public Long saveService(Long id, String name, String category, int durationMinutes, int price, String description) {
        if (name == null || name.isBlank() || category == null || category.isBlank() || durationMinutes < 10 || price <= 0) throw new IllegalArgumentException("시술명, 카테고리, 시간, 가격을 확인하세요.");
        if (id == null) {
            ServiceDto service = new ServiceDto();
            service.setName(name);
            service.setCategory(category);
            service.setDurationMinutes(durationMinutes);
            service.setPrice(java.math.BigDecimal.valueOf(price));
            service.setDescription(description == null ? "" : description);
            salonServiceDao.insertService(service);
            id = service.getId();
        }
        else if (salonServiceDao.updateService(id, name, category, durationMinutes, price, description == null ? "" : description) == 0) throw new IllegalArgumentException("시술 메뉴가 없습니다.");
        return id;
    }

    /** 메뉴 저장과 대표 이미지 교체를 하나의 트랜잭션으로 처리합니다. */
    @Transactional
    public void saveService(Long id, String name, String category, int durationMinutes, int price, String description, MultipartFile image, MultipartFile[] galleryImages) {
        Long serviceId = saveService(id, name, category, durationMinutes, price, description);
        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImageFile(image);
            salonServiceDao.deleteRepresentativeImage(serviceId);
            salonServiceDao.insertRepresentativeImage(serviceId, imageUrl);
        }
        // 상세 이미지가 전달된 경우에만 기존 묶음을 새 묶음으로 교체합니다.
        if (galleryImages != null && galleryImages.length > 0) {
            List<MultipartFile> files = List.of(galleryImages).stream().filter(file -> !file.isEmpty()).toList();
            if (files.size() > 5) throw new IllegalArgumentException("메뉴별 상세 이미지는 최대 5장까지 등록할 수 있습니다.");
            if (!files.isEmpty()) {
                salonServiceDao.deleteDetailImages(serviceId);
                for (int index = 0; index < files.size(); index++) {
                    salonServiceDao.insertDetailImage(serviceId, saveImageFile(files.get(index)), index + 1);
                }
            }
        }
    }

    private String saveImageFile(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !Set.of("image/jpeg", "image/png", "image/gif", "image/webp").contains(contentType)) {
            throw new IllegalArgumentException("JPG, PNG, GIF, WEBP 이미지 파일만 등록할 수 있습니다.");
        }
        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        try {
            Path directory = Paths.get("uploads", "services").toAbsolutePath().normalize();
            Files.createDirectories(directory);
            String fileName = UUID.randomUUID() + extension;
            image.transferTo(directory.resolve(fileName));
            return "/uploads/services/" + fileName;
        } catch (Exception exception) {
            throw new IllegalArgumentException("이미지 파일 저장에 실패했습니다.", exception);
        }
    }
    public void deleteService(Long id) { if (salonServiceDao.deleteService(id) == 0) throw new IllegalArgumentException("시술 메뉴가 없습니다."); }

    private String findRepresentativeImage(List<ServiceDto> images) {
        // 대표 이미지가 없으면 화면이 깨지지 않도록 빈 문자열을 반환합니다.
        return images.stream()
                .filter(image -> "REPRESENTATIVE".equals(image.getImageType()))
                .map(ServiceDto::getImageUrl)
                .findFirst()
                .orElse("");
    }

    private List<String> findDetailImages(List<ServiceDto> images) {
        // 상세 이미지는 고객이 추가 예시를 볼 수 있도록 배열로 내려줍니다.
        return images.stream()
                .filter(image -> "DETAIL".equals(image.getImageType()))
                .map(ServiceDto::getImageUrl)
                .toList();
    }

    private void normalizeSeedServiceText(ServiceDto service) {
        // 로컬 Oracle에 이미 들어간 초기 샘플 데이터가 깨졌을 때도 고객 화면은 정상 한글로 보여줍니다.
        if (service.getId() == null) {
            return;
        }

        switch (service.getId().intValue()) {
            case 1 -> {
                service.setName("웨이브 펌");
                service.setCategory("펌");
                service.setDescription("1인 미용실에서 진행하는 부드러운 웨이브 펌 시술입니다.");
            }
            case 2 -> {
                service.setName("시그니처 컷");
                service.setCategory("컷");
                service.setDescription("상담을 포함한 맞춤형 커트 시술입니다.");
            }
            case 3 -> {
                service.setName("두피 클리닉");
                service.setCategory("클리닉");
                service.setDescription("두피 상태를 점검하고 진정 케어를 진행하는 클리닉입니다.");
            }
            case 4 -> {
                service.setName("젤 네일 기본");
                service.setCategory("네일");
                service.setDescription("손톱 케어와 기본 젤 컬러를 진행하는 네일 시술입니다.");
            }
            case 5 -> {
                service.setName("꾸미기 화장");
                service.setCategory("메이크업");
                service.setDescription("소개팅, 촬영, 중요한 약속 전 자연스럽게 완성하는 메이크업입니다.");
            }
            case 6 -> {
                service.setName("신부 화장");
                service.setCategory("웨딩");
                service.setDescription("본식 또는 웨딩 촬영을 위한 신부 헤어와 메이크업 패키지입니다.");
            }
            default -> {
                // 관리자가 새로 등록한 메뉴는 DB 값을 그대로 사용합니다.
            }
        }
    }
}
