package com.marinboy.holiday.service;

import java.util.List;

import com.marinboy.holiday.dto.request.HolidayRequest;
import com.marinboy.holiday.entity.Holiday;

// 관리자가 등록하는 임시 휴무일의 조회·등록·삭제 규칙을 정의합니다.
public interface HolidayService {

    List<Holiday> getList();

    Holiday insert(HolidayRequest request);

    void delete(Long id);
}
