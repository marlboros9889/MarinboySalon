package com.marinboy.service;

import com.marinboy.config.UploadDirectoryProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/** 시술 이미지의 검증·물리 저장·트랜잭션 후 정리를 독립적으로 처리합니다. */
@Component
public class ServiceImageTool {
    private static final Logger log = LoggerFactory.getLogger(ServiceImageTool.class);
    private static final String PUBLIC_URL_PREFIX = "/uploads/services/";
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private final Path serviceImageDirectory;

    public ServiceImageTool(UploadDirectoryProvider uploadDirectoryProvider) {
        this.serviceImageDirectory = uploadDirectoryProvider.getUploadDirectory().resolve("services").normalize();
    }

    /** 허용된 이미지에 안전한 임의 파일명을 부여하고 공용 업로드 경로에 저장합니다. */
    public String save(MultipartFile image) {
        String contentType = image == null ? null : image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG, PNG, GIF, WEBP 이미지 파일만 등록할 수 있습니다.");
        }

        String extension = extensionFor(contentType);
        try {
            Files.createDirectories(serviceImageDirectory);
            String fileName = UUID.randomUUID() + extension;
            image.transferTo(serviceImageDirectory.resolve(fileName));
            return PUBLIC_URL_PREFIX + fileName;
        } catch (Exception exception) {
            throw new IllegalArgumentException("이미지 파일 저장에 실패했습니다.", exception);
        }
    }

    /** DB 커밋이 끝난 뒤 교체된 기존 파일만 삭제합니다. */
    public void deleteAfterCommit(List<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteNow(imageUrls);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteNow(imageUrls);
            }
        });
    }

    /** DB 롤백 시 이번 요청에서 새로 저장한 파일만 되돌립니다. */
    public void deleteOnRollback(List<String> imageUrls) {
        if (imageUrls.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    deleteNow(imageUrls);
                }
            }
        });
    }

    /** 저장 도중 예외가 발생하면 아직 DB와 연결되지 않은 파일을 즉시 정리합니다. */
    public void deleteNow(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            Path imagePath = resolveStoredImage(imageUrl);
            if (imagePath == null) {
                continue;
            }
            try {
                Files.deleteIfExists(imagePath);
            } catch (Exception exception) {
                log.warn("시술 이미지 정리에 실패했습니다: {}", imagePath.getFileName());
            }
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private Path resolveStoredImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(PUBLIC_URL_PREFIX)) {
            return null;
        }
        String fileName = imageUrl.substring(PUBLIC_URL_PREFIX.length());
        Path imagePath = serviceImageDirectory.resolve(fileName).normalize();
        return imagePath.startsWith(serviceImageDirectory) ? imagePath : null;
    }
}
