package com.marinboy.businesshour.dto;

import java.time.LocalTime;

/**
 * 요일별 영업 시작, 종료, 휴무 정보를 전달합니다.
 */
public class BusinessHourDto {

    private Long id;
    private int dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean closed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * 화면에서 숫자 대신 한글 요일을 보여줍니다.
     */
    public String getDayName() {
        String[] dayNames = {"", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};

        if (dayOfWeek < 1 || dayOfWeek > 7) {
            return "알 수 없음";
        }

        return dayNames[dayOfWeek];
    }
}
