package com.marinboy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 설정된 v2 업로드 폴더를 /uploads URL로 제공하는 설정입니다. */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final UploadDirectoryProvider uploadDirectoryProvider;

    public UploadResourceConfig(UploadDirectoryProvider uploadDirectoryProvider) {
        this.uploadDirectoryProvider = uploadDirectoryProvider;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 저장과 조회가 반드시 같은 폴더를 보도록 공통 제공자를 사용합니다.
        String location = uploadDirectoryProvider.getUploadDirectory().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
