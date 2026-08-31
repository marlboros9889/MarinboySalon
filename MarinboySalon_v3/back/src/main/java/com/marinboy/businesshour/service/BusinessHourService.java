package com.marinboy.businesshour.service;

import java.util.List;

import com.marinboy.businesshour.dto.request.BusinessHourRequest;
import com.marinboy.businesshour.entity.BusinessHour;

// 주간 영업시간을 조회하고 관리자가 변경하는 업무 규칙을 정의합니다.
public interface BusinessHourService {

    List<BusinessHour> getList();

    void update(BusinessHourRequest request);
}
