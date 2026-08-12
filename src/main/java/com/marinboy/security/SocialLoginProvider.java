package com.marinboy.security;

// 소셜 로그인 제공자를 코드에서 안전하게 구분하기 위한 enum입니다.
public enum SocialLoginProvider {
    GOOGLE,
    NAVER,
    KAKAO,
    UNKNOWN;

    // Spring Security가 전달하는 registrationId 문자열을 우리 enum 값으로 변환합니다.
    public static SocialLoginProvider from(String registrationId) {
        if (registrationId == null || registrationId.isBlank()) {
            return UNKNOWN;
        }

        return switch (registrationId.toLowerCase()) {
            case "google" -> GOOGLE;
            case "naver" -> NAVER;
            case "kakao" -> KAKAO;
            default -> UNKNOWN;
        };
    }
}
