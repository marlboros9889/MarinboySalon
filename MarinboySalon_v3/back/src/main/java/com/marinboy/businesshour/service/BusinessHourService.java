package com.marinboy.businesshour.service;

import java.util.List;

import com.marinboy.businesshour.dto.request.BusinessHourRequest;
import com.marinboy.businesshour.entity.BusinessHour;

public interface BusinessHourService {

    List<BusinessHour> getList();

    void update(BusinessHourRequest request);
}
