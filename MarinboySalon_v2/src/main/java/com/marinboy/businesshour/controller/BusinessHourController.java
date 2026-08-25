package com.marinboy.businesshour.controller;

import com.marinboy.businesshour.dto.BusinessHourDto;
import com.marinboy.businesshour.service.BusinessHourService;
import com.marinboy.user.tool.LoginSessionTool;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;

/**
 * 관리자의 요일별 영업시간 조회와 수정 요청을 담당합니다.
 */
@Controller
@RequestMapping("/businessHour")
public class BusinessHourController {

    private final BusinessHourService businessHourService;
    private final LoginSessionTool loginSessionTool;

    public BusinessHourController(BusinessHourService businessHourService, LoginSessionTool loginSessionTool) {
        this.businessHourService = businessHourService;
        this.loginSessionTool = loginSessionTool;
    }

    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        List<BusinessHourDto> businessHourList = businessHourService.getList();
        model.addAttribute("businessHourList", businessHourList);
        return "businessHour/list";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long id,
                         @RequestParam(required = false) LocalTime openTime,
                         @RequestParam(required = false) LocalTime closeTime,
                         @RequestParam(defaultValue = "false") boolean closed,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        BusinessHourDto businessHourDto = new BusinessHourDto();
        businessHourDto.setId(id);
        businessHourDto.setOpenTime(openTime);
        businessHourDto.setCloseTime(closeTime);
        businessHourDto.setClosed(closed);

        businessHourService.update(businessHourDto);
        redirectAttributes.addFlashAttribute("successMessage", "영업시간이 수정되었습니다.");
        return "redirect:/businessHour/list";
    }
}
