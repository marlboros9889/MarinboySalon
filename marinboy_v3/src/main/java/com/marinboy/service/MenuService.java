package com.marinboy.service;

import com.marinboy.mapper.MenuMapper;
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
public class MenuService {

    private final MenuMapper salonServiceDao;

    public MenuService(MenuMapper salonServiceDao) {
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

}
