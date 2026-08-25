package com.marinboy.global.oauth2;

import java.util.Map;

/**
 * 소셜 제공자별 중첩 JSON에서 회원 식별값을 꺼냅니다.
 */
public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static UserInfoOAuth2 create(String provider, Map<String, Object> attributes) {
        if ("kakao".equals(provider)) {
            return kakao(attributes);
        }
        if ("naver".equals(provider)) {
            return naver(attributes);
        }
        return google(attributes);
    }

    private static UserInfoOAuth2 google(Map<String, Object> attributes) {
        return new SimpleUserInfo(
                String.valueOf(attributes.get("sub")),
                stringValue(attributes.get("email")),
                stringValue(attributes.get("name")));
    }

    @SuppressWarnings("unchecked")
    private static UserInfoOAuth2 kakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = account == null
                ? Map.of()
                : (Map<String, Object>) account.getOrDefault("profile", Map.of());
        return new SimpleUserInfo(
                String.valueOf(attributes.get("id")),
                account == null ? null : stringValue(account.get("email")),
                stringValue(profile.get("nickname")));
    }

    @SuppressWarnings("unchecked")
    private static UserInfoOAuth2 naver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return new SimpleUserInfo(
                stringValue(response.get("id")),
                stringValue(response.get("email")),
                stringValue(response.get("name")));
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record SimpleUserInfo(String providerUserId, String email, String name)
            implements UserInfoOAuth2 {
        @Override
        public String getProviderUserId() {
            return providerUserId;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
