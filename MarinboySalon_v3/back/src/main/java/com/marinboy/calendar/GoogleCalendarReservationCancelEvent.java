package com.marinboy.calendar;

import com.marinboy.reservation.entity.Reservation;

/** DB 취소가 확정된 뒤 외부 Google Calendar 일정을 정리하기 위한 이벤트입니다. */
public record GoogleCalendarReservationCancelEvent(
        Long reservationId,
        String calendarEventId) {

    public static GoogleCalendarReservationCancelEvent from(Reservation reservation) {
        return new GoogleCalendarReservationCancelEvent(
                reservation.getId(),
                reservation.getCalendarEventId());
    }
}
