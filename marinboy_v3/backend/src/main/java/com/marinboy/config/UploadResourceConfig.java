package com.marinboy.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** V3 루트에 고정한 메뉴 이미지를 /uploads URL로 제공하는 설정입니다. */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {
    private final Path uploadDirectory;

    public UploadResourceConfig(@Value("${app.upload.directory}") String uploadDirectory) {
        // backend 폴더가 이동해도 환경 변수 또는 V3 루트 상대 경로를 기준으로 해석합니다.
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 저장 경로와 정적 제공 경로를 하나로 통일해 파일 이동 후 404를 막습니다.
        String location = uploadDirectory.toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
