package com.marinboy.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.marinboy.reservation.entity.Reservation;

public interface ReservationMapper {

    List<Reservation> selectMyList(Long userId);

    List<Reservation> selectAll();

    Reservation selectById(Long id);

    int insert(Reservation reservation);

    int update(Reservation reservation);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int countOverlap(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") Long excludeId);
}
