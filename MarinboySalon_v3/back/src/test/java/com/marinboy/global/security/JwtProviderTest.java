package com.marinboy.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * JWT에 회원 번호와 권한이 정확히 저장되는지 확인합니다.
 */
class JwtProviderTest {

    @Test
    void accessTokenCreateAndParse() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("marinboy-salon-test-secret-key-longer-than-thirty-two-bytes");
        properties.setAccessTokenExpSeconds(1800);
        properties.setRefreshTokenExpSeconds(604800);

        JwtProvider provider = new JwtProvider(properties);
        String token = provider.createAccessToken("7", Map.of("role", "ADMIN"));

        assertThat(provider.parse(token).getSubject()).isEqualTo("7");
        assertThat(provider.parse(token).get("role")).isEqualTo("ADMIN");
        assertThat(provider.parse(token).getId()).isNotBlank();
    }
}
