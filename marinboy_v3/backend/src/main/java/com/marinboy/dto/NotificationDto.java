package com.marinboy.dto;

import java.time.LocalDateTime;

/** 관리자 알림 목록과 SSE 응답에 사용하는 DTO입니다. */
public class NotificationDto {
    private Long id;
    private Long reservationId;
    private Long adminId;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
    private String customerName;
    private String serviceName;
    private LocalDateTime reservationDateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public LocalDateTime getReservationDateTime() { return reservationDateTime; }
    public void setReservationDateTime(LocalDateTime reservationDateTime) { this.reservationDateTime = reservationDateTime; }
}
