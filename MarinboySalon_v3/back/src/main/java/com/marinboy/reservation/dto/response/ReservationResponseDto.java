package com.marinboy.reservation.dto.response;

import java.time.LocalDateTime;

import com.marinboy.reservation.entity.Reservation;

import lombok.Builder;
import lombok.Getter;

/**
 * 고객과 관리자 예약 화면에 전달하는 응답 DTO입니다.
 */
@Getter
@Builder
public class ReservationResponseDto {

    private Long id;
    private Long userId;
    private Long serviceId;
    private LocalDateTime reservationStart;
    private LocalDateTime reservationEnd;
    private String status;
    private String requestMemo;
    // 관리자 화면에서 외부 일정 등록 여부를 확인하기 위한 Google Calendar 이벤트 번호입니다.
    private String calendarEventId;
    private LocalDateTime createdAt;
    private String userName;
    private String userPhone;
    private String serviceName;
    private Integer servicePrice;
    private Integer durationMinutes;

    public static ReservationResponseDto from(Reservation reservation) {
        LocalDateTime end = reservation.getReservationStart()
                .plusMinutes(reservation.getDurationMinutes());
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .serviceId(reservation.getServiceId())
                .reservationStart(reservation.getReservationStart())
                .reservationEnd(end)
                .status(reservation.getStatus())
                .requestMemo(reservation.getRequestMemo())
                .calendarEventId(reservation.getCalendarEventId())
                .createdAt(reservation.getCreatedAt())
                .userName(reservation.getUserName())
                .userPhone(reservation.getUserPhone())
                .serviceName(reservation.getServiceName())
                .servicePrice(reservation.getServicePrice())
                .durationMinutes(reservation.getDurationMinutes())
                .build();
    }
}
