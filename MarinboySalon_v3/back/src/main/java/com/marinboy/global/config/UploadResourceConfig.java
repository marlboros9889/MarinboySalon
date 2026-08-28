package com.marinboy.global.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 저장된 메뉴 이미지를 /uploads 주소로만 읽을 수 있게 연결합니다. */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final String serviceImageDirectory;

    public UploadResourceConfig(@Value("${app.upload.service-image-directory}") String serviceImageDirectory) {
        this.serviceImageDirectory = serviceImageDirectory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = Path.of(serviceImageDirectory)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation = resourceLocation + "/";
        }
        registry.addResourceHandler("/uploads/service-items/**")
                .addResourceLocations(resourceLocation);
    }
}
