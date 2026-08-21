package com.marinboy.dto;

/** 관리자 화면에서 전달한 요일별 영업 규칙 변경값입니다. */
public class BusinessHourRequestDto {
    private Integer dayOfWeek;
    private Boolean open;
    private String openTime;
    private String closeTime;

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Boolean getOpen() { return open; }
    public void setOpen(Boolean open) { this.open = open; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
}
