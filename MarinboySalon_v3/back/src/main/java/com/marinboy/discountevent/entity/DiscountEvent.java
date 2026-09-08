package com.marinboy.discountevent.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/** 관리자 기간 할인 이벤트 한 건을 담는 엔티티입니다. */
@Getter
@Setter
public class DiscountEvent {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal discountRate;
    private LocalDateTime createdAt;
}
