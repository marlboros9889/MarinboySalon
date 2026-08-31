package com.marinboy.reservation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// 예약 상태별 고객 권한과 허용되는 상태 변경 규칙을 검증합니다.
class ReservationStatusTest {

    @Test
    void customerCanEditOnlyRequested() {
        assertTrue(ReservationStatus.REQUESTED.canCustomerEdit());
        assertFalse(ReservationStatus.CONFIRMED.canCustomerEdit());
        assertFalse(ReservationStatus.COMPLETED.canCustomerEdit());
    }

    @Test
    void completedCannotTransitionToCanceled() {
        assertThrows(IllegalArgumentException.class,
                () -> ReservationStatus.COMPLETED.assertTransitionTo(ReservationStatus.CANCELLED));
    }

    @Test
    void requestedCanConfirm() {
        ReservationStatus.REQUESTED.assertTransitionTo(ReservationStatus.CONFIRMED);
        assertEquals(ReservationStatus.CONFIRMED, ReservationStatus.from("confirmed"));
    }
}
