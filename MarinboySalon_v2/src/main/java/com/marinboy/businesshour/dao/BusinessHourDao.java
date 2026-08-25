package com.marinboy.businesshour.dao;

import com.marinboy.businesshour.dto.BusinessHourDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 영업시간 SQL을 BusinessHourMapper.xml과 연결합니다.
 */
@Mapper
public interface BusinessHourDao {

    List<BusinessHourDto> findAll();

    BusinessHourDto findByDayOfWeek(int dayOfWeek);

    int update(BusinessHourDto businessHourDto);
}
