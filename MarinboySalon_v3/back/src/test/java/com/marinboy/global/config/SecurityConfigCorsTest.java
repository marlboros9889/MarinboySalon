package com.marinboy.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.marinboy.auth.service.CustomOAuth2UserService;
import com.marinboy.global.oauth2.OAuth2SuccessHandler;
import com.marinboy.global.oauth2.RedisOAuthAuthorizationRequestRepository;
import com.marinboy.global.security.JwtAuthenticationFilter;
import com.marinboy.global.security.RestAccessDeniedHandler;
import com.marinboy.global.security.RestAuthenticationEntryPoint;

/**
 * 로컬에서 사용하는 두 호스트가 같은 백엔드 API에 연결되는지 확인합니다.
 */
class SecurityConfigCorsTest {

    @SuppressWarnings("unchecked")
    @Test
    void localFrontOriginsAreAllowed() {
        SecurityConfig securityConfig = new SecurityConfig(
                mock(JwtAuthenticationFilter.class),
                mock(CustomOAuth2UserService.class),
                mock(OAuth2SuccessHandler.class),
                mock(RedisOAuthAuthorizationRequestRepository.class),
                mock(ObjectProvider.class),
                mock(RestAuthenticationEntryPoint.class),
                mock(RestAccessDeniedHandler.class));
        ReflectionTestUtils.setField(
                securityConfig,
                "allowedOrigins",
                "http://localhost:3000,http://127.0.0.1:3000");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/service-items"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:3000", "http://127.0.0.1:3000");
    }
}
