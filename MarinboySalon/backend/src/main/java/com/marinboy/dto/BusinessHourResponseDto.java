package com.marinboy.dto;

/** 고객 예약 계산과 관리자 화면에 사용하는 요일별 영업 규칙입니다. */
public class BusinessHourResponseDto {
    private Long id;
    private Integer dayOfWeek;
    private Boolean open;
    private String openTime;
    private String closeTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Boolean getOpen() { return open; }
    public void setOpen(Boolean open) { this.open = open; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
}
