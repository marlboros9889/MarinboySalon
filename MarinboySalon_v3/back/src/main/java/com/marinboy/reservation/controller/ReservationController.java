package com.marinboy.reservation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.auth.service.AuthUserJwtService;
import com.marinboy.reservation.dto.request.ReservationRequestDto;
import com.marinboy.reservation.dto.response.ReservationResponseDto;
import com.marinboy.reservation.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 고객 본인의 예약 등록, 조회, 변경, 취소 REST API입니다.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;
    private final AuthUserJwtService authUserJwtService;

    /** 고객이 날짜와 메뉴를 고르면 실제로 선택 가능한 30분 단위 시작 시각만 돌려줍니다. */
    @GetMapping("/available-times")
    public ResponseEntity<List<String>> availableTimes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long serviceId) {
        return ResponseEntity.ok(service.getAvailableTimes(date, serviceId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponseDto>> myList(Authentication authentication) {
        Long userId = authUserJwtService.getCurrentUserId(authentication);
        return ResponseEntity.ok(service.getMyList(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> detail(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = authUserJwtService.getCurrentUserId(authentication);
        boolean admin = authUserJwtService.isAdmin(authentication);
        return ResponseEntity.ok(service.getDetail(id, userId, admin));
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> insert(
            @Valid @RequestBody ReservationRequestDto request,
            Authentication authentication) {
        Long userId = authUserJwtService.getCurrentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insert(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequestDto request,
            Authentication authentication) {
        Long userId = authUserJwtService.getCurrentUserId(authentication);
        return ResponseEntity.ok(service.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancel(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = authUserJwtService.getCurrentUserId(authentication);
        boolean admin = authUserJwtService.isAdmin(authentication);
        service.cancel(id, userId, admin);
        return ResponseEntity.ok(Map.of("success", true, "message", "예약이 취소되었습니다."));
    }
}
