package com.marinboy.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
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

    @Test
    void readsVerifiedEmailFlagFromGoogleProfile() {
        // 이메일 검증 값이 실제 제공자 응답에서 넘어와야 기존 일반 계정 연결을 허용할 수 있습니다.
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"google-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"sub\":\"google-id\",\"name\":\"구글 고객\","
                                + "\"email\":\"verified@example.com\",\"email_verified\":true}",
                        MediaType.APPLICATION_JSON));
        SocialLoginService service = new SocialLoginService(builder,
                "http://127.0.0.1:8082", "", "", "", "", "google-client", "google-secret");

        SocialProfile profile = service.loadProfile("google", "authorization-code", "unused-state");

        assertThat(profile.email()).isEqualTo("verified@example.com");
        assertThat(profile.emailVerified()).isTrue();
        server.verify();
    }
}
