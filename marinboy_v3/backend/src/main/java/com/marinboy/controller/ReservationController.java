package com.marinboy.controller;

import com.marinboy.dto.ReservationDto;
import com.marinboy.dto.ServiceDto;
import com.marinboy.dto.UserDto;
import com.marinboy.security.SecurityConstants;
import com.marinboy.service.ReservationService;
import com.marinboy.service.MenuService;
import com.marinboy.service.AuthService;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 고객의 시술 조회와 예약 흐름을 담당합니다. */
@RestController
@Tag(name = "예약 관리", description = "고객 예약 생성·조회·수정·취소 기능")
public class ReservationController {
    private final MenuService serviceService;
    private final ReservationService reservationService;
    private final AuthService authService;

    public ReservationController(
            MenuService serviceService,
            ReservationService reservationService, AuthService authService) {
        this.serviceService = serviceService;
        this.reservationService = reservationService;
        this.authService = authService;
    }

    @GetMapping("/api/services")
    @Operation(summary = "시술 메뉴 전체 조회")
    public List<ServiceDto> services() {
        // 예약 화면의 시술 선택 목록을 반환합니다.
        return serviceService.getServices();
    }

    @GetMapping("/api/services/{serviceId}/available-slots")
    @Operation(summary = "시술별 예약 가능 시간 조회")
    public ReservationDto availableSlots(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // 영업시간, 휴무일, 시술 시간과 기존 예약을 반영한 시간만 반환합니다.
        return reservationService.getAvailableSlots(serviceId, date);
    }

    @PostMapping("/api/reservations")
    @Operation(summary = "새 예약 생성")
    public ResponseEntity<Void> createReservation(@RequestBody ReservationDto request) {
        // 중복 시간과 필수 동의 여부를 검증한 후 예약을 저장합니다.
        reservationService.createReservation(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/customers/history")
    @Operation(summary = "고객 이전 시술 이력 조회")
    public ResponseEntity<List<ReservationDto>> customerHistory(
            @RequestParam String customerPhone, HttpSession session) {
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user)) return ResponseEntity.status(401).build();
        boolean admin = SecurityConstants.ROLE_ADMIN.equals(user.getRole());
        if (!admin && !customerPhone.equals(user.getPhone())) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(reservationService.getCustomerHistory(customerPhone));
    }

    @GetMapping("/api/customers/my-reservations")
    @Operation(summary = "내 진행 중인 예약 조회")
    public ResponseEntity<List<ReservationDto>> myReservations(HttpSession session) {
        // 로그인 고객의 전화번호를 기준으로 진행 중인 예약을 조회합니다.
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user) || user.getPhone() == null || user.getPhone().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reservationService.getCustomerActiveReservations(user.getPhone()));
    }

    // 수정 화면에서 로그인한 고객의 예약 한 건만 조회해 다른 고객 예약 노출을 차단합니다.
    @GetMapping("/api/customers/my-reservations/{reservationId}")
    @Operation(summary = "내 예약 수정용 상세 조회")
    public ResponseEntity<ReservationDto> myReservation(
            @PathVariable Long reservationId, HttpSession session) {
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user) || user.getPhone() == null || user.getPhone().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reservationService.getCustomerReservation(reservationId, user.getPhone()));
    }

    @PutMapping("/api/customers/my-reservations/{reservationId}")
    @Operation(summary = "내 예약 수정")
    public ResponseEntity<Void> updateMyReservation(@PathVariable Long reservationId, @RequestBody ReservationDto request, HttpSession session) {
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user) || user.getPhone() == null || user.getPhone().isBlank())
            return ResponseEntity.status(401).build();
        reservationService.updateCustomerReservation(reservationId, user.getPhone(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/customers/my-reservations/{reservationId}")
    @Operation(summary = "내 예약 취소")
    public ResponseEntity<Void> cancelMyReservation(@PathVariable Long reservationId, HttpSession session) {
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user) || user.getPhone() == null || user.getPhone().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        reservationService.cancelCustomerReservation(reservationId, user.getPhone());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/auth/me")
    @Operation(summary = "현재 로그인 사용자 조회")
    public ResponseEntity<UserDto> me(HttpSession session) {
        // 예약 폼 자동 입력에 사용할 로그인 사용자 정보를 반환합니다.
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        return loginUser instanceof UserDto user
                ? ResponseEntity.ok(user)
                : ResponseEntity.noContent().build();
    }

    // 고객 본인이 이름·이메일·연락처를 수정하고 세션 정보도 즉시 갱신합니다.
    @PutMapping("/api/customers/me")
    @Operation(summary = "내 고객 정보 수정")
    public ResponseEntity<UserDto> updateMe(@RequestBody UserDto request, HttpSession session) {
        Object loginUser = session.getAttribute(SecurityConstants.LOGIN_USER);
        if (!(loginUser instanceof UserDto user)) return ResponseEntity.status(401).build();
        UserDto updated = authService.updateProfile(user, request);
        session.setAttribute(SecurityConstants.LOGIN_USER, updated);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/api/auth/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<Void> logout(HttpSession session) {
        // 세션을 폐기해 저장된 로그인 정보를 제거합니다.
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
