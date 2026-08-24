package com.marinboy.controller;

import com.marinboy.dto.UserDto;
import com.marinboy.security.SecurityConstants;
import com.marinboy.service.SalonReservationService;
import com.marinboy.service.SalonServiceService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 관리자 화면 진입과 예약·휴무일·시술 메뉴 관리 API를 제공합니다. */
@Controller
public class AdminController {
    // 관리자 권한을 확인한 뒤 관리 화면을 반환합니다.
    @GetMapping("/admin") public String admin(HttpSession session) { requireAdmin(session); return "admin"; }
    // 모든 관리자 기능에서 공통으로 사용하는 세션 권한 검사입니다.
    static void requireAdmin(HttpSession session) {
        Object user = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(user instanceof UserDto current) || !SecurityConstants.ROLE_ADMIN.equals(current.getRole())) throw new IllegalArgumentException("관리자 로그인이 필요합니다.");
    }
    /** 관리자 화면의 비동기 요청을 처리하는 내부 REST 컨트롤러입니다. */
    @RestController
    static class AdminApi {
        private final SalonReservationService service;
        private final SalonServiceService salonService;
        // 예약 관리와 시술 메뉴 관리 서비스를 주입받습니다.
        AdminApi(SalonReservationService service, SalonServiceService salonService) { this.service = service; this.salonService = salonService; }
        // 전체 예약을 페이지 단위로 조회하고 총 개수를 함께 반환합니다.
        @GetMapping("/api/admin/reservations") Object reservations(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, HttpSession s) { requireAdmin(s); return Map.of("items", service.getReservationsPage(page, size), "total", service.countReservations(), "page", page, "size", size); }
        // 선택한 예약의 상세 정보를 조회합니다.
        @GetMapping("/api/admin/reservations/{id}") Object reservation(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession s) { requireAdmin(s); return service.getReservation(id); }
        // 가까운 시일 내 알림이 필요한 진행 예약을 조회합니다.
        @GetMapping("/api/admin/reservations/reminder-targets") Object reminderTargets(HttpSession s) { requireAdmin(s); return service.getReminderTargets(); }
        // 승인·취소·완료 등 예약 처리 상태를 변경합니다.
        @PatchMapping("/api/admin/reservations/{id}/status") ResponseEntity<Void> status(@org.springframework.web.bind.annotation.PathVariable Long id, @RequestParam String status, HttpSession s) { requireAdmin(s); service.updateReservationStatus(id, status); return ResponseEntity.noContent().build(); }
        // 거절 사유와 함께 예약을 거절 상태로 변경합니다.
        @PatchMapping("/api/admin/reservations/{id}/reject") ResponseEntity<Void> reject(@org.springframework.web.bind.annotation.PathVariable Long id, @RequestParam String reason, HttpSession s) { requireAdmin(s); service.rejectReservation(id, reason); return ResponseEntity.noContent().build(); }
        // 등록된 전체 휴무일을 조회합니다.
        @GetMapping("/api/admin/holidays") Object holidays(HttpSession s) { requireAdmin(s); return service.getHolidays(); }
        // 날짜와 사유를 받아 휴무일을 등록합니다.
        @PostMapping("/api/admin/holidays") ResponseEntity<Void> save(@RequestBody Map<String,String> body, HttpSession s) { requireAdmin(s); service.saveHoliday(LocalDate.parse(body.get("holidayDate")), body.get("reason")); return ResponseEntity.noContent().build(); }
        // 선택한 휴무일을 삭제해 다시 예약 가능하게 합니다.
        @DeleteMapping("/api/admin/holidays") ResponseEntity<Void> delete(@RequestParam LocalDate holidayDate, HttpSession s) { requireAdmin(s); service.deleteHoliday(holidayDate); return ResponseEntity.noContent().build(); }
        // 메뉴 관리 화면에 표시할 전체 시술을 조회합니다.
        @GetMapping("/api/admin/services") Object services(HttpSession s) { requireAdmin(s); return salonService.getServices(); }
        // 메뉴 정보와 대표 이미지 파일을 함께 등록합니다.
        @PostMapping(value="/api/admin/services", consumes="multipart/form-data") ResponseEntity<Void> createService(@RequestParam String name, @RequestParam String category, @RequestParam int durationMinutes, @RequestParam int price, @RequestParam(defaultValue="") String description, @RequestParam(required=false) MultipartFile image, @RequestParam(required=false) MultipartFile[] galleryImages, HttpSession s) { requireAdmin(s); salonService.saveService(null, name, category, durationMinutes, price, description, image, galleryImages); return ResponseEntity.noContent().build(); }
        // 새 이미지가 선택된 경우에만 기존 대표 이미지를 교체합니다.
        @PatchMapping(value="/api/admin/services/{id}", consumes="multipart/form-data") ResponseEntity<Void> updateService(@org.springframework.web.bind.annotation.PathVariable Long id, @RequestParam String name, @RequestParam String category, @RequestParam int durationMinutes, @RequestParam int price, @RequestParam(defaultValue="") String description, @RequestParam(required=false) MultipartFile image, @RequestParam(required=false) MultipartFile[] galleryImages, HttpSession s) { requireAdmin(s); salonService.saveService(id, name, category, durationMinutes, price, description, image, galleryImages); return ResponseEntity.noContent().build(); }
        // 예약 이력은 보존하면서 선택한 시술을 메뉴 목록에서 제외합니다.
        @DeleteMapping("/api/admin/services/{id}") ResponseEntity<Void> deleteService(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession s) { requireAdmin(s); salonService.deleteService(id); return ResponseEntity.noContent().build(); }
        @GetMapping("/api/admin/data-retention") Object retentionSummary(HttpSession s) { requireAdmin(s); return service.getRetentionSummary(); }
        @DeleteMapping("/api/admin/data-retention") Object cleanupOldData(@RequestParam String confirmation, HttpSession s) { requireAdmin(s); return service.cleanupOldData(confirmation); }
    }
}
