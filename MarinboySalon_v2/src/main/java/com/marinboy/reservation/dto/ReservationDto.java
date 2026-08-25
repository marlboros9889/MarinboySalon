package com.marinboy.reservation.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 예약 정보와 화면에 함께 보여줄 회원, 시술 이름을 전달합니다.
 */
public class ReservationDto {

    private Long id;
    private Long userId;
    private Long serviceId;
    private LocalDateTime reservationStart;
    private String status;
    private String requestMemo;
    private LocalDateTime createdAt;
    private String userName;
    private String serviceName;
    private int servicePrice;
    private int durationMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getReservationStart() {
        return reservationStart;
    }

    public void setReservationStart(LocalDateTime reservationStart) {
        this.reservationStart = reservationStart;
    }

    /**
     * 수정 화면의 date 입력값에 바로 사용할 날짜를 반환합니다.
     */
    public LocalDate getReservationDate() {
        if (reservationStart == null) {
            return null;
        }
        return reservationStart.toLocalDate();
    }

    /**
     * 수정 화면의 time 입력값에 바로 사용할 시간을 반환합니다.
     */
    public LocalTime getReservationTime() {
        if (reservationStart == null) {
            return null;
        }
        return reservationStart.toLocalTime();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestMemo() {
        return requestMemo;
    }

    public void setRequestMemo(String requestMemo) {
        this.requestMemo = requestMemo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(int servicePrice) {
        this.servicePrice = servicePrice;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
