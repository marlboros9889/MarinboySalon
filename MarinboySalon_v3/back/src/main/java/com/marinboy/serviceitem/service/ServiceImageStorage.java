package com.marinboy.serviceitem.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 관리자 선택 이미지를 서버 한 폴더에 안전한 이름으로 저장합니다. */
@Service
public class ServiceImageStorage {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path uploadDirectory;

    public ServiceImageStorage(@Value("${app.upload.service-image-directory}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    /** 파일 형식과 크기를 확인한 뒤 공개 URL과 연결되는 경로에 저장합니다. */
    public List<String> store(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return List.of();
        }
        if (imageFiles.size() > 4) {
            throw new IllegalArgumentException("메뉴 이미지는 최대 4개까지 선택할 수 있습니다.");
        }

        List<String> imageUrls = new ArrayList<>();
        try {
            Files.createDirectories(uploadDirectory);
            for (MultipartFile imageFile : imageFiles) {
                validateImageFile(imageFile);
                String extension = getExtension(imageFile.getOriginalFilename());
                String storedFileName = UUID.randomUUID() + extension;
                Path targetPath = uploadDirectory.resolve(storedFileName).normalize();
                if (!targetPath.startsWith(uploadDirectory)) {
                    throw new IllegalArgumentException("이미지 저장 경로가 올바르지 않습니다.");
                }
                Files.copy(imageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                imageUrls.add("/uploads/service-items/" + storedFileName);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("이미지 파일을 저장하지 못했습니다.");
        }
        return imageUrls;
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("빈 이미지 파일은 등록할 수 없습니다.");
        }
        if (imageFile.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("이미지 한 장은 5MB 이하만 등록할 수 있습니다.");
        }
        if (!ALLOWED_TYPES.contains(imageFile.getContentType())) {
            throw new IllegalArgumentException("JPG, PNG, WEBP 이미지 파일만 등록할 수 있습니다.");
        }
    }

    private String getExtension(String originalFileName) {
        if (originalFileName == null) {
            throw new IllegalArgumentException("이미지 파일 이름을 확인할 수 없습니다.");
        }
        String lowerCaseName = originalFileName.toLowerCase();
        if (lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg")) {
            return ".jpg";
        }
        if (lowerCaseName.endsWith(".png")) {
            return ".png";
        }
        if (lowerCaseName.endsWith(".webp")) {
            return ".webp";
        }
        throw new IllegalArgumentException("JPG, PNG, WEBP 확장자만 등록할 수 있습니다.");
    }
}
