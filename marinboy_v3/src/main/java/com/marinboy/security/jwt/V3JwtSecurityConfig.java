package com.marinboy.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/** /api/v3/**에만 적용하는 무상태 JWT 보안 정책입니다. */
@Profile("v3")
@Configuration
public class V3JwtSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public V3JwtSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @Order(1)
    SecurityFilterChain v3SecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/v3/**")
                .csrf(AbstractHttpConfigurer::disable)
                // React의 Authorization 사전 요청을 인증 전에 처리합니다.
                .cors(cors -> cors.configurationSource(v3CorsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v3/**").permitAll()
                        .requestMatchers("/api/v3/auth/login").permitAll()
                        .requestMatchers("/api/v3/auth/social/session").permitAll()
                        // 시술 메뉴 조회는 로그인 고객에게, 등록·수정은 관리자에게만 허용합니다.
                        .requestMatchers(HttpMethod.POST, "/api/v3/service-items/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v3/service-items/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .headers(headers -> headers
                        .contentTypeOptions(contentType -> {})
                        .frameOptions(frame -> frame.sameOrigin())
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.SAME_ORIGIN)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource v3CorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // localhost와 127.0.0.1 모두 Vite 개발 서버 주소로 지원합니다.
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        // OAuth2 완료 후 기존 서버 세션을 JWT로 교환할 수 있도록 쿠키 전송을 허용합니다.
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v3/**", configuration);
        return source;
    }
}
