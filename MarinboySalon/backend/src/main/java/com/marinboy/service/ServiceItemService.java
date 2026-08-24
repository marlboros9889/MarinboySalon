package com.marinboy.service;

import com.marinboy.mapper.ServiceItemMapper;
import com.marinboy.dto.ServiceItemDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// 고객 화면에 표시할 시술 메뉴와 이미지 데이터를 조립하는 서비스입니다.
@Service
public class ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;
    private final ServiceImageTool serviceImageTool;

    public ServiceItemService(ServiceItemMapper serviceItemMapper, ServiceImageTool serviceImageTool) {
        // 메뉴 SQL과 독립 이미지 도구를 주입해 DB 규칙과 물리 파일 처리를 분리합니다.
        this.serviceItemMapper = serviceItemMapper;
        this.serviceImageTool = serviceImageTool;
    }

    public List<ServiceItemDto> getServices() {
        // 서비스 기본 정보와 이미지 행을 따로 조회한 뒤 화면 DTO로 합칩니다.
        List<ServiceItemDto> services = serviceItemMapper.findAllServices();
        Map<Long, List<ServiceItemDto>> imagesByServiceId = serviceItemMapper.findAllServiceImages()
                .stream()
                .collect(Collectors.groupingBy(ServiceItemDto::getServiceId));

        for (ServiceItemDto service : services) {
            List<ServiceItemDto> images = imagesByServiceId.getOrDefault(service.getId(), List.of());
            service.setImageUrl(findRepresentativeImage(images));
            service.setAdditionalImageUrls(findDetailImages(images));
        }

        return services;
    }

    public Integer getDurationMinutes(Long serviceId) {
        // 예약 가능 시간 계산에서 시술별 소요 시간을 확인합니다.
        return serviceItemMapper.findActiveDurationMinutesById(serviceId);
    }

    public String getServiceName(Long serviceId) {
        // 알림에는 현재 메뉴명을 사용하고 예약 상세 정보는 DB 조인으로 다시 조회합니다.
        return serviceItemMapper.findActiveNameById(serviceId);
    }

    private Long saveServiceData(Long id, String name, String category, int durationMinutes, int price, String description) {
        if (name == null || name.isBlank() || category == null || category.isBlank()
                || durationMinutes < 10 || price <= 0) {
            throw new IllegalArgumentException("시술명, 카테고리, 시간, 가격을 확인하세요.");
        }
        if (id == null) {
            ServiceItemDto service = new ServiceItemDto();
            service.setName(name);
            service.setCategory(category);
            service.setDurationMinutes(durationMinutes);
            service.setPrice(java.math.BigDecimal.valueOf(price));
            service.setDescription(description == null ? "" : description);
            serviceItemMapper.insertService(service);
            id = service.getId();
        } else if (serviceItemMapper.updateService(
                id, name, category, durationMinutes, price, description == null ? "" : description) == 0) {
            throw new IllegalArgumentException("시술 메뉴가 없습니다.");
        }
        return id;
    }

    /** 메뉴 저장과 대표 이미지 교체를 하나의 트랜잭션으로 처리합니다. */
    @Transactional
    public void saveService(
            Long id, String name, String category, int durationMinutes, int price, String description,
            MultipartFile image, MultipartFile[] galleryImages) {
        // DB 트랜잭션이 실패하면 새 파일을 지우고, 성공하면 교체된 기존 파일만 정리합니다.
        List<String> savedImageUrls = new ArrayList<>();
        List<String> replacedImageUrls = new ArrayList<>();

        try {
            List<ServiceItemDto> existingImages = id == null
                    ? List.of()
                    : serviceItemMapper.findServiceImagesByServiceId(id);
            Long serviceId = saveServiceData(id, name, category, durationMinutes, price, description);

            if (image != null && !image.isEmpty()) {
                String imageUrl = serviceImageTool.save(image);
                savedImageUrls.add(imageUrl);
                replacedImageUrls.addAll(findImageUrlsByType(existingImages, "REPRESENTATIVE"));
                serviceItemMapper.deleteRepresentativeImage(serviceId);
                serviceItemMapper.insertRepresentativeImage(serviceId, imageUrl);
            }

            // 상세 이미지는 실제 새 파일이 전달된 경우에만 기존 묶음을 바꿉니다.
            if (galleryImages != null && galleryImages.length > 0) {
                List<MultipartFile> files = new ArrayList<>();
                for (MultipartFile galleryImage : galleryImages) {
                    if (galleryImage != null && !galleryImage.isEmpty()) {
                        files.add(galleryImage);
                    }
                }
                if (files.size() > 5) {
                    throw new IllegalArgumentException("메뉴별 상세 이미지는 최대 5장까지 등록할 수 있습니다.");
                }
                if (!files.isEmpty()) {
                    replacedImageUrls.addAll(findImageUrlsByType(existingImages, "DETAIL"));
                    serviceItemMapper.deleteDetailImages(serviceId);
                    for (int index = 0; index < files.size(); index++) {
                        String imageUrl = serviceImageTool.save(files.get(index));
                        savedImageUrls.add(imageUrl);
                        serviceItemMapper.insertDetailImage(serviceId, imageUrl, index + 1);
                    }
                }
            }

            serviceImageTool.deleteAfterCommit(replacedImageUrls);
            serviceImageTool.deleteOnRollback(savedImageUrls);
        } catch (RuntimeException exception) {
            serviceImageTool.deleteNow(savedImageUrls);
            throw exception;
        }
    }

    public void deleteService(Long id) {
        if (serviceItemMapper.deleteService(id) == 0) {
            throw new IllegalArgumentException("시술 메뉴가 없습니다.");
        }
    }

    private List<String> findImageUrlsByType(List<ServiceItemDto> images, String imageType) {
        return images.stream()
                .filter(image -> imageType.equals(image.getImageType()))
                .map(ServiceItemDto::getImageUrl)
                .toList();
    }


    private String findRepresentativeImage(List<ServiceItemDto> images) {
        // 대표 이미지가 없으면 화면이 깨지지 않도록 빈 문자열을 반환합니다.
        return images.stream()
                .filter(image -> "REPRESENTATIVE".equals(image.getImageType()))
                .map(ServiceItemDto::getImageUrl)
                .findFirst()
                .orElse("");
    }

    private List<String> findDetailImages(List<ServiceItemDto> images) {
        // 상세 이미지는 고객이 추가 예시를 볼 수 있도록 배열로 내려줍니다.
        return images.stream()
                .filter(image -> "DETAIL".equals(image.getImageType()))
                .map(ServiceItemDto::getImageUrl)
                .toList();
    }

}
