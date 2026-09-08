package com.marinboy.discountevent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.discountevent.dto.response.DiscountEventResponseDto;
import com.marinboy.discountevent.service.DiscountEventService;

import lombok.RequiredArgsConstructor;

/** 고객 예약 화면에 현재 적용 중인 할인 이벤트만 전달합니다. */
@RestController
@RequestMapping("/api/discount-events")
@RequiredArgsConstructor
public class DiscountEventController {

    private final DiscountEventService service;

    @GetMapping("/current")
    public ResponseEntity<DiscountEventResponseDto> current() {
        return ResponseEntity.ok(service.getCurrentEvent());
    }
}
