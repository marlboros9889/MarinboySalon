package com.marinboy.service;

import com.marinboy.dto.ReservationDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 영업시간과 기존 예약을 받아 DB 없이 예약 가능 시간을 계산하는 기능 부품입니다. */
@Component
public class ReservationScheduleTool {
    private static final int SLOT_INTERVAL_MINUTES = 30;

    public List<LocalDateTime> createAvailableSlots(
            LocalDate date,
            LocalTime openTime,
            LocalTime closeTime,
            int durationMinutes,
            LocalDateTime minimumTime,
            List<ReservationDto> existingReservations) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalTime cursor = openTime;

        while (!cursor.plusMinutes(durationMinutes).isAfter(closeTime)) {
            LocalDateTime candidateStart = LocalDateTime.of(date, cursor);
            LocalDateTime candidateEnd = candidateStart.plusMinutes(durationMinutes);
            boolean afterMinimumTime = !candidateStart.isBefore(minimumTime);
            boolean overlaps = overlapsExistingReservation(candidateStart, candidateEnd, existingReservations);
            if (afterMinimumTime && !overlaps) {
                slots.add(candidateStart);
            }
            cursor = cursor.plusMinutes(SLOT_INTERVAL_MINUTES);
        }
        return slots;
    }

    /** 두 시간 구간은 기존 시작이 새 종료보다 빠르고 기존 종료가 새 시작보다 늦을 때 겹칩니다. */
    private boolean overlapsExistingReservation(
            LocalDateTime candidateStart,
            LocalDateTime candidateEnd,
            List<ReservationDto> existingReservations) {
        for (ReservationDto reservation : existingReservations) {
            LocalDateTime existingStart = reservation.getReservationDateTime();
            Integer existingDuration = reservation.getDurationMinutes();
            if (existingStart == null || existingDuration == null) {
                continue;
            }
            LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);
            if (existingStart.isBefore(candidateEnd) && existingEnd.isAfter(candidateStart)) {
                return true;
            }
        }
        return false;
    }
}
