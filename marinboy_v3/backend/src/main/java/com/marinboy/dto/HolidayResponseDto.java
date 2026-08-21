package com.marinboy.dto;

import java.time.LocalDate;

/** 관리자가 지정한 특정 휴무일과 사유를 화면에 전달합니다. */
public class HolidayResponseDto {
    private LocalDate holidayDate;
    private String reason;

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
