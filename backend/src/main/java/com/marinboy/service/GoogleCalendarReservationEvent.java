package com.marinboy.service;

import java.time.LocalDateTime;

/** DB 커밋 후 Google Calendar 일정 생성에 필요한 예약 정보를 전달합니다. */
public record GoogleCalendarReservationEvent(
        String customerName,
        String customerPhone,
        String serviceName,
        LocalDateTime reservationDateTime,
        int durationMinutes
) {
}
