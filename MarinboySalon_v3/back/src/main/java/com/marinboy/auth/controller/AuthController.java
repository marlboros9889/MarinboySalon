package com.marinboy.auth.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.auth.dto.request.LoginRequest;
import com.marinboy.auth.dto.request.UserRequestDto;
import com.marinboy.auth.dto.response.UserResponseDto;
import com.marinboy.auth.service.AuthUserJwtService;
import com.marinboy.global.security.JwtProperties;
import com.marinboy.global.security.JwtProvider;
import com.marinboy.global.security.TokenStore;
import com.marinboy.user.service.AppUserService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 회원가입, 로그인, 토큰 재발급, 로그아웃 REST API입니다.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;
    private final AuthUserJwtService authUserJwtService;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final TokenStore tokenStore;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody UserRequestDto request) {
        return ResponseEntity.ok(appUserService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        UserResponseDto user = appUserService.login(request);
        String userId = user.getId().toString();
        String accessToken = jwtProvider.createAccessToken(userId, Map.of("role", user.getRole()));
        String refreshToken = jwtProvider.createRefreshToken(userId);

        tokenStore.saveRefreshToken(userId, refreshToken, jwtProperties.getRefreshTokenExpSeconds());
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshCookie(refreshToken).toString());

        return ResponseEntity.ok(Map.of("accessToken", accessToken, "user", user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(Authentication authentication) {
        Long userId = authUserJwtService.getCurrentUserId(authentication);
        return ResponseEntity.ok(appUserService.findById(userId));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(appUserService.existsByEmail(email));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        Claims claims = jwtProvider.parse(refreshToken);
        String userId = claims.getSubject();
        String savedToken = tokenStore.getRefreshToken(userId);
        if (!refreshToken.equals(savedToken)) {
            return ResponseEntity.status(401).build();
        }

        String role = appUserService.findRoleByUserId(Long.valueOf(userId));
        String accessToken = jwtProvider.createAccessToken(userId, Map.of("role", role));
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null) {
            try {
                String userId = jwtProvider.parse(refreshToken).getSubject();
                tokenStore.deleteRefreshToken(userId);
            } catch (Exception exception) {
                // 만료된 토큰이어도 브라우저 쿠키는 반드시 삭제합니다.
            }
        }

        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie().toString());
        return ResponseEntity.noContent().build();
    }

    private ResponseCookie createRefreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpSeconds())
                .build();
    }

    private ResponseCookie deleteRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }
}
