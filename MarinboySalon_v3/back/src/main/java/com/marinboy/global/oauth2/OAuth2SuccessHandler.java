package com.marinboy.global.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.marinboy.global.security.JwtProperties;
import com.marinboy.global.security.JwtProvider;
import com.marinboy.global.security.TokenStore;
import com.marinboy.user.entity.AppUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 소셜 로그인 성공 시 일반 로그인과 같은 JWT와 Redis Refresh Token을 발급합니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final TokenStore tokenStore;

    @Value("${app.front-url}")
    private String frontUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        AppUser user = oauthUser.getAppUser();
        String userId = user.getId().toString();
        String accessToken = jwtProvider.createAccessToken(userId, Map.of("role", user.getRole()));
        String refreshToken = jwtProvider.createRefreshToken(userId);
        tokenStore.saveRefreshToken(userId, refreshToken, jwtProperties.getRefreshTokenExpSeconds());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        response.sendRedirect(frontUrl + "/oauth2/callback?accessToken=" + encodedToken);
    }
}
