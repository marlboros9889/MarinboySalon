package com.marinboy.dto;

/** 로그인 성공 시 Bearer 토큰과 화면에 필요한 사용자 정보를 함께 반환합니다. */
public record JwtAuthResponseDto(String accessToken, String tokenType, UserResponseDto user) { }
