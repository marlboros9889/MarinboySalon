package com.marinboy.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

import com.marinboy.global.security.JwtAuthenticationFilter;
import com.marinboy.global.security.RestAccessDeniedHandler;
import com.marinboy.global.security.RestAuthenticationEntryPoint;
import com.marinboy.auth.service.CustomOAuth2UserService;
import com.marinboy.global.oauth2.OAuth2FailureHandler;
import com.marinboy.global.oauth2.OAuth2SuccessHandler;
import com.marinboy.global.oauth2.RedisOAuthAuthorizationRequestRepository;

import lombok.RequiredArgsConstructor;

/**
 * v3 REST API의 JWT 인증, 관리자 권한, CORS를 설정합니다.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final OAuth2FailureHandler oauth2FailureHandler;
    private final RedisOAuthAuthorizationRequestRepository authorizationRequestRepository;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/oauth2/**", "/login/oauth2/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/signup", "/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/check-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/service-items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/discount-events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/service-items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/available-times").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth -> oauth
                    .authorizationEndpoint(endpoint -> endpoint
                            .authorizationRequestResolver(
                                    googleAccountSelectionResolver(clientRegistrationRepository))
                            .authorizationRequestRepository(authorizationRequestRepository))
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .successHandler(oauth2SuccessHandler)
                    // OAuth 제공자 오류를 기본 오류 페이지가 아닌 프런트 로그인 화면에서 안내합니다.
                    .failureHandler(oauth2FailureHandler));
        }
        return http.build();
    }

    /**
     * Google은 브라우저에 로그인된 계정이 있어도 계정 선택 화면을 보여 줍니다.
     * Kakao와 Naver는 제공자 세션을 강제로 끄지 않아 기존 로그인 경험을 유지합니다.
     */
    private OAuth2AuthorizationRequestResolver googleAccountSelectionResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                String provider = request.getRequestURI().substring(
                        request.getRequestURI().lastIndexOf('/') + 1);
                return addProviderLoginPrompt(authorizationRequest, provider);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, registrationId);
                return addProviderLoginPrompt(authorizationRequest, registrationId);
            }
        };
    }

    /** 제공자별로 이전 계정의 자동 승인을 막는 로그인 화면을 요청합니다. */
    private OAuth2AuthorizationRequest addProviderLoginPrompt(
            OAuth2AuthorizationRequest authorizationRequest, String provider) {
        if (authorizationRequest == null) {
            return authorizationRequest;
        }

        String parameterName = null;
        String parameterValue = null;
        if ("google".equals(provider)) {
            parameterName = "prompt";
            parameterValue = "select_account";
        } else if ("kakao".equals(provider)) {
            parameterName = "prompt";
            parameterValue = "login";
        } else if ("naver".equals(provider)) {
            parameterName = "auth_type";
            parameterValue = "reauthenticate";
        }
        if (parameterName == null) {
            return authorizationRequest;
        }
        String requestParameterName = parameterName;
        String requestParameterValue = parameterValue;

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(parameters -> parameters.put(requestParameterName, requestParameterValue))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
