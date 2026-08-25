package com.marinboy.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.reservation.dto.request.ReservationStatusRequest;
import com.marinboy.reservation.dto.response.ReservationResponseDto;
import com.marinboy.reservation.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 예약 목록과 상태 변경 REST API입니다.
 */
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService service;

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> list() {
        return ResponseEntity.ok(service.getAdminList());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request.getStatus()));
    }
}
