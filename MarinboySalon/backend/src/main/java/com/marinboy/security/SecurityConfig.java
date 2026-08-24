package com.marinboy.security;

import java.util.LinkedHashSet;
import java.util.List;

import com.marinboy.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** 전체 API를 JWT + Redis 기반 무상태 인증으로 보호합니다. */
@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.cors.allowed-origin:http://localhost:3000}") String configuredOrigin) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        // 설정 주소와 로컬의 두 대표 주소를 중복 없이 허용해 localhost/127.0.0.1 혼용 오류를 막습니다.
        LinkedHashSet<String> origins = new LinkedHashSet<>();
        if (configuredOrigin != null && !configuredOrigin.isBlank()) origins.add(configuredOrigin.trim());
        origins.add("http://localhost:3000");
        origins.add("http://127.0.0.1:3000");
        this.allowedOrigins = List.copyOf(origins);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 쿠키 세션을 만들지 않고 Authorization 헤더의 JWT만 사용합니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // 정적 자원과 로그인·소셜 시작·시술 조회만 비로그인 사용자에게 공개합니다.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/error", "/images/**", "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/check-username", "/api/auth/check-email",
                                "/api/auth/social/providers", "/oauth2/authorization/**", "/login/oauth2/code/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                        // 예약 생성은 컨트롤러가 JWT 고객을 사용하므로 비회원 허용 규칙을 두지 않습니다.
                        .requestMatchers(HttpMethod.POST, "/api/reservations").authenticated()
                        // 관리자 URL은 화면 접근 여부와 관계없이 서버에서 ADMIN 역할을 다시 검사합니다.
                        .requestMatchers("/api/admin/**").hasRole(SecurityConstants.ROLE_ADMIN)
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // 비밀번호 원문을 저장하지 않도록 단방향 BCrypt 인코더를 공통 빈으로 사용합니다.
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // JWT는 Authorization 헤더로만 전달하므로 쿠키 자격 증명은 허용하지 않습니다.
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
