package com.marinboy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 백엔드 기본 주소를 Next.js v3 고객 화면으로 연결합니다.
 */
@Controller
public class HomeController {

    private final String reactApplicationUrl;

    public HomeController(@Value("${app.react.redirect-uri:http://127.0.0.1:3000}") String reactApplicationUrl) {
        // 기본 진입 시 오래된 Thymeleaf 첫 화면이 열리지 않도록 React 주소를 주입받습니다.
        this.reactApplicationUrl = reactApplicationUrl;
    }

    /** GET / 요청을 고객용 React v3 첫 화면으로 이동합니다. */
    @GetMapping("/")
    public String home() {
        return "redirect:" + reactApplicationUrl;
    }
}
