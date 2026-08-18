package com.marinboy.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.marinboy.dto.UserDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;

/** 세션 기반 자체 로그인과 REST 요청을 사용할 수 있도록 Spring Security를 설정합니다. */
@Configuration
public class SecurityConfig {
    private final SocialLoginSuccessHandler socialLoginSuccessHandler;
    private final List<String> reactAllowedOrigins;

    public SecurityConfig(
            SocialLoginSuccessHandler socialLoginSuccessHandler,
            @Value("${app.cors.allowed-origin:http://localhost:5173}") String configuredOrigin
    ) {
        this.socialLoginSuccessHandler = socialLoginSuccessHandler;
        // 배포 주소와 로컬 Vite 주소를 함께 허용해 React의 세션 쿠키 요청을 지원합니다.
        LinkedHashSet<String> origins = new LinkedHashSet<>();
        if (configuredOrigin != null && !configuredOrigin.isBlank()) {
            origins.add(configuredOrigin.trim());
        }
        origins.add("http://localhost:5173");
        origins.add("http://127.0.0.1:5173");
        this.reactAllowedOrigins = List.copyOf(origins);
    }

    // 관리자 및 DB 진단 경로는 세션의 관리자 역할을 확인한 뒤 접근을 허용합니다.
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        return http
                // 세션 쿠키를 사용하는 기존 API는 쿠키와 헤더의 CSRF 토큰이 일치해야 변경할 수 있습니다.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(csrfCookieFilter(), BasicAuthenticationFilter.class)
                // React 개발 서버가 로그인 세션 쿠키를 포함해 legacy API를 호출할 수 있도록 허용합니다.
                .cors(cors -> cors.configurationSource(legacyCorsConfigurationSource()))
                .addFilterBefore(sessionAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                // 공개 고객 기능과 관리자 전용 기능을 URL 수준에서도 분리합니다.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**", "/api/admin/**", "/api/db/**", "/api/db-time")
                        .access(adminSessionAuthorization())
                        .anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")))
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .successHandler(socialLoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            // 공급자 내부 오류 전문은 노출하지 않고 고객이 이해할 수 있는 안내만 전달합니다.
                            String message = URLEncoder.encode(oauthFailureMessage(exception), StandardCharsets.UTF_8);
                            response.sendRedirect("/login?oauthError=" + message);
                        }))
                .build();
    }

    private CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        return repository;
    }

    private OncePerRequestFilter csrfCookieFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response,
                    jakarta.servlet.FilterChain filterChain
            ) throws java.io.IOException, jakarta.servlet.ServletException {
                CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                if (csrfToken != null) csrfToken.getToken();
                filterChain.doFilter(request, response);
            }
        };
    }

    /** React 기본 화면에서 사용하는 /api/** 요청에 credential 포함 CORS 정책을 적용합니다. */
    private CorsConfigurationSource legacyCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(reactAllowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /** OAuth 토큰 교환 실패를 설정 점검이 가능한 안내 문구로 변환합니다. */
    private String oauthFailureMessage(Exception exception) {
        String detail = exception.getMessage() == null ? "" : exception.getMessage();
        if (detail.contains("invalid_token_response") || detail.contains("401 Unauthorized")) {
            return "소셜 로그인 설정을 확인 중입니다. 잠시 후 다시 시도해 주세요.";
        }
        return "소셜 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.";
    }

    // 카카오 계정 세션이 남아 있어도 매번 카카오 계정 인증 화면을 표시합니다.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private OncePerRequestFilter sessionAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response,
                    jakarta.servlet.FilterChain filterChain
            ) throws java.io.IOException, jakarta.servlet.ServletException {
                var session = request.getSession(false);
                Object loginUser = session == null ? null : session.getAttribute(SecurityConstants.LOGIN_USER);
                if (loginUser instanceof UserDto user
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    private AuthorizationManager<RequestAuthorizationContext> adminSessionAuthorization() {
        return (authentication, context) -> {
            var session = context.getRequest().getSession(false);
            Object loginUser = session == null ? null : session.getAttribute(SecurityConstants.LOGIN_USER);
            boolean admin = loginUser instanceof UserDto user
                    && SecurityConstants.ROLE_ADMIN.equals(user.getRole());
            return new AuthorizationDecision(admin);
        };
    }
}
