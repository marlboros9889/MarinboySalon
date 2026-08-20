package com.marinboy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/** Swagger UI에 프로젝트와 API 문서 정보를 표시합니다. */
@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI marinboyOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("마린보이 예약 서비스 API")
                .version("v3")
                .description("예약, 시술 메뉴, 관리자, 인증 기능을 확인하는 API 문서입니다."));
    }
}
