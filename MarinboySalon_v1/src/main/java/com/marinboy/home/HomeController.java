package com.marinboy.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 첫 화면 이동을 담당하는 Controller입니다.
 */
@Controller
public class HomeController {

    /**
     * 브라우저에서 기본 주소로 접속하면 홈 JSP를 보여줍니다.
     */
    @GetMapping("/")
    public String home() {
        return "home/index";
    }
}
