package com.marinboy.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.marinboy.dto.UserDto;
import com.marinboy.security.jwt.JwtTokenProvider;
import com.marinboy.security.oauth.RedisOAuthStateService;
import com.marinboy.security.oauth.SocialLoginService;
import com.marinboy.security.oauth.SocialProfile;
import com.marinboy.service.SocialAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 소셜 로그인 시작과 콜백을 세션 없이 Redis state + JWT로 연결합니다. */
@RestController
public class SocialLoginController {
    private static final Logger log = LoggerFactory.getLogger(SocialLoginController.class);
    private final SocialLoginService socialLoginService;
    private final RedisOAuthStateService stateService;
    private final SocialAccountService socialAccountService;
    private final JwtTokenProvider jwtTokenProvider;
    private final String frontendBaseUrl;

    public SocialLoginController(SocialLoginService socialLoginService, RedisOAuthStateService stateService,
            SocialAccountService socialAccountService, JwtTokenProvider jwtTokenProvider,
            @Value("${app.frontend.base-url:http://127.0.0.1:3000}") String frontendBaseUrl) {
        this.socialLoginService = socialLoginService;
        this.stateService = stateService;
        this.socialAccountService = socialAccountService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.frontendBaseUrl = removeTrailingSlash(frontendBaseUrl);
    }

    // 화면은 키가 실제로 준비된 제공자만 로그인 가능 상태로 표시합니다.
    @GetMapping("/api/auth/social/providers")
    public ResponseEntity<Map<String, Boolean>> providers() {
        return ResponseEntity.ok(socialLoginService.providerAvailability());
    }

    @GetMapping("/oauth2/authorization/{provider}")
    public ResponseEntity<Void> authorize(@PathVariable String provider) {
        String normalizedProvider = socialLoginService.normalizeProvider(provider);
        String state = stateService.create(normalizedProvider);
        URI providerUri = socialLoginService.createAuthorizationUri(normalizedProvider, state);
        return ResponseEntity.status(302).location(providerUri).build();
    }

    @GetMapping("/login/oauth2/code/{provider}")
    public ResponseEntity<Void> callback(@PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {
        try {
            String normalizedProvider = socialLoginService.normalizeProvider(provider);
            if (error != null) {
                throw new IllegalArgumentException("소셜 로그인이 취소되었습니다.");
            }
            if (!stateService.consume(state, normalizedProvider)) {
                throw new IllegalArgumentException("소셜 로그인 요청이 만료되었거나 올바르지 않습니다.");
            }

            SocialProfile profile = socialLoginService.loadProfile(normalizedProvider, code, state);
            UserDto user = socialAccountService.findOrCreate(profile);
            user.setDisplayName(user.getName());
            String accessToken = jwtTokenProvider.createAccessToken(user);
            // 토큰은 서버 접근 로그에 남는 query가 아니라 브라우저 내부 fragment로 전달합니다.
            URI successUri = URI.create(frontendBaseUrl + "/auth/social-callback#access_token=" + accessToken);
            return ResponseEntity.status(302).location(successUri).build();
        } catch (RuntimeException exception) {
            log.warn("소셜 로그인 콜백 처리 실패: provider={}, cause={}", provider, exception.getClass().getSimpleName());
            String message = exception.getMessage() == null ? "소셜 로그인에 실패했습니다." : exception.getMessage();
            URI errorUri = URI.create(frontendBaseUrl + "/auth/social-callback?error="
                    + URLEncoder.encode(message, StandardCharsets.UTF_8));
            return ResponseEntity.status(302).location(errorUri).build();
        }
    }

    private String removeTrailingSlash(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
