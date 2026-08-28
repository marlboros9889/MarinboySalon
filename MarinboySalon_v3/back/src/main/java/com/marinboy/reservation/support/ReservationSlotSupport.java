package com.marinboy.reservation.support;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 가능 시간 조회와 예약 검증이 같은 30분 슬롯 규칙을 쓰도록 공통화합니다.
 */
public final class ReservationSlotSupport {

    public static final int SLOT_MINUTES = 30;

    private ReservationSlotSupport() {
    }

    public static void assertAlignedSlot(LocalDateTime start) {
        if (start == null) {
            throw new IllegalArgumentException("예약 시간이 필요합니다.");
        }
        if (start.getMinute() % SLOT_MINUTES != 0 || start.getSecond() != 0 || start.getNano() != 0) {
            throw new IllegalArgumentException("예약 시간은 30분 단위로 선택해 주세요.");
        }
    }

    public static LocalDateTime alignToNextSlot(LocalDateTime candidate) {
        int minuteRemainder = candidate.getMinute() % SLOT_MINUTES;
        if (minuteRemainder != 0) {
            candidate = candidate.plusMinutes(SLOT_MINUTES - minuteRemainder);
        }
        return candidate.withSecond(0).withNano(0);
    }

    public static LocalDateTime nextSlot(LocalDateTime candidate) {
        return candidate.plusMinutes(SLOT_MINUTES);
    }

    public static boolean fitsWithinBusinessHours(LocalTime open, LocalTime close,
                                                  LocalTime startTime, LocalTime endTime) {
        if (open == null || close == null || startTime == null || endTime == null) {
            return false;
        }
        return !startTime.isBefore(open) && !endTime.isAfter(close);
    }
}
