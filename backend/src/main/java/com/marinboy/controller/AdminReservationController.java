package com.marinboy.controller;

import com.marinboy.service.GoogleCalendarDisplayService;
import com.marinboy.service.ReservationService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ADMIN 예약 목록·상태·Calendar 표시 API를 한 예약 운영 흐름으로 제공합니다. */
@RestController
public class AdminReservationController {
    private final ReservationService reservationService;
    private final GoogleCalendarDisplayService googleCalendarDisplayService;

    public AdminReservationController(
            ReservationService reservationService,
            GoogleCalendarDisplayService googleCalendarDisplayService) {
        this.reservationService = reservationService;
        this.googleCalendarDisplayService = googleCalendarDisplayService;
    }

    // Calendar ID는 공개 빌드 변수가 아니라 ADMIN 권한 API를 통과한 화면에만 전달합니다.
    @GetMapping("/api/admin/calendar")
    Object calendar() {
        return googleCalendarDisplayService.getDisplayConfiguration();
    }

    // 페이지 결과와 전체 건수를 함께 반환해 관리자 화면이 필요한 예약만 다시 조회하게 합니다.
    @GetMapping("/api/admin/reservations")
    Object reservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return Map.of(
                "items", reservationService.getReservationsPage(page, size),
                "total", reservationService.countReservations(),
                "page", page,
                "size", size);
    }

    @PatchMapping("/api/admin/reservations/{id}/status")
    ResponseEntity<Void> status(@PathVariable Long id, @RequestParam String status) {
        reservationService.updateReservationStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
