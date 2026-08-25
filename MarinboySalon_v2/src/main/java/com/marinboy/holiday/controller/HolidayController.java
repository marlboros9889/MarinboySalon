package com.marinboy.holiday.controller;

import com.marinboy.holiday.dto.HolidayDto;
import com.marinboy.holiday.service.HolidayService;
import com.marinboy.user.tool.LoginSessionTool;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자의 임시 휴무일 조회, 등록, 삭제 요청을 담당합니다.
 */
@Controller
@RequestMapping("/holiday")
public class HolidayController {

    private final HolidayService holidayService;
    private final LoginSessionTool loginSessionTool;

    public HolidayController(HolidayService holidayService, LoginSessionTool loginSessionTool) {
        this.holidayService = holidayService;
        this.loginSessionTool = loginSessionTool;
    }

    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        List<HolidayDto> holidayList = holidayService.getList();
        model.addAttribute("holidayList", holidayList);
        return "holiday/list";
    }

    @PostMapping("/insert")
    public String insert(@RequestParam LocalDate holidayDate,
                         @RequestParam(required = false) String reason,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        HolidayDto holidayDto = new HolidayDto();
        holidayDto.setHolidayDate(holidayDate);
        holidayDto.setReason(reason);
        holidayService.insert(holidayDto);

        redirectAttributes.addFlashAttribute("successMessage", "휴무일이 등록되었습니다.");
        return "redirect:/holiday/list";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        holidayService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "휴무일이 삭제되었습니다.");
        return "redirect:/holiday/list";
    }
}
