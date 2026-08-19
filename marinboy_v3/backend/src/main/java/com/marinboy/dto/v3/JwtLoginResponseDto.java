package com.marinboy.dto.v3;

/** 민감한 비밀번호를 제외한 JWT 로그인 응답 DTO입니다. */
public record JwtLoginResponseDto(String accessToken, String tokenType, String username, String role) { }
