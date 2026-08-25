package com.marinboy.serviceitem.controller;

import com.marinboy.serviceitem.dto.ServiceItemDto;
import com.marinboy.serviceitem.service.ServiceItemService;
import com.marinboy.user.tool.LoginSessionTool;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 고객용 시술 메뉴 화면 이동을 담당합니다.
 */
@Controller
@RequestMapping("/serviceItem")
public class ServiceItemController {

    private final ServiceItemService serviceItemService;
    private final LoginSessionTool loginSessionTool;

    public ServiceItemController(ServiceItemService serviceItemService, LoginSessionTool loginSessionTool) {
        this.serviceItemService = serviceItemService;
        this.loginSessionTool = loginSessionTool;
    }

    /**
     * 활성화된 시술 목록을 조회하여 list.jsp에 전달합니다.
     */
    @GetMapping("/list")
    public String list(Model model) {
        List<ServiceItemDto> serviceItemList = serviceItemService.getActiveList();
        model.addAttribute("serviceItemList", serviceItemList);

        return "serviceItem/list";
    }

    @GetMapping("/adminList")
    public String adminList(HttpSession session, Model model) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        List<ServiceItemDto> serviceItemList = serviceItemService.getList();
        model.addAttribute("serviceItemList", serviceItemList);
        return "serviceItem/adminList";
    }

    @GetMapping("/insertForm")
    public String insertForm(HttpSession session) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        return "serviceItem/insertForm";
    }

    @PostMapping("/insert")
    public String insert(@RequestParam String name,
                         @RequestParam int price,
                         @RequestParam int durationMinutes,
                         @RequestParam(required = false) String description,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        ServiceItemDto serviceItemDto = createServiceItemDto(name, price, durationMinutes, description, true);
        serviceItemService.insert(serviceItemDto);

        redirectAttributes.addFlashAttribute("successMessage", "시술 메뉴가 등록되었습니다.");
        return "redirect:/serviceItem/adminList";
    }

    @GetMapping("/updateForm")
    public String updateForm(@RequestParam Long id, HttpSession session, Model model) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        ServiceItemDto serviceItemDto = serviceItemService.getServiceItem(id);
        model.addAttribute("serviceItemDto", serviceItemDto);
        return "serviceItem/updateForm";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long id,
                         @RequestParam String name,
                         @RequestParam int price,
                         @RequestParam int durationMinutes,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "false") boolean active,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        ServiceItemDto serviceItemDto = createServiceItemDto(name, price, durationMinutes, description, active);
        serviceItemDto.setId(id);
        serviceItemService.update(serviceItemDto);

        redirectAttributes.addFlashAttribute("successMessage", "시술 메뉴가 수정되었습니다.");
        return "redirect:/serviceItem/adminList";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!loginSessionTool.isAdmin(session)) {
            return "redirect:/user/loginForm";
        }

        serviceItemService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "시술 메뉴가 비활성화되었습니다.");
        return "redirect:/serviceItem/adminList";
    }

    private ServiceItemDto createServiceItemDto(String name,
                                                int price,
                                                int durationMinutes,
                                                String description,
                                                boolean active) {
        ServiceItemDto serviceItemDto = new ServiceItemDto();
        serviceItemDto.setName(name);
        serviceItemDto.setPrice(price);
        serviceItemDto.setDurationMinutes(durationMinutes);
        serviceItemDto.setDescription(description);
        serviceItemDto.setActive(active);

        return serviceItemDto;
    }
}
