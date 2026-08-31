package com.marinboy.businesshour.repository;

import java.util.List;

import com.marinboy.businesshour.entity.BusinessHour;

// 영업시간 행을 조회하고, 예약 생성 시 같은 요일 행을 잠글 수 있게 합니다.
public interface BusinessHourMapper {

    List<BusinessHour> selectAll();

    BusinessHour selectByDayOfWeek(Integer dayOfWeek);

    // 같은 요일 예약을 직렬화하기 위해 트랜잭션 안에서 행 잠금을 획득합니다.
    BusinessHour selectByDayOfWeekForUpdate(Integer dayOfWeek);

    int update(BusinessHour businessHour);
}
