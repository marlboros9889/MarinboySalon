package com.marinboy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 백엔드 기본 주소를 Next.js 고객 화면으로 연결합니다.
 */
@Controller
public class HomeController {

    private final String frontendBaseUrl;

    public HomeController(@Value("${app.frontend.base-url:http://127.0.0.1:3000}") String frontendBaseUrl) {
        // 백엔드와 프론트엔드 주소를 설정 한 곳에서 관리할 수 있도록 값을 주입받습니다.
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /** GET / 요청을 고객용 Next.js 첫 화면으로 이동합니다. */
    @GetMapping("/")
    public String home() {
        return "redirect:" + frontendBaseUrl;
    }
}
