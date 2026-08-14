package com.marinboy.controller;

import java.time.Instant;

import com.marinboy.dto.UserDto;
import com.marinboy.security.SecurityConstants;
import com.marinboy.dto.v3.JwtLoginRequestDto;
import com.marinboy.dto.v3.JwtLoginResponseDto;
import com.marinboy.security.jwt.JwtTokenProvider;
import com.marinboy.security.jwt.RedisTokenBlacklistService;
import com.marinboy.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** React 전용 JWT 로그인·로그아웃 API입니다. */
@Profile("v3")
@RestController
@RequestMapping("/api/v3/auth")
public class V3AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenBlacklistService redisTokenBlacklistService;

    public V3AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider,
            RedisTokenBlacklistService redisTokenBlacklistService) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTokenBlacklistService = redisTokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtLoginResponseDto> login(@Valid @RequestBody JwtLoginRequestDto request, HttpSession session) {
        UserDto user = authService.login(request.username(), request.password());
        // 기존 Thymeleaf 예약·관리자 기능도 같은 8082 서버 세션으로 이어서 사용합니다.
        session.setAttribute(SecurityConstants.LOGIN_USER, user);
        session.setAttribute(SecurityConstants.LOGIN_PROVIDER, "DATABASE");
        String token = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new JwtLoginResponseDto(token, "Bearer", user.getUsername(), user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, HttpSession session) {
        // 필터에서 이미 검증한 토큰의 ID를 Redis에 남겨 만료 전 재사용을 막습니다.
        Claims claims = jwtTokenProvider.parseClaims(authorization.substring(7));
        redisTokenBlacklistService.blacklist(claims.getId(), claims.getExpiration().toInstant());
        // JWT와 함께 기존 예약 화면에 사용한 서버 세션도 종료합니다.
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/social/session")
    public ResponseEntity<JwtLoginResponseDto> exchangeSocialSession(HttpSession session) {
        // OAuth2 성공 처리기가 저장한 서버 세션을 React 전용 JWT로 한 번만 교환합니다.
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user)) {
            return ResponseEntity.status(401).build();
        }
        String token = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new JwtLoginResponseDto(token, "Bearer", user.getUsername(), user.getRole()));
    }
}
