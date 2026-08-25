package com.marinboy.reservation.service;

import java.util.List;

import com.marinboy.reservation.dto.request.ReservationRequestDto;
import com.marinboy.reservation.dto.response.ReservationResponseDto;

public interface ReservationService {

    List<ReservationResponseDto> getMyList(Long userId);

    List<ReservationResponseDto> getAdminList();

    ReservationResponseDto getDetail(Long id, Long userId, boolean admin);

    ReservationResponseDto insert(Long userId, ReservationRequestDto request);

    ReservationResponseDto update(Long id, Long userId, ReservationRequestDto request);

    void cancel(Long id, Long userId, boolean admin);

    ReservationResponseDto updateStatus(Long id, String status);
}
