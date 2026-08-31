package com.marinboy.reservation.service;

import java.time.LocalDate;
import java.util.List;

import com.marinboy.reservation.dto.request.ReservationRequestDto;
import com.marinboy.reservation.dto.response.ReservationResponseDto;

// 예약 가능 시간 계산, 고객 예약, 관리자 상태 변경의 업무 규칙을 정의합니다.
public interface ReservationService {

    List<ReservationResponseDto> getMyList(Long userId);

    List<ReservationResponseDto> getAdminList();

    List<String> getAvailableTimes(LocalDate date, Long serviceId);

    ReservationResponseDto getDetail(Long id, Long userId, boolean admin);

    ReservationResponseDto insert(Long userId, ReservationRequestDto request);

    ReservationResponseDto update(Long id, Long userId, ReservationRequestDto request);

    void cancel(Long id, Long userId, boolean admin);

    ReservationResponseDto updateStatus(Long id, String status);
}
