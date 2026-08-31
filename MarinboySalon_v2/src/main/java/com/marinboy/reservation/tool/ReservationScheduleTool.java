package com.marinboy.reservation.tool;

import com.marinboy.businesshour.dao.BusinessHourDao;
import com.marinboy.businesshour.dto.BusinessHourDto;
import com.marinboy.holiday.dao.HolidayDao;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 예약 시간이 영업시간과 휴무일 규칙에 맞는지 한 곳에서 검사합니다.
 */
@Component
public class ReservationScheduleTool {

    private final BusinessHourDao businessHourDao;
    private final HolidayDao holidayDao;

    public ReservationScheduleTool(BusinessHourDao businessHourDao, HolidayDao holidayDao) {
        this.businessHourDao = businessHourDao;
        this.holidayDao = holidayDao;
    }

    /**
     * 예약 시작부터 시술 종료까지 매장이 실제 영업 중인지 확인합니다.
     */
    public void validateAvailableTime(LocalDateTime reservationStart, LocalDateTime reservationEnd) {
        boolean invalidMinute = reservationStart.getMinute() % 30 != 0;
        boolean hasSeconds = reservationStart.getSecond() != 0 || reservationStart.getNano() != 0;
        if (invalidMinute || hasSeconds) {
            throw new IllegalArgumentException("예약 시간은 30분 단위로 선택해 주세요.");
        }

        int holidayCount = holidayDao.countByDate(reservationStart.toLocalDate());
        if (holidayCount > 0) {
            throw new IllegalArgumentException("선택한 날짜는 휴무일입니다.");
        }

        int dayOfWeek = reservationStart.getDayOfWeek().getValue();
        // 트랜잭션이 끝날 때까지 요일 행을 잠가 겹침 조회와 저장을 하나씩 처리합니다.
        BusinessHourDto businessHourDto = businessHourDao.findByDayOfWeekForUpdate(dayOfWeek);

        if (businessHourDto == null || businessHourDto.isClosed()) {
            throw new IllegalArgumentException("선택한 요일은 정기 휴무일입니다.");
        }

        LocalTime reservationStartTime = reservationStart.toLocalTime();
        LocalTime reservationEndTime = reservationEnd.toLocalTime();
        boolean startsBeforeOpen = reservationStartTime.isBefore(businessHourDto.getOpenTime());
        boolean endsAfterClose = reservationEndTime.isAfter(businessHourDto.getCloseTime());

        if (startsBeforeOpen || endsAfterClose) {
            throw new IllegalArgumentException("영업시간 안에서 시술이 끝나는 시간을 선택해 주세요.");
        }
    }
}
