package com.marinboy.businesshour.repository;

import java.util.List;

import com.marinboy.businesshour.entity.BusinessHour;

public interface BusinessHourMapper {

    List<BusinessHour> selectAll();

    BusinessHour selectByDayOfWeek(Integer dayOfWeek);

    BusinessHour selectByDayOfWeekForUpdate(Integer dayOfWeek);

    int update(BusinessHour businessHour);
}
