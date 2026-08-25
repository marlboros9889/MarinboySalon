package com.marinboy.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * application.yml의 JWT 설정값을 한 곳에서 관리합니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private int accessTokenExpSeconds;
    private int refreshTokenExpSeconds;
    private boolean cookieSecure;
}
