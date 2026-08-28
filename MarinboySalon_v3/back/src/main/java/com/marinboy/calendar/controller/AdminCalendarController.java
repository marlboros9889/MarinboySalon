package com.marinboy.calendar.controller;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.calendar.GoogleCalendarService;

import jakarta.validation.constraints.Email;

/** 관리자만 Google Calendar 연동 상태를 안전하게 확인하는 REST API입니다. */
@RestController
@RequestMapping("/api/admin/calendar")
public class AdminCalendarController {

    private final ObjectProvider<GoogleCalendarService> googleCalendarService;

    public AdminCalendarController(ObjectProvider<GoogleCalendarService> googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    /** 연동을 끄거나 키를 비운 경우에도 서버 오류 없이 상태를 알려 줍니다. */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> checkConnection() {
        GoogleCalendarService calendarService = googleCalendarService.getIfAvailable();
        if (calendarService == null) {
            return ResponseEntity.ok(Map.of(
                    "connected", false,
                    "message", "Google Calendar 연동이 활성화되어 있지 않습니다."));
        }

        return ResponseEntity.ok(calendarService.checkConnection());
    }

    /** 개인 캘린더 공유가 막혔을 때 사용할 서비스 계정 전용 예약 캘린더를 생성합니다. */
    @PostMapping("/dedicated")
    public ResponseEntity<Map<String, String>> createDedicatedCalendar(
            @RequestParam @Email(message = "공유할 이메일 형식을 확인해 주세요.") String ownerEmail) throws Exception {
        GoogleCalendarService calendarService = googleCalendarService.getIfAvailable();
        if (calendarService == null) {
            throw new IllegalStateException("Google Calendar 연동이 활성화되어 있지 않습니다.");
        }

        return ResponseEntity.status(201).body(calendarService.createDedicatedCalendar(ownerEmail));
    }
}
