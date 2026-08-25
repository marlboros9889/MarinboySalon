package com.marinboy.holiday.dao;

import com.marinboy.holiday.dto.HolidayDto;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 휴무일 SQL을 HolidayMapper.xml과 연결합니다.
 */
@Mapper
public interface HolidayDao {

    List<HolidayDto> findAll();

    int countByDate(LocalDate holidayDate);

    int insert(HolidayDto holidayDto);

    int delete(Long id);
}
