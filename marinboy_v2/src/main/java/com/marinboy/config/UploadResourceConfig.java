package com.marinboy.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 실행 폴더에 저장된 메뉴 이미지를 /uploads URL로 제공하는 설정입니다. */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 운영체제에 맞는 절대 file URI로 변환해 업로드 파일을 정적 리소스로 제공합니다.
        String location = Paths.get("uploads").toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
