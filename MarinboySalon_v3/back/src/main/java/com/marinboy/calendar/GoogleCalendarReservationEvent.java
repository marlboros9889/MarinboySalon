package com.marinboy.calendar;

import java.time.LocalDateTime;

import com.marinboy.reservation.entity.Reservation;

/** DB 커밋 후 Google Calendar 일정 생성에 필요한 예약 정보입니다. */
public record GoogleCalendarReservationEvent(
        Long reservationId,
        String customerName,
        String customerPhone,
        String serviceName,
        LocalDateTime reservationDateTime,
        int durationMinutes) {

    public static GoogleCalendarReservationEvent from(Reservation reservation) {
        return new GoogleCalendarReservationEvent(
                reservation.getId(),
                reservation.getUserName(),
                reservation.getUserPhone(),
                reservation.getServiceName(),
                reservation.getReservationStart(),
                reservation.getDurationMinutes());
    }
}
