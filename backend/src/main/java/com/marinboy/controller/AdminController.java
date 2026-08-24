package com.marinboy.controller;

import com.marinboy.dto.BusinessHourRequestDto;
import com.marinboy.service.GoogleCalendarDisplayService;
import com.marinboy.service.ReservationService;
import com.marinboy.service.ReservationScheduleService;
import com.marinboy.service.ServiceItemService;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** JWT의 ADMIN 권한으로 보호되는 예약·영업 설정·시술 메뉴 운영 API를 제공합니다. */
@RestController
public class AdminController {
    private final ReservationService reservationService;
    private final ReservationScheduleService reservationScheduleService;
    private final ServiceItemService serviceItemService;
    private final GoogleCalendarDisplayService googleCalendarDisplayService;

    public AdminController(ReservationService reservationService,
            ReservationScheduleService reservationScheduleService,
            ServiceItemService serviceItemService,
            GoogleCalendarDisplayService googleCalendarDisplayService) {
        this.reservationService = reservationService;
        this.reservationScheduleService = reservationScheduleService;
        this.serviceItemService = serviceItemService;
        this.googleCalendarDisplayService = googleCalendarDisplayService;
    }

    //1. 캘린더 ID는 공개 빌드 변수가 아니라 ADMIN 권한 API를 통과한 화면에만 전달합니다.
    @GetMapping("/api/admin/calendar")
    Object calendar() {
        return googleCalendarDisplayService.getDisplayConfiguration();
    }

    //2. 예약 목록은 페이지 데이터와 전체 건수를 함께 반환해 관리자 페이지 이동을 계산하게 합니다.
    @GetMapping("/api/admin/reservations")
    Object reservations(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return Map.of("items", reservationService.getReservationsPage(page, size),
                "total", reservationService.countReservations(), "page", page, "size", size);
    }

    //3. 예약 상태 변경 규칙은 Service에서 검증하고 성공 시 본문 없는 204 응답을 반환합니다.
    @PatchMapping("/api/admin/reservations/{id}/status")
    ResponseEntity<Void> status(@PathVariable Long id, @RequestParam String status) {
        reservationService.updateReservationStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    //4. 특정 휴무일은 요일별 영업시간보다 우선하도록 조회·등록·삭제 API를 한 묶음으로 제공합니다.
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

    //5. 요일별 영업 여부와 시작·종료 시각을 예약 가능 시간 계산에 연결합니다.
    @GetMapping("/api/admin/business-hours")
    Object businessHours() {
        return reservationScheduleService.getBusinessHours();
    }

    @PutMapping("/api/admin/business-hours/{dayOfWeek}")
    ResponseEntity<Void> saveBusinessHour(
            @PathVariable int dayOfWeek, @RequestBody BusinessHourRequestDto request) {
        request.setDayOfWeek(dayOfWeek);
        reservationScheduleService.saveBusinessHour(request);
        return ResponseEntity.noContent().build();
    }

    //6. 시술 메뉴와 이미지는 multipart 요청으로 함께 받아 DB와 파일 저장을 한 흐름으로 처리합니다.
    @GetMapping("/api/admin/services")
    Object services() {
        return serviceItemService.getServices();
    }

    @PostMapping(value = "/api/admin/services", consumes = "multipart/form-data")
    ResponseEntity<Void> createService(@RequestParam String name, @RequestParam String category,
            @RequestParam int durationMinutes, @RequestParam int price,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) MultipartFile[] galleryImages) {
        serviceItemService.saveService(null, name, category, durationMinutes, price, description, image, galleryImages);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/api/admin/services/{id}", consumes = "multipart/form-data")
    ResponseEntity<Void> updateService(@PathVariable Long id, @RequestParam String name,
            @RequestParam String category, @RequestParam int durationMinutes, @RequestParam int price,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) MultipartFile[] galleryImages) {
        serviceItemService.saveService(id, name, category, durationMinutes, price, description, image, galleryImages);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/services/{id}")
    ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceItemService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
