package com.marinboy.controller;

import com.marinboy.service.ReservationService;
import com.marinboy.service.ServiceItemService;
import com.marinboy.service.GoogleCalendarDisplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** JWT의 ADMIN 권한으로 보호되는 운영 API를 제공합니다. */
@RestController
@Tag(name = "관리자", description = "예약·휴무일·시술 메뉴·보관 데이터 관리 기능")
public class AdminController {
    /** 관리자 화면의 비동기 요청을 처리하며 권한 검사는 중앙 SecurityConfig에 위임합니다. */
        private final ReservationService reservationService;
        private final ServiceItemService serviceItemService;
        private final GoogleCalendarDisplayService googleCalendarDisplayService;

        public AdminController(ReservationService reservationService, ServiceItemService serviceItemService,
                GoogleCalendarDisplayService googleCalendarDisplayService) {
            this.reservationService = reservationService;
            this.serviceItemService = serviceItemService;
            this.googleCalendarDisplayService = googleCalendarDisplayService;
        }

        @GetMapping("/api/admin/calendar")
        @Operation(summary = "관리자 Google Calendar 표시 설정 조회")
        Object calendar() {
            // 캘린더 ID는 공개 빌드 변수가 아니라 ADMIN 권한 API를 통과한 화면에만 전달합니다.
            return googleCalendarDisplayService.getDisplayConfiguration();
        }

        @GetMapping("/api/admin/reservations")
        @Operation(summary = "전체 예약 목록 조회")
        Object reservations(@RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "5") int size) {
            return Map.of("items", reservationService.getReservationsPage(page, size),
                    "total", reservationService.countReservations(), "page", page, "size", size);
        }

        @GetMapping("/api/admin/reservations/{id}")
        @Operation(summary = "예약 상세 조회")
        Object reservation(@PathVariable Long id) {
            return reservationService.getReservation(id);
        }

        @GetMapping("/api/admin/reservations/reminder-targets")
        @Operation(summary = "예약 알림 대상 조회")
        Object reminderTargets() {
            return reservationService.getReminderTargets();
        }

        @PatchMapping("/api/admin/reservations/{id}/status")
        @Operation(summary = "예약 상태 변경")
        ResponseEntity<Void> status(@PathVariable Long id, @RequestParam String status) {
            reservationService.updateReservationStatus(id, status);
            return ResponseEntity.noContent().build();
        }

        @PatchMapping("/api/admin/reservations/{id}/reject")
        @Operation(summary = "예약 거절 처리")
        ResponseEntity<Void> reject(@PathVariable Long id, @RequestParam String reason) {
            reservationService.rejectReservation(id, reason);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/api/admin/holidays")
        @Operation(summary = "휴무일 목록 조회")
        Object holidays() {
            return reservationService.getHolidays();
        }

        @PostMapping("/api/admin/holidays")
        @Operation(summary = "휴무일 등록")
        ResponseEntity<Void> saveHoliday(@RequestBody Map<String, String> body) {
            reservationService.saveHoliday(LocalDate.parse(body.get("holidayDate")), body.get("reason"));
            return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/api/admin/holidays")
        @Operation(summary = "휴무일 삭제")
        ResponseEntity<Void> deleteHoliday(@RequestParam LocalDate holidayDate) {
            reservationService.deleteHoliday(holidayDate);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/api/admin/services")
        @Operation(summary = "관리자 시술 메뉴 목록 조회")
        Object services() {
            return serviceItemService.getServices();
        }

        @PostMapping(value = "/api/admin/services", consumes = "multipart/form-data")
        @Operation(summary = "시술 메뉴 등록")
        ResponseEntity<Void> createService(@RequestParam String name, @RequestParam String category,
                @RequestParam int durationMinutes, @RequestParam int price,
                @RequestParam(defaultValue = "") String description,
                @RequestParam(required = false) MultipartFile image,
                @RequestParam(required = false) MultipartFile[] galleryImages) {
            serviceItemService.saveService(null, name, category, durationMinutes, price, description, image, galleryImages);
            return ResponseEntity.noContent().build();
        }

        @PatchMapping(value = "/api/admin/services/{id}", consumes = "multipart/form-data")
        @Operation(summary = "시술 메뉴 수정")
        ResponseEntity<Void> updateService(@PathVariable Long id, @RequestParam String name,
                @RequestParam String category, @RequestParam int durationMinutes, @RequestParam int price,
                @RequestParam(defaultValue = "") String description,
                @RequestParam(required = false) MultipartFile image,
                @RequestParam(required = false) MultipartFile[] galleryImages) {
            serviceItemService.saveService(id, name, category, durationMinutes, price, description, image, galleryImages);
            return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/api/admin/services/{id}")
        @Operation(summary = "시술 메뉴 삭제")
        ResponseEntity<Void> deleteService(@PathVariable Long id) {
            serviceItemService.deleteService(id);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/api/admin/data-retention")
        @Operation(summary = "보관 기간 만료 데이터 요약")
        Object retentionSummary() {
            return reservationService.getRetentionSummary();
        }

        @DeleteMapping("/api/admin/data-retention")
        @Operation(summary = "보관 기간 만료 데이터 정리")
        Object cleanupOldData(@RequestParam String confirmation) {
            return reservationService.cleanupOldData(confirmation);
        }
}
