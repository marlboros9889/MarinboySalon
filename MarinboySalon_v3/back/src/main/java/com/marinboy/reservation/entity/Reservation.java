package com.marinboy.reservation.entity;

import java.time.LocalDateTime;
import java.math.BigDecimal;

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
    // 예약을 넣은 시점의 원가·할인·최종 결제 예정 금액을 보존합니다.
    private Integer originalPrice;
    private BigDecimal discountRate;
    private Integer discountAmount;
    private Integer finalPrice;
    private Long discountEventId;
    private LocalDateTime createdAt;

    // 목록 화면에 필요한 회원과 시술 조인 값입니다.
    private String userName;
    private String userPhone;
    private String serviceName;
    private Integer servicePrice;
    private Integer durationMinutes;
}
