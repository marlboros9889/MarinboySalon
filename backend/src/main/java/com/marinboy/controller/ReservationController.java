package com.marinboy.controller;

import com.marinboy.dto.ReservationDto;
import com.marinboy.dto.ServiceItemDto;
import com.marinboy.dto.UserDto;
import com.marinboy.service.ReservationService;
import com.marinboy.service.ReservationScheduleService;
import com.marinboy.service.ServiceItemService;
import com.marinboy.service.AuthService;
import com.marinboy.service.AuthenticatedUserService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
public class ReservationController {
    private final ServiceItemService serviceItemService;
    private final ReservationService reservationService;
    private final ReservationScheduleService reservationScheduleService;
    private final AuthService authService;
    private final AuthenticatedUserService authenticatedUserService;

    public ReservationController(
            ServiceItemService serviceItemService,
            ReservationService reservationService,
            ReservationScheduleService reservationScheduleService,
            AuthService authService,
            AuthenticatedUserService authenticatedUserService) {
        this.serviceItemService = serviceItemService;
        this.reservationService = reservationService;
        this.reservationScheduleService = reservationScheduleService;
        this.authService = authService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/api/services")
    public List<ServiceItemDto> services() {
        // 예약 화면의 시술 선택 목록을 반환합니다.
        return serviceItemService.getServices();
    }

    @GetMapping("/api/services/{serviceId}/available-slots")
    public ReservationDto availableSlots(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // 영업시간, 휴무일, 시술 시간과 기존 예약을 반영한 시간만 반환합니다.
        return reservationScheduleService.getAvailableSlots(serviceId, date);
    }

    @PostMapping("/api/reservations")
    public ResponseEntity<Void> createReservation(
            @RequestBody ReservationDto request, Authentication authentication) {
        // JWT 고객 정보로 예약자를 고정하여 다른 고객 연락처로 예약이 연결되지 않게 합니다.
        UserDto user = authenticatedUserService.requireUser(authentication);
        if (!user.isProfileComplete()) {
            throw new IllegalArgumentException("예약 전에 고객 정보에서 이메일과 연락처를 입력해 주세요.");
        }
        request.setCustomerName(user.getName());
        request.setCustomerEmail(user.getEmail());
        request.setCustomerPhone(user.getPhone());
        reservationService.createReservation(request, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/customers/my-reservations")
    public ResponseEntity<List<ReservationDto>> myReservations(Authentication authentication) {
        // 로그인 고객의 전화번호를 기준으로 진행 중인 예약을 조회합니다.
        UserDto user = authenticatedUserService.requireUser(authentication);
        if (user.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reservationService.getCustomerActiveReservations(user.getId()));
    }

    // 수정 화면에서 로그인한 고객의 예약 한 건만 조회해 다른 고객 예약 노출을 차단합니다.
    @GetMapping("/api/customers/my-reservations/{reservationId}")
    public ResponseEntity<ReservationDto> myReservation(
            @PathVariable Long reservationId, Authentication authentication) {
        UserDto user = authenticatedUserService.requireUser(authentication);
        if (user.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reservationService.getCustomerReservation(reservationId, user.getId()));
    }

    @PutMapping("/api/customers/my-reservations/{reservationId}")
    public ResponseEntity<Void> updateMyReservation(@PathVariable Long reservationId, @RequestBody ReservationDto request,
            Authentication authentication) {
        UserDto user = authenticatedUserService.requireUser(authentication);
        if (user.getId() == null)
            return ResponseEntity.status(401).build();
        reservationService.updateCustomerReservation(reservationId, user.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/customers/my-reservations/{reservationId}")
    public ResponseEntity<Void> cancelMyReservation(@PathVariable Long reservationId, Authentication authentication) {
        UserDto user = authenticatedUserService.requireUser(authentication);
        if (user.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        reservationService.cancelCustomerReservation(reservationId, user.getId());
        return ResponseEntity.noContent().build();
    }

    // 고객 본인이 이름·이메일·연락처를 수정하며 다음 로그인 토큰부터 변경값을 사용합니다.
    @PutMapping("/api/customers/me")
    public ResponseEntity<UserDto> updateMe(@RequestBody UserDto request, Authentication authentication) {
        UserDto user = authenticatedUserService.requireUser(authentication);
        UserDto updated = authService.updateProfile(user, request);
        return ResponseEntity.ok(updated);
    }
}
