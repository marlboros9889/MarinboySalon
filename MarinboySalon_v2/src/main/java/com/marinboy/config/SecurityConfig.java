package com.marinboy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * v2의 로그인 권한 검사는 기존 Interceptor가 담당하고,
 * Spring Security는 모든 상태 변경 폼의 CSRF 토큰을 검증합니다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                // 기본 HttpSessionCsrfTokenRepository로 서버 세션과 폼 토큰을 비교합니다.
                .csrf(Customizer.withDefaults());

        return http.build();
    }
}
