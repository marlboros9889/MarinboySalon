package com.marinboy.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.ibatis.annotations.Param;

/**
 * 예약 시간대별 행 잠금을 획득합니다.
 * 같은 날짜와 30분 슬롯을 요청한 트랜잭션만 서로 대기하도록 만듭니다.
 */
public interface ReservationSlotLockMapper {

    int lockSlot(
            @Param("reservationDate") LocalDate reservationDate,
            @Param("slotTime") LocalTime slotTime);
}
