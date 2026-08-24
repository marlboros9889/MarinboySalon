package com.marinboy.mapper;

import com.marinboy.dto.BusinessHourResponseDto;
import com.marinboy.dto.HolidayResponseDto;
import com.marinboy.dto.ReservationDto;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 예약 시간 계산에 필요한 영업시간·휴무일·하루 예약 SQL만 연결합니다. */
@Mapper
public interface ReservationScheduleMapper {
    List<ReservationDto> findActiveReservationsForDate(@Param("reservationDate") LocalDate reservationDate);

    int countHoliday(@Param("reservationDate") LocalDate reservationDate);
    List<HolidayResponseDto> findHolidays();
    int saveHoliday(@Param("holidayDate") LocalDate holidayDate, @Param("reason") String reason);
    int deleteHoliday(@Param("holidayDate") LocalDate holidayDate);

    List<BusinessHourResponseDto> findBusinessHours();
    BusinessHourResponseDto findBusinessHour(@Param("dayOfWeek") int dayOfWeek);
    int saveBusinessHour(
            @Param("dayOfWeek") int dayOfWeek,
            @Param("open") int open,
            @Param("openTime") String openTime,
            @Param("closeTime") String closeTime);
}
