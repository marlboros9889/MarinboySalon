package com.marinboy.holiday.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 날짜별 임시 휴무일 엔티티입니다.
 */
@Getter
@Setter
public class Holiday {

    private Long id;
    private LocalDate holidayDate;
    private String reason;
    private LocalDateTime createdAt;
}
