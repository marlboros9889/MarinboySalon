package com.marinboy.global.oauth2;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * OAuth state와 인가 요청을 HttpSession이 아닌 Redis에 10분간 보관합니다.
 */
@Component
@RequiredArgsConstructor
public class RedisOAuthAuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String KEY_PREFIX = "marinboy:oauth:";
    private static final String MOBILE_CLIENT_ATTRIBUTE = "mobileClient";
    private final RedisTemplate<String, OAuth2AuthorizationRequest> redisTemplate;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null) {
            return null;
        }
        return redisTemplate.opsForValue().get(KEY_PREFIX + state);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (authorizationRequest == null) {
            return;
        }
        redisTemplate.opsForValue().set(
                KEY_PREFIX + authorizationRequest.getState(),
                authorizationRequest,
                Duration.ofMinutes(10));
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response) {
        String state = request.getParameter("state");
        OAuth2AuthorizationRequest saved = loadAuthorizationRequest(request);
        if (saved != null && Boolean.TRUE.equals(saved.getAttribute(MOBILE_CLIENT_ATTRIBUTE))) {
            ResponseCookie cookie = ResponseCookie.from("mobileOAuth", "true")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofMinutes(2))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        if (state != null) {
            redisTemplate.delete(KEY_PREFIX + state);
        }
        return saved;
    }
}
