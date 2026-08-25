package com.marinboy.holiday.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.holiday.dto.request.HolidayRequest;
import com.marinboy.holiday.entity.Holiday;
import com.marinboy.holiday.repository.HolidayMapper;

import lombok.RequiredArgsConstructor;

/**
 * 임시 휴무일 등록과 삭제를 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<Holiday> getList() {
        return mapper.selectAll();
    }

    @Override
    public Holiday insert(HolidayRequest request) {
        if (mapper.selectByDate(request.getHolidayDate()) != null) {
            throw new IllegalArgumentException("이미 등록된 휴무일입니다.");
        }
        Holiday holiday = new Holiday();
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setReason(request.getReason());
        mapper.insert(holiday);
        return mapper.selectByDate(request.getHolidayDate());
    }

    @Override
    public void delete(Long id) {
        mapper.delete(id);
    }
}
