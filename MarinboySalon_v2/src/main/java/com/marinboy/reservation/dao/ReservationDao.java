package com.marinboy.reservation.dao;

import com.marinboy.reservation.dto.ReservationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 SQL을 ReservationMapper.xml과 연결합니다.
 */
@Mapper
public interface ReservationDao {

    int insert(ReservationDto reservationDto);

    List<ReservationDto> findByUserId(Long userId);

    List<ReservationDto> findAll();

    ReservationDto findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int countTimeConflict(@Param("reservationStart") LocalDateTime reservationStart,
                          @Param("reservationEnd") LocalDateTime reservationEnd,
                          @Param("excludeId") Long excludeId);

    int update(ReservationDto reservationDto);

    int cancelByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
