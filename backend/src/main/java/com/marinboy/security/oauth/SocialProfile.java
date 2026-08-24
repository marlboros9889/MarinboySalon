package com.marinboy.security.oauth;

/** 소셜 제공자 응답을 계정 연결에 필요한 공통 사용자 정보로 변환합니다. */
public record SocialProfile(
        String provider,
        String socialId,
        String name,
        String email,
        String phone,
        boolean emailVerified) {
}
