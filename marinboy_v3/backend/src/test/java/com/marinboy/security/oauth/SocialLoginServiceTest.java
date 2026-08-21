package com.marinboy.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/** API 키 설정과 제공자별 인가 주소가 올바르게 연결되는지 검증합니다. */
class SocialLoginServiceTest {

    @Test
    void exposesOnlyConfiguredProvidersAndBuildsKakaoAuthorizationUrl() {
        SocialLoginService service = new SocialLoginService(RestClient.builder(),
                "http://127.0.0.1:3000/", "kakao-client", "kakao-secret", "", "");

        URI authorizationUri = service.createAuthorizationUri("kakao", "safe-state");

        assertThat(service.providerAvailability()).containsEntry("kakao", true).containsEntry("naver", false);
        assertThat(authorizationUri.toString())
                .startsWith("https://kauth.kakao.com/oauth/authorize?")
                .contains("client_id=kakao-client")
                .contains("state=safe-state")
                .contains("login/oauth2/code/kakao");
    }
}
