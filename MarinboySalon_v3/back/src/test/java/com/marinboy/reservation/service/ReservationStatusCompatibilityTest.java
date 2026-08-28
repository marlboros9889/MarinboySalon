package com.marinboy.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.marinboy.businesshour.repository.BusinessHourMapper;
import com.marinboy.holiday.repository.HolidayMapper;
import com.marinboy.reservation.entity.Reservation;
import com.marinboy.reservation.repository.ReservationMapper;
import com.marinboy.serviceitem.repository.ServiceItemMapper;

/** 과거 v3 취소 철자를 받아도 공용 DB 상태값으로 저장하고 응답하는지 확인합니다. */
@ExtendWith(MockitoExtension.class)
class ReservationStatusCompatibilityTest {

    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private ServiceItemMapper serviceItemMapper;
    @Mock
    private BusinessHourMapper businessHourMapper;
    @Mock
    private HolidayMapper holidayMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void blocksCanceledStatusChanges() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setReservationStart(LocalDateTime.of(2026, 8, 29, 10, 0));
        reservation.setDurationMinutes(30);
        reservation.setStatus("CANCELED");
        when(reservationMapper.selectById(1L)).thenReturn(reservation);

        assertThatThrownBy(() -> reservationService.updateStatus(1L, "CANCELED"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경할 수 없습니다");
    }
}
