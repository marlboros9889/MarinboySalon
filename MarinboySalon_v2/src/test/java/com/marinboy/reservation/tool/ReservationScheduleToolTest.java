package com.marinboy.reservation.tool;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marinboy.businesshour.dao.BusinessHourDao;
import com.marinboy.businesshour.dto.BusinessHourDto;
import com.marinboy.holiday.dao.HolidayDao;

/** v2도 v3와 같은 30분 슬롯과 요일 행 잠금을 사용하는지 확인합니다. */
@ExtendWith(MockitoExtension.class)
class ReservationScheduleToolTest {

    @Mock
    private BusinessHourDao businessHourDao;
    @Mock
    private HolidayDao holidayDao;
    @InjectMocks
    private ReservationScheduleTool reservationScheduleTool;

    @Test
    void rejectsTimeOutsideThirtyMinuteSlots() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 7, 14, 4);

        assertThatThrownBy(() -> reservationScheduleTool.validateAvailableTime(
                start, start.plusMinutes(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("30분 단위");
    }

    @Test
    void locksBusinessHourRowBeforeCheckingSchedule() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 7, 14, 0);
        BusinessHourDto businessHour = new BusinessHourDto();
        businessHour.setOpenTime(LocalTime.of(9, 0));
        businessHour.setCloseTime(LocalTime.of(18, 0));
        businessHour.setClosed(false);
        when(holidayDao.countByDate(start.toLocalDate())).thenReturn(0);
        when(businessHourDao.findByDayOfWeekForUpdate(start.getDayOfWeek().getValue()))
                .thenReturn(businessHour);

        reservationScheduleTool.validateAvailableTime(start, start.plusMinutes(30));

        verify(businessHourDao).findByDayOfWeekForUpdate(start.getDayOfWeek().getValue());
    }
}
