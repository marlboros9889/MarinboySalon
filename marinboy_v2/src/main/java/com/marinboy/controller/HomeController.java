package com.marinboy.controller;

import com.marinboy.service.SalonServiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 새 MyBatis 프로젝트의 기본 진입 화면을 반환하는 컨트롤러입니다.
@Controller
public class HomeController {

    private final SalonServiceService salonServiceService;

    public HomeController(SalonServiceService salonServiceService) {
        // 홈 화면 상단에 실제 등록된 시술 수를 보여주기 위해 시술 서비스를 주입받습니다.
        this.salonServiceService = salonServiceService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // 고객 예약 화면에서 사용할 기본 문구를 서버에서 내려줍니다.
        model.addAttribute("projectName", "마린 헤어 스튜디오");
        model.addAttribute("headline", "1인 뷰티/헤어샵 예약");
        model.addAttribute("description", "원하는 시술과 날짜를 선택해 예약을 요청할 수 있습니다.");
        model.addAttribute("serviceCount", salonServiceService.getServices().size());
        return "index";
    }

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/my-reservations")
    public String myReservations() { return "my-reservations"; }

    @GetMapping("/treatment-history")
    public String treatmentHistory() { return "treatment-history"; }
}
