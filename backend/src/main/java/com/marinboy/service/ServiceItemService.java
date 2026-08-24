package com.marinboy.service;

import com.marinboy.config.UploadDirectoryProvider;
import com.marinboy.mapper.ServiceItemMapper;
import com.marinboy.dto.ServiceItemDto;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

// 고객 화면에 표시할 시술 메뉴와 이미지 데이터를 조립하는 서비스입니다.
@Service
public class ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;
    private final Path uploadDirectory;

    public ServiceItemService(ServiceItemMapper serviceItemMapper, UploadDirectoryProvider uploadDirectoryProvider) {
        // 시술 메뉴 조회 SQL과 검증된 공용 업로드 경로를 주입받습니다.
        this.serviceItemMapper = serviceItemMapper;
        this.uploadDirectory = uploadDirectoryProvider.getUploadDirectory();
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

    @Transactional
    public Long saveService(Long id, String name, String category, int durationMinutes, int price, String description) {
        if (name == null || name.isBlank() || category == null || category.isBlank() || durationMinutes < 10 || price <= 0) throw new IllegalArgumentException("시술명, 카테고리, 시간, 가격을 확인하세요.");
        if (id == null) {
            ServiceItemDto service = new ServiceItemDto();
            service.setName(name);
            service.setCategory(category);
            service.setDurationMinutes(durationMinutes);
            service.setPrice(java.math.BigDecimal.valueOf(price));
            service.setDescription(description == null ? "" : description);
            serviceItemMapper.insertService(service);
            id = service.getId();
        }
        else if (serviceItemMapper.updateService(id, name, category, durationMinutes, price, description == null ? "" : description) == 0) throw new IllegalArgumentException("시술 메뉴가 없습니다.");
        return id;
    }

    /** 메뉴 저장과 대표 이미지 교체를 하나의 트랜잭션으로 처리합니다. */
    @Transactional
    public void saveService(Long id, String name, String category, int durationMinutes, int price, String description, MultipartFile image, MultipartFile[] galleryImages) {
        // DB 트랜잭션이 실패하면 새 파일을 지우고, 성공하면 교체된 기존 파일만 정리합니다.
        List<String> savedImageUrls = new ArrayList<>();
        List<String> replacedImageUrls = new ArrayList<>();

        try {
            List<ServiceItemDto> existingImages = id == null
                    ? List.of()
                    : serviceItemMapper.findServiceImagesByServiceId(id);
            Long serviceId = saveService(id, name, category, durationMinutes, price, description);

            if (image != null && !image.isEmpty()) {
                String imageUrl = saveImageFile(image);
                savedImageUrls.add(imageUrl);
                replacedImageUrls.addAll(findImageUrlsByType(existingImages, "REPRESENTATIVE"));
                serviceItemMapper.deleteRepresentativeImage(serviceId);
                serviceItemMapper.insertRepresentativeImage(serviceId, imageUrl);
            }

            // 상세 이미지는 실제 새 파일이 전달된 경우에만 기존 묶음을 바꿉니다.
            if (galleryImages != null && galleryImages.length > 0) {
                List<MultipartFile> files = List.of(galleryImages).stream().filter(file -> !file.isEmpty()).toList();
                if (files.size() > 5) throw new IllegalArgumentException("메뉴별 상세 이미지는 최대 5장까지 등록할 수 있습니다.");
                if (!files.isEmpty()) {
                    replacedImageUrls.addAll(findImageUrlsByType(existingImages, "DETAIL"));
                    serviceItemMapper.deleteDetailImages(serviceId);
                    for (int index = 0; index < files.size(); index++) {
                        String imageUrl = saveImageFile(files.get(index));
                        savedImageUrls.add(imageUrl);
                        serviceItemMapper.insertDetailImage(serviceId, imageUrl, index + 1);
                    }
                }
            }

            deleteFilesAfterCommit(replacedImageUrls);
            deleteNewFilesIfRollback(savedImageUrls);
        } catch (RuntimeException exception) {
            deleteStoredFiles(savedImageUrls);
            throw exception;
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
            // 실행 위치가 아닌 설정된 공용 경로에 저장해 폴더 이동 뒤에도 이미지를 유지합니다.
            Path directory = uploadDirectory.resolve("services");
            Files.createDirectories(directory);
            String fileName = UUID.randomUUID() + extension;
            image.transferTo(directory.resolve(fileName));
            return "/uploads/services/" + fileName;
        } catch (Exception exception) {
            throw new IllegalArgumentException("이미지 파일 저장에 실패했습니다.", exception);
        }
    }
    public void deleteService(Long id) { if (serviceItemMapper.deleteService(id) == 0) throw new IllegalArgumentException("시술 메뉴가 없습니다."); }

    private List<String> findImageUrlsByType(List<ServiceItemDto> images, String imageType) {
        return images.stream()
                .filter(image -> imageType.equals(image.getImageType()))
                .map(ServiceItemDto::getImageUrl)
                .toList();
    }

    private void deleteFilesAfterCommit(List<String> imageUrls) {
        if (imageUrls.isEmpty()) return;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteStoredFiles(imageUrls);
            }
        });
    }

    private void deleteNewFilesIfRollback(List<String> imageUrls) {
        if (imageUrls.isEmpty()) return;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    deleteStoredFiles(imageUrls);
                }
            }
        });
    }

    private void deleteStoredFiles(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            if (imageUrl == null || !imageUrl.startsWith("/uploads/services/")) continue;
            String fileName = imageUrl.substring("/uploads/services/".length());
            Path imagePath = uploadDirectory.resolve("services").resolve(fileName).normalize();
            if (!imagePath.startsWith(uploadDirectory.resolve("services"))) continue;
            try {
                Files.deleteIfExists(imagePath);
            } catch (Exception ignored) {
                // 파일 정리 실패는 이미 완료된 메뉴 저장을 취소하지 않고 다음 관리 작업에서 다시 정리합니다.
            }
        }
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
