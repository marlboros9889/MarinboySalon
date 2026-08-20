package com.marinboy.service;

/** 예약 커밋 이후 관리자 알림에 필요한 최소 정보입니다. */
public record ReservationCreatedEvent(Long reservationId, String customerName, String serviceName) {
}
