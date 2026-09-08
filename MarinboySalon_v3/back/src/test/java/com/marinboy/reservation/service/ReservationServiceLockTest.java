package com.marinboy.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.marinboy.businesshour.entity.BusinessHour;
import com.marinboy.businesshour.repository.BusinessHourMapper;
import com.marinboy.calendar.GoogleCalendarReservationCancelEvent;
import com.marinboy.calendar.GoogleCalendarReservationEvent;
import com.marinboy.holiday.repository.HolidayMapper;
import com.marinboy.reservation.dto.request.ReservationRequestDto;
import com.marinboy.reservation.entity.Reservation;
import com.marinboy.reservation.repository.ReservationMapper;
import com.marinboy.reservation.repository.ReservationSlotLockMapper;
import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.repository.ServiceItemMapper;
import com.marinboy.discountevent.service.DiscountEventService;
import com.marinboy.discountevent.entity.DiscountEvent;
import java.math.BigDecimal;

// 예약 겹침 검사 전에 시술에 포함된 슬롯만 잠가 동시 요청 순서를 지키는지 확인합니다.
@ExtendWith(MockitoExtension.class)
class ReservationServiceLockTest {

    @Mock private ReservationMapper reservationMapper;
    @Mock private ReservationSlotLockMapper reservationSlotLockMapper;
    @Mock private ServiceItemMapper serviceItemMapper;
    @Mock private BusinessHourMapper businessHourMapper;
    @Mock private HolidayMapper holidayMapper;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private DiscountEventService discountEventService;

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
        item.setPrice(30000);
        when(serviceItemMapper.selectById(1L)).thenReturn(item);
        DiscountEvent discountEvent = new DiscountEvent();
        discountEvent.setId(3L);
        discountEvent.setDiscountRate(new BigDecimal("15.00"));
        when(discountEventService.findActiveEvent(any())).thenReturn(discountEvent);

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

        ArgumentCaptor<Reservation> savedReservation = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationMapper).insert(savedReservation.capture());
        assertThat(savedReservation.getValue().getOriginalPrice()).isEqualTo(30000);
        assertThat(savedReservation.getValue().getDiscountAmount()).isEqualTo(4500);
        assertThat(savedReservation.getValue().getFinalPrice()).isEqualTo(25500);
        assertThat(savedReservation.getValue().getDiscountEventId()).isEqualTo(3L);

        InOrder order = inOrder(businessHourMapper, serviceItemMapper, holidayMapper, reservationSlotLockMapper, reservationMapper);
        order.verify(businessHourMapper).selectByDayOfWeek(start.getDayOfWeek().getValue());
        order.verify(serviceItemMapper).selectById(1L);
        order.verify(holidayMapper).selectByDate(start.toLocalDate());
        order.verify(reservationSlotLockMapper).lockSlot(start.toLocalDate(), LocalTime.of(11, 0));
        order.verify(reservationSlotLockMapper).lockSlot(start.toLocalDate(), LocalTime.of(11, 30));
        order.verify(reservationMapper).countOverlapForUpdate(start, start.plusMinutes(60), null);
        order.verify(reservationMapper).insert(any(Reservation.class));
    }

    @Test
    void updatePublishesCalendarDeleteAndCreateEvents() {
        LocalDateTime oldStart = LocalDate.now().plusDays(14).atTime(11, 0);
        LocalDateTime newStart = oldStart.plusHours(1);
        Reservation reservation = new Reservation();
        reservation.setId(11L);
        reservation.setUserId(7L);
        reservation.setServiceId(1L);
        reservation.setReservationStart(oldStart);
        reservation.setStatus("REQUESTED");
        reservation.setCalendarEventId("old-calendar-event");
        reservation.setUserName("테스트 고객");
        reservation.setUserPhone("010-0000-0000");
        reservation.setServiceName("테스트 시술");
        reservation.setDurationMinutes(60);
        when(reservationMapper.selectById(11L)).thenReturn(reservation);

        ServiceItem item = new ServiceItem();
        item.setId(1L);
        item.setActive(true);
        item.setDurationMinutes(60);
        item.setPrice(30000);
        when(serviceItemMapper.selectById(1L)).thenReturn(item);
        when(discountEventService.findActiveEvent(any())).thenReturn(null);
        when(discountEventService.findActiveEvent(any())).thenReturn(null);

        BusinessHour businessHour = new BusinessHour();
        businessHour.setOpenTime(LocalTime.of(10, 0));
        businessHour.setCloseTime(LocalTime.of(19, 0));
        businessHour.setClosed(false);
        when(businessHourMapper.selectByDayOfWeek(anyInt())).thenReturn(businessHour);
        when(holidayMapper.selectByDate(any())).thenReturn(null);
        when(reservationMapper.countOverlapForUpdate(any(), any(), any())).thenReturn(0);

        ReservationRequestDto request = new ReservationRequestDto();
        request.setServiceId(1L);
        request.setReservationStart(newStart);

        reservationService.update(11L, 7L, request);

        ArgumentCaptor<Object> events = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, org.mockito.Mockito.times(2)).publishEvent(events.capture());
        assertThat(events.getAllValues().get(0)).isInstanceOf(GoogleCalendarReservationCancelEvent.class);
        assertThat(events.getAllValues().get(1)).isInstanceOf(GoogleCalendarReservationEvent.class);
    }
}
