package com.marinboy.holiday.service;

import com.marinboy.holiday.dto.HolidayDto;

import java.util.List;

/**
 * 휴무일 기능에서 Controller가 사용할 작업 목록입니다.
 */
public interface HolidayService {

    List<HolidayDto> getList();

    void insert(HolidayDto holidayDto);

    void delete(Long id);
}
