package com.marinboy.reservation.service;

import com.marinboy.reservation.dto.ReservationDto;

import java.util.List;

/**
 * 예약 기능에서 Controller가 사용할 작업 목록입니다.
 */
public interface ReservationService {

    void insert(ReservationDto reservationDto);

    List<ReservationDto> getMyList(Long userId);

    ReservationDto getMyReservation(Long id, Long userId);

    void update(ReservationDto reservationDto);

    void cancel(Long id, Long userId);
}
