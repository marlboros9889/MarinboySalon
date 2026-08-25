package com.marinboy.serviceitem.controller;

import com.marinboy.serviceitem.dto.ServiceItemDto;
import com.marinboy.serviceitem.service.ServiceItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 고객용 시술 메뉴 화면 이동을 담당합니다.
 */
@Controller
@RequestMapping("/serviceItem")
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    public ServiceItemController(ServiceItemService serviceItemService) {
        this.serviceItemService = serviceItemService;
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
}
