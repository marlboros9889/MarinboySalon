package com.marinboy.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.marinboy.businesshour.entity.BusinessHour;
import com.marinboy.businesshour.repository.BusinessHourMapper;
import com.marinboy.holiday.repository.HolidayMapper;
import com.marinboy.reservation.repository.ReservationMapper;
import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.repository.ServiceItemMapper;

/** 고객 화면에 30분 간격의 빈 시작 시각만 제공하는지 확인합니다. */
@ExtendWith(MockitoExtension.class)
class ReservationAvailableTimeTest {

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
    void returnsOnlyAvailableThirtyMinuteSlots() {
        LocalDate date = LocalDate.now().plusDays(14);
        BusinessHour businessHour = new BusinessHour();
        businessHour.setOpenTime(LocalTime.of(10, 0));
        businessHour.setCloseTime(LocalTime.of(12, 0));
        businessHour.setClosed(false);
        when(businessHourMapper.selectByDayOfWeek(date.getDayOfWeek().getValue())).thenReturn(businessHour);

        ServiceItem item = new ServiceItem();
        item.setActive(true);
        item.setDurationMinutes(60);
        when(serviceItemMapper.selectById(1L)).thenReturn(item);
        when(holidayMapper.selectByDate(date)).thenReturn(null);
        when(reservationMapper.countOverlap(any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
                .thenAnswer(invocation -> {
                    LocalDateTime start = invocation.getArgument(0);
                    return start.toLocalTime().equals(LocalTime.of(10, 30)) ? 1 : 0;
                });

        List<String> times = reservationService.getAvailableTimes(date, 1L);

        assertThat(times).containsExactly("10:00", "11:00");
    }
}
