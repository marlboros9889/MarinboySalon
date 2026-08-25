package com.marinboy.holiday.service;

import java.util.List;

import com.marinboy.holiday.dto.request.HolidayRequest;
import com.marinboy.holiday.entity.Holiday;

public interface HolidayService {

    List<Holiday> getList();

    Holiday insert(HolidayRequest request);

    void delete(Long id);
}
