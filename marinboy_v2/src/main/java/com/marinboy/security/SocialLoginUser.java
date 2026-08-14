package com.marinboy.security;

import java.util.Map;

// 소셜 로그인 성공 후 세션에 저장할 최소 사용자 정보입니다.
public record SocialLoginUser(
        SocialLoginProvider provider,
        String socialId,
        String name,
        String email,
        String role
) {

    // OAuth2 제공자별 응답 구조가 다르기 때문에 provider에 맞춰 이름과 이메일을 꺼냅니다.
    public static SocialLoginUser from(String registrationId, Map<String, Object> attributes) {
        SocialLoginProvider provider = SocialLoginProvider.from(registrationId);

        return switch (provider) {
            case GOOGLE -> fromGoogle(attributes);
            case NAVER -> fromNaver(attributes);
            case KAKAO -> fromKakao(attributes);
            case UNKNOWN -> fromUnknown(attributes);
        };
    }

    // Google은 id, name, email이 최상위 속성으로 내려옵니다.
    private static SocialLoginUser fromGoogle(Map<String, Object> attributes) {
        return new SocialLoginUser(
                SocialLoginProvider.GOOGLE,
                stringValue(attributes.get("sub")),
                stringValue(attributes.get("name")),
                stringValue(attributes.get("email")),
                SecurityConstants.ROLE_CUSTOMER
        );
    }

    // Naver는 response 객체 안에 id, name, email이 들어오는 구조입니다.
    private static SocialLoginUser fromNaver(Map<String, Object> attributes) {
        Map<String, Object> response = mapValue(attributes.get("response"));
        return new SocialLoginUser(
                SocialLoginProvider.NAVER,
                stringValue(response.get("id")),
                stringValue(response.get("name")),
                stringValue(response.get("email")),
                SecurityConstants.ROLE_CUSTOMER
        );
    }

    // Kakao는 id가 최상위에 있고, 이름/이메일은 kakao_account 또는 profile 안에 들어옵니다.
    private static SocialLoginUser fromKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = mapValue(attributes.get("kakao_account"));
        Map<String, Object> profile = mapValue(kakaoAccount.get("profile"));
        return new SocialLoginUser(
                SocialLoginProvider.KAKAO,
                stringValue(attributes.get("id")),
                stringValue(profile.get("nickname")),
                stringValue(kakaoAccount.get("email")),
                SecurityConstants.ROLE_CUSTOMER
        );
    }

    // 알 수 없는 제공자라도 세션 저장이 실패하지 않도록 기본값으로 사용자 정보를 만듭니다.
    private static SocialLoginUser fromUnknown(Map<String, Object> attributes) {
        return new SocialLoginUser(
                SocialLoginProvider.UNKNOWN,
                stringValue(attributes.get("id")),
                stringValue(attributes.get("name")),
                stringValue(attributes.get("email")),
                SecurityConstants.ROLE_CUSTOMER
        );
    }

    // Object 값을 문자열로 안전하게 변환해 null 때문에 화면이나 세션 처리가 깨지지 않게 합니다.
    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    // 제공자별 중첩 응답을 Map으로 꺼내며, 구조가 예상과 달라도 빈 Map으로 방어합니다.
    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
