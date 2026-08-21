package com.marinboy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** V3 루트에 고정한 메뉴 이미지를 /uploads URL로 제공하는 설정입니다. */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {
    private final UploadDirectoryProvider uploadDirectoryProvider;

    public UploadResourceConfig(UploadDirectoryProvider uploadDirectoryProvider) {
        // 저장 서비스와 동일한 검증된 절대 경로를 사용합니다.
        this.uploadDirectoryProvider = uploadDirectoryProvider;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 저장 경로와 정적 제공 경로를 하나로 통일해 파일 이동 후 404를 막습니다.
        String location = uploadDirectoryProvider.getUploadDirectory().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
