package com.marinboy.reservation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
                () -> ReservationStatus.COMPLETED.assertTransitionTo(ReservationStatus.CANCELED));
    }

    @Test
    void requestedCanConfirm() {
        ReservationStatus.REQUESTED.assertTransitionTo(ReservationStatus.CONFIRMED);
        assertEquals(ReservationStatus.CONFIRMED, ReservationStatus.from("confirmed"));
    }
}
