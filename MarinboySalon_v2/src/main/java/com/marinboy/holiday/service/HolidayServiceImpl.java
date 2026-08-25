package com.marinboy.holiday.service;

import com.marinboy.holiday.dao.HolidayDao;
import com.marinboy.holiday.dto.HolidayDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 임시 휴무일 조회, 등록, 삭제의 실제 처리 순서를 담당합니다.
 */
@Service
public class HolidayServiceImpl implements HolidayService {

    private final HolidayDao holidayDao;

    public HolidayServiceImpl(HolidayDao holidayDao) {
        this.holidayDao = holidayDao;
    }

    @Override
    public List<HolidayDto> getList() {
        return holidayDao.findAll();
    }

    @Override
    @Transactional
    public void insert(HolidayDto holidayDto) {
        if (holidayDto.getHolidayDate() == null || holidayDto.getHolidayDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("오늘 이후의 휴무일을 선택해 주세요.");
        }

        int holidayCount = holidayDao.countByDate(holidayDto.getHolidayDate());
        if (holidayCount > 0) {
            throw new IllegalArgumentException("이미 등록된 휴무일입니다.");
        }

        holidayDao.insert(holidayDto);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        int deletedCount = holidayDao.delete(id);

        if (deletedCount == 0) {
            throw new IllegalArgumentException("삭제할 휴무일을 찾을 수 없습니다.");
        }
    }
}
