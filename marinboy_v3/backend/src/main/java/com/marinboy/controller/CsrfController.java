package com.marinboy.controller;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 세션 기반 화면이 상태 변경 요청에 사용할 CSRF 토큰을 제공합니다.
@RestController
public class CsrfController {

    //1. CSRF 토큰 조회  GET: /api/csrf
    @GetMapping("/api/csrf")
    public Map<String, String> csrfToken(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }
}
