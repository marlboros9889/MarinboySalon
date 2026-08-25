package com.marinboy.holiday.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 관리자가 등록한 임시 휴무일 정보를 전달합니다.
 */
public class HolidayDto {

    private Long id;
    private LocalDate holidayDate;
    private String reason;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
