package com.marinboy.businesshour.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.businesshour.dto.request.BusinessHourRequest;
import com.marinboy.businesshour.entity.BusinessHour;
import com.marinboy.businesshour.repository.BusinessHourMapper;

import lombok.RequiredArgsConstructor;

/**
 * 요일별 영업시간 조회와 수정을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BusinessHourServiceImpl implements BusinessHourService {

    private final BusinessHourMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<BusinessHour> getList() {
        return mapper.selectAll();
    }

    @Override
    public void update(BusinessHourRequest request) {
        if (!request.getClosed()) {
            if (request.getOpenTime() == null || request.getCloseTime() == null) {
                throw new IllegalArgumentException("영업일에는 시작과 종료 시간이 필요합니다.");
            }
            if (!request.getOpenTime().isBefore(request.getCloseTime())) {
                throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
            }
        }

        BusinessHour businessHour = new BusinessHour();
        businessHour.setId(request.getId());
        businessHour.setOpenTime(request.getOpenTime());
        businessHour.setCloseTime(request.getCloseTime());
        businessHour.setClosed(request.getClosed());
        mapper.update(businessHour);
    }
}
