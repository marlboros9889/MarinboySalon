package com.marinboy.reservation.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 예약 상태와 허용 전이를 한 곳에서 관리합니다.
 */
public enum ReservationStatus {
    REQUESTED,
    CONFIRMED,
    COMPLETED,
    CANCELED;

    private static final Map<ReservationStatus, Set<ReservationStatus>> ALLOWED_TRANSITIONS = Map.of(
            REQUESTED, EnumSet.of(CONFIRMED, CANCELED),
            CONFIRMED, EnumSet.of(COMPLETED, CANCELED),
            COMPLETED, EnumSet.noneOf(ReservationStatus.class),
            CANCELED, EnumSet.noneOf(ReservationStatus.class)
    );

    public static ReservationStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("예약 상태가 비어 있습니다.");
        }
        try {
            return ReservationStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("사용할 수 없는 예약 상태입니다: " + value);
        }
    }

    public boolean canCustomerEdit() {
        return this == REQUESTED;
    }

    public boolean canCustomerCancel() {
        return this == REQUESTED || this == CONFIRMED;
    }

    public boolean canTransitionTo(ReservationStatus next) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(ReservationStatus.class)).contains(next);
    }

    public void assertTransitionTo(ReservationStatus next) {
        if (!canTransitionTo(next)) {
            throw new IllegalArgumentException(
                    "예약 상태를 " + this.name() + "에서 " + next.name() + "(으)로 변경할 수 없습니다.");
        }
    }
}
