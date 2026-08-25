package com.marinboy.businesshour.entity;

import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 요일별 영업시간 엔티티입니다.
 */
@Getter
@Setter
public class BusinessHour {

    private Long id;
    private Integer dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean closed;
}
