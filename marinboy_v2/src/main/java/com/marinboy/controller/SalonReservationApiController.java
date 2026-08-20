package com.marinboy.controller;

import com.marinboy.dto.ReservationDto;
import com.marinboy.dto.ServiceDto;
import com.marinboy.dto.UserDto;
import com.marinboy.security.SecurityConstants;
import com.marinboy.service.SalonReservationService;
import com.marinboy.service.SalonServiceService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/** 고객의 시술 조회와 예약 흐름을 담당합니다. */
@RestController
public class SalonReservationApiController {
    private final SalonServiceService serviceService;
    private final SalonReservationService reservationService;

    public SalonReservationApiController(
            SalonServiceService serviceService,
            SalonReservationService reservationService) {
        this.serviceService = serviceService;
        this.reservationService = reservationService;
    }

    @GetMapping("/api/services")
    public List<ServiceDto> services() {
        // 예약 화면의 시술 선택 목록을 반환합니다.
        return serviceService.getServices();
    }

    @GetMapping("/api/services/{serviceId}/available-slots")
    public ReservationDto availableSlots(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // 영업시간, 휴무일, 시술 시간과 기존 예약을 반영한 시간만 반환합니다.
        return reservationService.getAvailableSlots(serviceId, date);
    }

    @PostMapping("/api/reservations")
    public ResponseEntity<Void> createReservation(@RequestBody ReservationDto request) {
        // 중복 시간과 필수 동의 여부를 검증한 후 예약을 저장합니다.
        reservationService.createReservation(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/customers/history")
    public ResponseEntity<List<ReservationDto>> customerHistory(
            @RequestParam String customerPhone, HttpSession session) {
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user)) return ResponseEntity.status(401).build();
        boolean admin = SecurityConstants.ROLE_ADMIN.equals(user.getRole());
        if (!admin && !customerPhone.equals(user.getPhone())) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(reservationService.getCustomerHistory(customerPhone));
    }

    @GetMapping("/api/customers/my-reservations")
    public ResponseEntity<List<ReservationDto>> myReservations(HttpSession session) {
        // 로그인 고객의 전화번호를 기준으로 진행 중인 예약을 조회합니다.
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user) || user.getPhone() == null || user.getPhone().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reservationService.getCustomerActiveReservations(user.getPhone()));
    }
    @PutMapping("/api/customers/my-reservations/{reservationId}")
    public ResponseEntity<Void> updateMyReservation(@PathVariable Long reservationId, @RequestBody ReservationDto request, HttpSession session) {
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user) || user.getPhone() == null || user.getPhone().isBlank())
            return ResponseEntity.status(401).build();
        reservationService.updateCustomerReservation(reservationId, user.getPhone(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/customers/my-reservations/{reservationId}")
    public ResponseEntity<Void> cancelMyReservation(@PathVariable Long reservationId, HttpSession session) {
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user) || user.getPhone() == null || user.getPhone().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        reservationService.cancelCustomerReservation(reservationId, user.getPhone());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<UserDto> me(HttpSession session) {
        // 예약 폼 자동 입력에 사용할 로그인 사용자 정보를 반환합니다.
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        return loginUser instanceof UserDto user
                ? ResponseEntity.ok(user)
                : ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        // 세션을 폐기해 저장된 로그인 정보를 제거합니다.
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
