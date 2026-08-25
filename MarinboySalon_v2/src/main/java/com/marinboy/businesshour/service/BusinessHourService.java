package com.marinboy.businesshour.service;

import com.marinboy.businesshour.dto.BusinessHourDto;

import java.util.List;

/**
 * 영업시간 기능에서 Controller가 사용할 작업 목록입니다.
 */
public interface BusinessHourService {

    List<BusinessHourDto> getList();

    void update(BusinessHourDto businessHourDto);
}
