package com.marinboy.holiday.repository;

import java.time.LocalDate;
import java.util.List;

import com.marinboy.holiday.entity.Holiday;

// 예약 가능 여부 판단에 사용할 임시 휴무일 데이터를 조회·변경합니다.
public interface HolidayMapper {

    List<Holiday> selectAll();

    Holiday selectByDate(LocalDate holidayDate);

    int insert(Holiday holiday);

    int delete(Long id);
}
