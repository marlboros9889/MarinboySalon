package com.marinboy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.marinboy.dto.ReservationDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 예약 계산 도구는 Spring·DB 없이 시간 구간 규칙만 빠르게 검증합니다. */
class ReservationScheduleToolTest {
    private final ReservationScheduleTool tool = new ReservationScheduleTool();

    @Test
    void 기존예약과겹치는후보시간을제외한다() {
        LocalDate date = LocalDate.of(2026, 8, 25);
        ReservationDto existing = reservation(date.atTime(11, 0), 60);

        List<LocalDateTime> slots = tool.createAvailableSlots(
                date, LocalTime.of(10, 0), LocalTime.of(12, 0), 60,
                date.atTime(9, 0), List.of(existing));

        assertEquals(List.of(date.atTime(10, 0)), slots);
    }

    @Test
    void 최소예약시간보다이른후보를제외한다() {
        LocalDate date = LocalDate.of(2026, 8, 25);

        List<LocalDateTime> slots = tool.createAvailableSlots(
                date, LocalTime.of(10, 0), LocalTime.of(11, 0), 30,
                date.atTime(10, 30), List.of());

        assertEquals(List.of(date.atTime(10, 30)), slots);
    }

    private ReservationDto reservation(LocalDateTime startTime, int durationMinutes) {
        ReservationDto reservation = new ReservationDto();
        reservation.setReservationDateTime(startTime);
        reservation.setDurationMinutes(durationMinutes);
        return reservation;
    }
}
