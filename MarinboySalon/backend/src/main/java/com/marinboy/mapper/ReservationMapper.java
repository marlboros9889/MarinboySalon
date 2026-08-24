package com.marinboy.mapper;

import com.marinboy.dto.ReservationDto;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 고객 예약 저장·조회·상태 변경 SQL을 연결하는 MyBatis Mapper입니다. */
@Mapper
public interface ReservationMapper {
    // 같은 시간에 여러 요청이 들어와도 중복 예약이 저장되지 않도록 시술 행을 먼저 잠급니다.
    Long lockReservationSchedule(@Param("serviceId") Long serviceId);

    int countOverlappingReservation(
            @Param("serviceId") Long serviceId,
            @Param("reservationDateTime") LocalDateTime reservationDateTime);

    int countOverlappingReservationExcept(
            @Param("serviceId") Long serviceId,
            @Param("reservationDateTime") LocalDateTime reservationDateTime,
            @Param("reservationId") Long reservationId);

    // 검증이 끝난 고객·시술·시간 정보를 하나의 예약 행으로 저장합니다.
    void insertReservation(
            @Param("serviceId") Long serviceId,
            @Param("customerId") Long customerId,
            @Param("customerName") String customerName,
            @Param("customerEmail") String customerEmail,
            @Param("customerPhone") String customerPhone,
            @Param("reservationDateTime") LocalDateTime reservationDateTime,
            @Param("noShowPolicyAgreed") int noShowPolicyAgreed,
            @Param("memo") String memo);

    // 고객용 조회·수정·취소 SQL은 customerId 조건을 함께 사용해 다른 고객의 예약 접근을 막습니다.
    List<ReservationDto> findCustomerReservationsByCustomerId(@Param("customerId") Long customerId);
    ReservationDto findCustomerReservationByCustomerId(
            @Param("reservationId") Long reservationId,
            @Param("customerId") Long customerId);
    int updateCustomerReservationByCustomerId(
            @Param("reservationId") Long reservationId,
            @Param("customerId") Long customerId,
            @Param("serviceId") Long serviceId,
            @Param("reservationDateTime") LocalDateTime reservationDateTime,
            @Param("memo") String memo);
    int cancelCustomerReservationByCustomerId(
            @Param("reservationId") Long reservationId,
            @Param("customerId") Long customerId);

    // 관리자 목록은 전체 건수와 페이지 결과를 분리 조회해 화면의 페이지 이동을 계산합니다.
    int countReservations();
    List<ReservationDto> findReservationsPage(@Param("offset") int offset, @Param("size") int size);
    ReservationDto findReservationById(@Param("reservationId") Long reservationId);
    int updateReservationStatus(@Param("reservationId") Long reservationId, @Param("status") String status);
}
