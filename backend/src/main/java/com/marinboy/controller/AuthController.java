package com.marinboy.controller;

import com.marinboy.dto.UserDto;
import com.marinboy.dto.JwtAuthResponseDto;
import com.marinboy.dto.JwtLoginRequestDto;
import com.marinboy.dto.SignupRequestDto;
import com.marinboy.dto.UserResponseDto;
import com.marinboy.security.jwt.JwtTokenProvider;
import com.marinboy.security.jwt.RedisTokenBlacklistService;
import com.marinboy.service.AuthenticatedUserService;
import com.marinboy.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** 로그인·현재 사용자·로그아웃을 세션 없이 JWT로 처리합니다. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticatedUserService authenticatedUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenBlacklistService redisTokenBlacklistService;

    public AuthController(AuthService authService, AuthenticatedUserService authenticatedUserService,
            JwtTokenProvider jwtTokenProvider, RedisTokenBlacklistService redisTokenBlacklistService) {
        this.authService = authService;
        this.authenticatedUserService = authenticatedUserService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTokenBlacklistService = redisTokenBlacklistService;
    }

    //1. 일반 로그인  POST: /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> login(@Valid @RequestBody JwtLoginRequestDto request) {
        UserDto user = authService.login(request.username(), request.password());
        user.setDisplayName(user.getName());
        user.setLoginProvider("DATABASE");
        String token = jwtTokenProvider.createAccessToken(user);
        return ResponseEntity.ok(new JwtAuthResponseDto(token, "Bearer", UserResponseDto.from(user)));
    }

    //2. 고객 회원가입  POST: /api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto request) {
        // 관리자 권한은 회원가입 API에서 만들지 않고 고객 권한만 생성합니다.
        authService.signup(request);
        return ResponseEntity.noContent().build();
    }

    //3. 아이디 중복 확인  GET: /api/auth/check-username?username=...
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(Map.of("available", authService.isUsernameAvailable(username)));
    }

    //4. 이메일 중복 확인  GET: /api/auth/check-email?email=...
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(Map.of("available", authService.isEmailAvailable(email)));
    }

    //5. 현재 사용자 조회  GET: /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(Authentication authentication) {
        UserDto user = authenticatedUserService.requireUser(authentication);
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    //6. 로그아웃  POST: /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Claims claims = jwtTokenProvider.parseClaims(authorization.substring(7));
        // Redis에 토큰 ID를 만료 시각까지만 저장해 로그아웃한 토큰의 재사용을 차단합니다.
        redisTokenBlacklistService.blacklist(claims.getId(), claims.getExpiration().toInstant());
        return ResponseEntity.noContent().build();
    }
}
