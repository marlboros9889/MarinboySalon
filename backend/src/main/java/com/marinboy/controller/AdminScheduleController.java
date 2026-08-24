package com.marinboy.controller;

import com.marinboy.dto.BusinessHourRequestDto;
import com.marinboy.service.ReservationScheduleService;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ADMIN 영업시간과 특정 휴무일 API를 예약 일정 책임으로 분리합니다. */
@RestController
public class AdminScheduleController {
    private final ReservationScheduleService reservationScheduleService;

    public AdminScheduleController(ReservationScheduleService reservationScheduleService) {
        this.reservationScheduleService = reservationScheduleService;
    }

    // 특정 휴무일은 요일별 영업시간보다 우선 적용합니다.
    @GetMapping("/api/admin/holidays")
    Object holidays() {
        return reservationScheduleService.getHolidays();
    }

    @PostMapping("/api/admin/holidays")
    ResponseEntity<Void> saveHoliday(@RequestBody Map<String, String> body) {
        reservationScheduleService.saveHoliday(LocalDate.parse(body.get("holidayDate")), body.get("reason"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/holidays")
    ResponseEntity<Void> deleteHoliday(@RequestParam LocalDate holidayDate) {
        reservationScheduleService.deleteHoliday(holidayDate);
        return ResponseEntity.noContent().build();
    }

    // 요일별 영업 여부와 시작·종료 시각을 예약 가능 시간 계산에 연결합니다.
    @GetMapping("/api/admin/business-hours")
    Object businessHours() {
        return reservationScheduleService.getBusinessHours();
    }

    @PutMapping("/api/admin/business-hours/{dayOfWeek}")
    ResponseEntity<Void> saveBusinessHour(
            @PathVariable int dayOfWeek,
            @RequestBody BusinessHourRequestDto request) {
        request.setDayOfWeek(dayOfWeek);
        reservationScheduleService.saveBusinessHour(request);
        return ResponseEntity.noContent().build();
    }
}
