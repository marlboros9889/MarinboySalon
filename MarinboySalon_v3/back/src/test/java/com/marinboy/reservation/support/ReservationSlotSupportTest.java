package com.marinboy.reservation.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class ReservationSlotSupportTest {

    @Test
    void rejectsUnalignedMinutes() {
        LocalDateTime bad = LocalDateTime.of(2026, 8, 28, 10, 15, 0);
        assertThrows(IllegalArgumentException.class, () -> ReservationSlotSupport.assertAlignedSlot(bad));
    }

    @Test
    void acceptsThirtyMinuteSlot() {
        LocalDateTime ok = LocalDateTime.of(2026, 8, 28, 10, 30, 0);
        assertDoesNotThrow(() -> ReservationSlotSupport.assertAlignedSlot(ok));
    }

    @Test
    void alignsToNextSlot() {
        LocalDateTime candidate = LocalDateTime.of(2026, 8, 28, 9, 10, 0);
        LocalDateTime aligned = ReservationSlotSupport.alignToNextSlot(candidate);
        assertEquals(30, aligned.getMinute());
        assertEquals(0, aligned.getSecond());
    }

    @Test
    void businessHourFit() {
        assertTrue(ReservationSlotSupport.fitsWithinBusinessHours(
                LocalTime.of(10, 0), LocalTime.of(19, 0),
                LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }
}
