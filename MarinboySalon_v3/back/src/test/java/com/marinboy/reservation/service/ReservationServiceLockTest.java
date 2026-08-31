package com.marinboy.reservation.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.marinboy.businesshour.entity.BusinessHour;
import com.marinboy.businesshour.repository.BusinessHourMapper;
import com.marinboy.holiday.repository.HolidayMapper;
import com.marinboy.reservation.dto.request.ReservationRequestDto;
import com.marinboy.reservation.entity.Reservation;
import com.marinboy.reservation.repository.ReservationMapper;
import com.marinboy.reservation.repository.ReservationSlotLockMapper;
import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.repository.ServiceItemMapper;

// 예약 겹침 검사 전에 시술에 포함된 슬롯만 잠가 동시 요청 순서를 지키는지 확인합니다.
@ExtendWith(MockitoExtension.class)
class ReservationServiceLockTest {

    @Mock private ReservationMapper reservationMapper;
    @Mock private ReservationSlotLockMapper reservationSlotLockMapper;
    @Mock private ServiceItemMapper serviceItemMapper;
    @Mock private BusinessHourMapper businessHourMapper;
    @Mock private HolidayMapper holidayMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void insertLocksOnlyRequestedSlotsBeforeOverlapCheck() {
        LocalDateTime start = LocalDate.now().plusDays(14).atTime(11, 0);
        ReservationRequestDto request = new ReservationRequestDto();
        request.setServiceId(1L);
        request.setReservationStart(start);

        ServiceItem item = new ServiceItem();
        item.setId(1L);
        item.setActive(true);
        item.setDurationMinutes(60);
        when(serviceItemMapper.selectById(1L)).thenReturn(item);

        BusinessHour businessHour = new BusinessHour();
        businessHour.setDayOfWeek(start.getDayOfWeek().getValue());
        businessHour.setOpenTime(LocalTime.of(10, 0));
        businessHour.setCloseTime(LocalTime.of(19, 0));
        businessHour.setClosed(false);
        when(businessHourMapper.selectByDayOfWeek(anyInt())).thenReturn(businessHour);
        when(holidayMapper.selectByDate(any())).thenReturn(null);
        when(reservationMapper.countOverlapForUpdate(any(), any(), isNull())).thenReturn(0);

        doAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(99L);
            return 1;
        }).when(reservationMapper).insert(any(Reservation.class));

        Reservation saved = new Reservation();
        saved.setId(99L);
        saved.setUserId(7L);
        saved.setServiceId(1L);
        saved.setReservationStart(start);
        saved.setDurationMinutes(30);
        saved.setStatus("REQUESTED");
        when(reservationMapper.selectById(99L)).thenReturn(saved);

        reservationService.insert(7L, request);

        InOrder order = inOrder(businessHourMapper, serviceItemMapper, holidayMapper, reservationSlotLockMapper, reservationMapper);
        order.verify(businessHourMapper).selectByDayOfWeek(start.getDayOfWeek().getValue());
        order.verify(serviceItemMapper).selectById(1L);
        order.verify(holidayMapper).selectByDate(start.toLocalDate());
        order.verify(reservationSlotLockMapper).lockSlot(start.toLocalDate(), LocalTime.of(11, 0));
        order.verify(reservationSlotLockMapper).lockSlot(start.toLocalDate(), LocalTime.of(11, 30));
        order.verify(reservationMapper).countOverlapForUpdate(start, start.plusMinutes(60), null);
        order.verify(reservationMapper).insert(any(Reservation.class));
    }
}
