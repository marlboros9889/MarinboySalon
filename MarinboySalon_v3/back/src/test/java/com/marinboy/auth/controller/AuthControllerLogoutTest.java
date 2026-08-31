package com.marinboy.auth.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.marinboy.auth.service.AuthUserJwtService;
import com.marinboy.global.security.JwtProperties;
import com.marinboy.global.security.JwtProvider;
import com.marinboy.global.security.TokenStore;
import com.marinboy.user.service.AppUserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 로그아웃이 현재 Access Token의 JTI를 즉시 차단하는지 확인합니다. */
class AuthControllerLogoutTest {

    @Test
    void blocksCurrentAccessTokenWithRemainingTtl() {
        AppUserService appUserService = mock(AppUserService.class);
        AuthUserJwtService authUserJwtService = mock(AuthUserJwtService.class);
        JwtProvider jwtProvider = mock(JwtProvider.class);
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setCookieSecure(false);
        TokenStore tokenStore = mock(TokenStore.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        Claims claims = Jwts.claims();
        claims.setId("logout-jti");
        claims.setExpiration(new Date(System.currentTimeMillis() + 120_000L));
        when(request.getHeader("Authorization")).thenReturn("Bearer access-token");
        when(jwtProvider.parse("access-token")).thenReturn(claims);

        AuthController controller = new AuthController(
                appUserService,
                authUserJwtService,
                jwtProvider,
                jwtProperties,
                tokenStore);
        controller.logout(null, request, response);

        verify(tokenStore).blockAccessToken(
                eq("logout-jti"),
                longThat(seconds -> seconds > 0L && seconds <= 120L));
    }
}
