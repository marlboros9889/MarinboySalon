package com.marinboy.reservation.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * reservation 테이블과 목록 조인 결과를 함께 담는 예약 엔티티입니다.
 */
@Getter
@Setter
public class Reservation {

    private Long id;
    private Long userId;
    private Long serviceId;
    private LocalDateTime reservationStart;
    private String status;
    private String requestMemo;
    private String calendarEventId;
    private LocalDateTime createdAt;

    // 목록 화면에 필요한 회원과 시술 조인 값입니다.
    private String userName;
    private String userPhone;
    private String serviceName;
    private Integer servicePrice;
    private Integer durationMinutes;
}
