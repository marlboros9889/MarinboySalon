package com.marinboy.controller;

import com.marinboy.dto.UserDto;
import com.marinboy.security.SecurityConstants;
import com.marinboy.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 로그인 화면 제공과 사용자 인증 API를 담당합니다. */
@Controller
public class AuthController {
    private final AuthService authService;
    private final boolean kakaoLoginEnabled;
    private final boolean googleLoginEnabled;
    private final boolean naverLoginEnabled;
    // 실제 계정 조회와 비밀번호 검증은 인증 서비스에 위임합니다.
    public AuthController(AuthService authService,
            @Value("${spring.security.oauth2.client.registration.kakao.client-id:not-configured}") String kakaoClientId,
            @Value("${spring.security.oauth2.client.registration.google.client-id:not-configured}") String googleClientId,
            @Value("${spring.security.oauth2.client.registration.naver.client-id:not-configured}") String naverClientId) {
        this.authService = authService;
        this.kakaoLoginEnabled = !"not-configured".equals(kakaoClientId) && !kakaoClientId.isBlank();
        this.googleLoginEnabled = !"not-configured".equals(googleClientId) && !googleClientId.isBlank();
        this.naverLoginEnabled = !"not-configured".equals(naverClientId) && !naverClientId.isBlank();
    }
    // 브라우저에 로그인 템플릿을 반환합니다.
    @GetMapping("/login") public String loginPage(Model model) {
        model.addAttribute("kakaoLoginEnabled", kakaoLoginEnabled);
        model.addAttribute("googleLoginEnabled", googleLoginEnabled);
        model.addAttribute("naverLoginEnabled", naverLoginEnabled);
        return "login";
    }
    /** 로그인 폼에서 호출하는 인증 REST API입니다. */
    @RestController
    @Tag(name = "인증", description = "일반 로그인과 사용자 인증 기능")
    static class AuthApiController {
        private final AuthService authService;
        AuthApiController(AuthService authService) { this.authService = authService; }
        @PostMapping("/api/auth/login")
        @Operation(summary = "아이디·비밀번호 로그인")
        ResponseEntity<UserDto> login(@RequestBody UserDto request, HttpSession session) {
            // 전달받은 계정 정보가 일치하는 사용자를 조회합니다.
            UserDto user = authService.login(request.getUsername(), request.getPassword());
            // 화면에서 공통으로 사용할 표시 이름을 실제 고객명으로 설정합니다.
            user.setDisplayName(user.getName());
            user.setLoginProvider("DATABASE");
            // 이후 요청에서 로그인 여부와 권한을 확인하도록 세션에 저장합니다.
            session.setAttribute(SecurityConstants.LOGIN_USER, user);
            session.setAttribute(SecurityConstants.LOGIN_PROVIDER, "DATABASE");
            return ResponseEntity.ok(user);
        }

        /** POST /api/auth/signup 요청으로 고객 일반 회원가입을 처리합니다. */
        @PostMapping("/api/auth/signup")
        @Operation(summary = "고객 회원가입")
        ResponseEntity<Void> signup(@RequestBody UserDto request) {
            // 회원가입은 고객 권한만 생성하며 관리자 권한은 이 API로 만들 수 없습니다.
            authService.signup(request);
            return ResponseEntity.noContent().build();
        }

        //1. 아이디 중복 확인  GET: /api/auth/check-username?username=...
        @GetMapping("/api/auth/check-username")
        @Operation(summary = "회원가입 아이디 중복 확인")
        ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
            return ResponseEntity.ok(Map.of("available", authService.isUsernameAvailable(username)));
        }

        //2. 이메일 중복 확인  GET: /api/auth/check-email?email=...
        @GetMapping("/api/auth/check-email")
        @Operation(summary = "회원가입 이메일 중복 확인")
        ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
            return ResponseEntity.ok(Map.of("available", authService.isEmailAvailable(email)));
        }
    }
}
