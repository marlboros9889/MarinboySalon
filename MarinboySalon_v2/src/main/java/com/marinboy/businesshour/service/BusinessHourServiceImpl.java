package com.marinboy.businesshour.service;

import com.marinboy.businesshour.dao.BusinessHourDao;
import com.marinboy.businesshour.dto.BusinessHourDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 요일별 영업시간 조회와 수정의 실제 처리 순서를 담당합니다.
 */
@Service
public class BusinessHourServiceImpl implements BusinessHourService {

    private final BusinessHourDao businessHourDao;

    public BusinessHourServiceImpl(BusinessHourDao businessHourDao) {
        this.businessHourDao = businessHourDao;
    }

    @Override
    public List<BusinessHourDto> getList() {
        return businessHourDao.findAll();
    }

    /**
     * 영업일에는 시작 시간과 종료 시간이 올바른 순서인지 확인합니다.
     */
    @Override
    @Transactional
    public void update(BusinessHourDto businessHourDto) {
        if (!businessHourDto.isClosed()) {
            if (businessHourDto.getOpenTime() == null || businessHourDto.getCloseTime() == null) {
                throw new IllegalArgumentException("영업일의 시작 시간과 종료 시간을 입력해 주세요.");
            }

            if (!businessHourDto.getOpenTime().isBefore(businessHourDto.getCloseTime())) {
                throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
            }
        }

        int updatedCount = businessHourDao.update(businessHourDto);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("수정할 영업시간을 찾을 수 없습니다.");
        }
    }
}
