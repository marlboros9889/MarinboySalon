package com.marinboy.holiday.repository;

import java.time.LocalDate;
import java.util.List;

import com.marinboy.holiday.entity.Holiday;

public interface HolidayMapper {

    List<Holiday> selectAll();

    Holiday selectByDate(LocalDate holidayDate);

    int insert(Holiday holiday);

    int delete(Long id);
}
