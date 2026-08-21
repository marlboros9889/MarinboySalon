package com.marinboy.security.oauth;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/** 카카오·네이버의 인가 코드, 토큰, 사용자 정보 조회를 한 흐름으로 처리합니다. */
@Service
public class SocialLoginService {
    private static final String KAKAO = "KAKAO";
    private static final String NAVER = "NAVER";

    private final RestClient restClient;
    private final String callbackBaseUrl;
    private final String kakaoClientId;
    private final String kakaoClientSecret;
    private final String naverClientId;
    private final String naverClientSecret;

    public SocialLoginService(RestClient.Builder restClientBuilder,
            @Value("${app.social-login.callback-base-url:http://127.0.0.1:3000}") String callbackBaseUrl,
            @Value("${app.social-login.kakao.client-id:}") String kakaoClientId,
            @Value("${app.social-login.kakao.client-secret:}") String kakaoClientSecret,
            @Value("${app.social-login.naver.client-id:}") String naverClientId,
            @Value("${app.social-login.naver.client-secret:}") String naverClientSecret) {
        this.restClient = restClientBuilder.build();
        this.callbackBaseUrl = removeTrailingSlash(callbackBaseUrl);
        this.kakaoClientId = kakaoClientId.trim();
        this.kakaoClientSecret = kakaoClientSecret.trim();
        this.naverClientId = naverClientId.trim();
        this.naverClientSecret = naverClientSecret.trim();
    }

    public Map<String, Boolean> providerAvailability() {
        return Map.of("kakao", isEnabled(KAKAO), "naver", isEnabled(NAVER));
    }

    public URI createAuthorizationUri(String provider, String state) {
        String normalizedProvider = normalizeProvider(provider);
        requireEnabled(normalizedProvider);
        String redirectUri = callbackUri(normalizedProvider);

        if (KAKAO.equals(normalizedProvider)) {
            return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                    .queryParam("response_type", "code")
                    .queryParam("client_id", kakaoClientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("state", state)
                    .build().encode().toUri();
        }
        return UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", naverClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build().encode().toUri();
    }

    public SocialProfile loadProfile(String provider, String code, String state) {
        String normalizedProvider = normalizeProvider(provider);
        requireEnabled(normalizedProvider);
        if (code == null || code.isBlank()) throw new IllegalArgumentException("소셜 로그인 인가 코드가 없습니다.");
        return KAKAO.equals(normalizedProvider)
                ? loadKakaoProfile(code)
                : loadNaverProfile(code, state);
    }

    public String normalizeProvider(String provider) {
        String normalized = provider == null ? "" : provider.trim().toUpperCase(Locale.ROOT);
        if (!KAKAO.equals(normalized) && !NAVER.equals(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        }
        return normalized;
    }

    private SocialProfile loadKakaoProfile(String code) {
        MultiValueMap<String, String> tokenForm = new LinkedMultiValueMap<>();
        tokenForm.add("grant_type", "authorization_code");
        tokenForm.add("client_id", kakaoClientId);
        tokenForm.add("redirect_uri", callbackUri(KAKAO));
        tokenForm.add("code", code);
        if (!kakaoClientSecret.isBlank()) tokenForm.add("client_secret", kakaoClientSecret);

        JsonNode token = requestToken("https://kauth.kakao.com/oauth/token", tokenForm);
        JsonNode profile = requestProfile("https://kapi.kakao.com/v2/user/me", requiredText(token, "access_token"));
        String socialId = requiredText(profile, "id");
        String name = firstText(profile, "/kakao_account/name", "/kakao_account/profile/nickname");
        String email = text(profile, "/kakao_account/email");
        String phone = text(profile, "/kakao_account/phone_number");
        return new SocialProfile(KAKAO, socialId, name, email, phone);
    }

    private SocialProfile loadNaverProfile(String code, String state) {
        MultiValueMap<String, String> tokenForm = new LinkedMultiValueMap<>();
        tokenForm.add("grant_type", "authorization_code");
        tokenForm.add("client_id", naverClientId);
        tokenForm.add("client_secret", naverClientSecret);
        tokenForm.add("redirect_uri", callbackUri(NAVER));
        tokenForm.add("code", code);
        tokenForm.add("state", state);

        JsonNode token = requestToken("https://nid.naver.com/oauth2.0/token", tokenForm);
        JsonNode profile = requestProfile("https://openapi.naver.com/v1/nid/me", requiredText(token, "access_token"));
        JsonNode response = profile.path("response");
        String socialId = requiredText(response, "id");
        String name = firstText(response, "/name", "/nickname");
        String email = text(response, "/email");
        String phone = text(response, "/mobile");
        return new SocialProfile(NAVER, socialId, name, email, phone);
    }

    private JsonNode requestToken(String uri, MultiValueMap<String, String> form) {
        JsonNode result = restClient.post().uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().body(JsonNode.class);
        if (result == null) throw new IllegalArgumentException("소셜 로그인 토큰을 받지 못했습니다.");
        return result;
    }

    private JsonNode requestProfile(String uri, String accessToken) {
        JsonNode result = restClient.get().uri(uri)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve().body(JsonNode.class);
        if (result == null) throw new IllegalArgumentException("소셜 로그인 사용자 정보를 받지 못했습니다.");
        return result;
    }

    private boolean isEnabled(String provider) {
        if (KAKAO.equals(provider)) return !kakaoClientId.isBlank();
        return !naverClientId.isBlank() && !naverClientSecret.isBlank();
    }

    private void requireEnabled(String provider) {
        if (!isEnabled(provider)) throw new IllegalArgumentException(provider + " 로그인 API 키가 설정되지 않았습니다.");
    }

    private String callbackUri(String provider) {
        return callbackBaseUrl + "/login/oauth2/code/" + provider.toLowerCase(Locale.ROOT);
    }

    private String requiredText(JsonNode node, String fieldName) {
        String value = node.path(fieldName).asText("");
        if (value.isBlank()) throw new IllegalArgumentException("소셜 로그인 응답에 " + fieldName + " 값이 없습니다.");
        return value;
    }

    private String firstText(JsonNode node, String firstPointer, String secondPointer) {
        String first = text(node, firstPointer);
        return first == null || first.isBlank() ? text(node, secondPointer) : first;
    }

    private String text(JsonNode node, String pointer) {
        String value = node.at(pointer).asText("");
        return value.isBlank() ? null : value;
    }

    private String removeTrailingSlash(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }

    /** 외부 제공자 응답에서 우리 서비스에 필요한 최소 사용자 정보만 전달합니다. */
    public record SocialProfile(String provider, String socialId, String name, String email, String phone) {}
}
