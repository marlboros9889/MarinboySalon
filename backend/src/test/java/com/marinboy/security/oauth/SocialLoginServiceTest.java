package com.marinboy.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/** API 키 설정과 제공자별 인가 주소가 올바르게 연결되는지 검증합니다. */
class SocialLoginServiceTest {

    @Test
    void exposesOnlyConfiguredProvidersAndBuildsKakaoAuthorizationUrl() {
        // 카카오 키만 설정된 환경에서는 카카오 버튼과 콜백 주소만 활성화되는지 확인합니다.
        SocialLoginService service = new SocialLoginService(RestClient.builder(),
                "http://127.0.0.1:3000/", "kakao-client", "kakao-secret", "", "", "", "");

        URI authorizationUri = service.createAuthorizationUri("kakao", "safe-state");

        assertThat(service.providerAvailability()).containsEntry("kakao", true).containsEntry("naver", false);
        assertThat(authorizationUri.toString())
                .startsWith("https://kauth.kakao.com/oauth/authorize?")
                .contains("client_id=kakao-client")
                .contains("state=safe-state")
                .contains("login/oauth2/code/kakao");
    }

    @Test
    void exposesGoogleAndBuildsOpenIdAuthorizationUrl() {
        // Google은 OpenID 범위와 위조 방지 state를 모두 포함해 인가 주소를 만들어야 합니다.
        SocialLoginService service = new SocialLoginService(RestClient.builder(),
                "http://127.0.0.1:3000", "", "", "", "", "google-client", "google-secret");

        URI authorizationUri = service.createAuthorizationUri("google", "google-state");

        assertThat(service.providerAvailability()).containsEntry("google", true);
        assertThat(authorizationUri.toString())
                .startsWith("https://accounts.google.com/o/oauth2/v2/auth?")
                .contains("client_id=google-client")
                .contains("scope=openid%20profile%20email")
                .contains("state=google-state")
                .contains("login/oauth2/code/google");
    }
}
