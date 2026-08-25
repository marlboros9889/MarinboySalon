package com.marinboy.reservation.controller;

import com.marinboy.reservation.dto.ReservationDto;
import com.marinboy.reservation.service.ReservationService;
import com.marinboy.serviceitem.dto.ServiceItemDto;
import com.marinboy.serviceitem.service.ServiceItemService;
import com.marinboy.user.dto.LoginUserDto;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 고객 본인의 예약 등록, 목록, 수정, 취소 요청을 담당합니다.
 */
@Controller
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final ServiceItemService serviceItemService;
    private final LoginSessionTool loginSessionTool;

    public ReservationController(ReservationService reservationService,
                                 ServiceItemService serviceItemService,
                                 LoginSessionTool loginSessionTool) {
        this.reservationService = reservationService;
        this.serviceItemService = serviceItemService;
        this.loginSessionTool = loginSessionTool;
    }

    @GetMapping("/insertForm")
    public String insertForm(@RequestParam(required = false) Long serviceId,
                             HttpSession session,
                             Model model) {
        LoginUserDto loginUserDto = loginSessionTool.getLoginUser(session);
        if (loginUserDto == null) {
            return "redirect:/user/loginForm?returnTo=/reservation/insertForm";
        }

        addServiceList(model);
        model.addAttribute("selectedServiceId", serviceId);
        return "reservation/insertForm";
    }

    @PostMapping("/insert")
    public String insert(@RequestParam Long serviceId,
                         @RequestParam LocalDate reservationDate,
                         @RequestParam LocalTime reservationTime,
                         @RequestParam(required = false) String requestMemo,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        LoginUserDto loginUserDto = loginSessionTool.getLoginUser(session);
        if (loginUserDto == null) {
            return "redirect:/user/loginForm?returnTo=/reservation/insertForm";
        }

        ReservationDto reservationDto = createReservationDto(
                loginUserDto.getId(), serviceId, reservationDate, reservationTime, requestMemo
        );

        try {
            reservationService.insert(reservationDto);
        } catch (IllegalArgumentException exception) {
            addServiceList(model);
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("selectedServiceId", serviceId);
            model.addAttribute("reservationDate", reservationDate);
            model.addAttribute("reservationTime", reservationTime);
            model.addAttribute("requestMemo", requestMemo);
            return "reservation/insertForm";
        }

        redirectAttributes.addFlashAttribute("successMessage", "예약이 접수되었습니다.");
        return "redirect:/reservation/list";
    }

    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        LoginUserDto loginUserDto = loginSessionTool.getLoginUser(session);
        if (loginUserDto == null) {
            return "redirect:/user/loginForm?returnTo=/reservation/list";
        }

        List<ReservationDto> reservationList = reservationService.getMyList(loginUserDto.getId());
        model.addAttribute("reservationList", reservationList);
        return "reservation/list";
    }

    @GetMapping("/updateForm")
    public String updateForm(@RequestParam Long id, HttpSession session, Model model) {
        LoginUserDto loginUserDto = loginSessionTool.getLoginUser(session);
        if (loginUserDto == null) {
            return "redirect:/user/loginForm?returnTo=/reservation/list";
        }

        ReservationDto reservationDto = reservationService.getMyReservation(id, loginUserDto.getId());
        addServiceList(model);
        model.addAttribute("reservationDto", reservationDto);
        return "reservation/updateForm";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long id,
                         @RequestParam Long serviceId,
                         @RequestParam LocalDate reservationDate,
                         @RequestParam LocalTime reservationTime,
                         @RequestParam(required = false) String requestMemo,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        LoginUserDto loginUserDto = loginSessionTool.getLoginUser(session);
        if (loginUserDto == null) {
            return "redirect:/user/loginForm?returnTo=/reservation/list";
        }

        ReservationDto reservationDto = createReservationDto(
                loginUserDto.getId(), serviceId, reservationDate, reservationTime, requestMemo
        );
        reservationDto.setId(id);
        reservationService.update(reservationDto);

        redirectAttributes.addFlashAttribute("successMessage", "예약이 수정되었습니다.");
        return "redirect:/reservation/list";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        LoginUserDto loginUserDto = loginSessionTool.getLoginUser(session);
        if (loginUserDto == null) {
            return "redirect:/user/loginForm?returnTo=/reservation/list";
        }

        reservationService.cancel(id, loginUserDto.getId());
        redirectAttributes.addFlashAttribute("successMessage", "예약이 취소되었습니다.");
        return "redirect:/reservation/list";
    }

    private ReservationDto createReservationDto(Long userId,
                                                Long serviceId,
                                                LocalDate reservationDate,
                                                LocalTime reservationTime,
                                                String requestMemo) {
        LocalDateTime reservationStart = LocalDateTime.of(reservationDate, reservationTime);

        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setUserId(userId);
        reservationDto.setServiceId(serviceId);
        reservationDto.setReservationStart(reservationStart);
        reservationDto.setRequestMemo(requestMemo);

        return reservationDto;
    }

    private void addServiceList(Model model) {
        List<ServiceItemDto> serviceItemList = serviceItemService.getActiveList();
        model.addAttribute("serviceItemList", serviceItemList);
    }
}
