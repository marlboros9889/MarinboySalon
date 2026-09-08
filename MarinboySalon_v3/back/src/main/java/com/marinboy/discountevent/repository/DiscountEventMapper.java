package com.marinboy.discountevent.repository;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.marinboy.discountevent.entity.DiscountEvent;

/** 기간 할인 이벤트 SQL과 연결되는 MyBatis Mapper입니다. */
public interface DiscountEventMapper {
    List<DiscountEvent> selectAll();
    DiscountEvent selectById(Long id);
    DiscountEvent selectActiveByDate(@Param("date") LocalDate date);
    int countOverlappingPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId);
    int insert(DiscountEvent event);
    int update(DiscountEvent event);
    int deleteById(Long id);
}
