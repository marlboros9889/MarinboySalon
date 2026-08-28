package com.marinboy.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger UI에서 JWT Bearer 인증을 바로 시험할 수 있게 합니다.
 * 문서 주소: /swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI marinboyOpenApi() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("MarinboySalon API")
                        .description("1인 헤어샵 v3 REST API (예약, 메뉴, 인증, 관리자)")
                        .version("v3"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
