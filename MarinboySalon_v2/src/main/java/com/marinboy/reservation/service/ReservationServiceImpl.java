package com.marinboy.reservation.service;

import com.marinboy.reservation.dao.ReservationDao;
import com.marinboy.reservation.dto.ReservationDto;
import com.marinboy.serviceitem.dao.ServiceItemDao;
import com.marinboy.serviceitem.dto.ServiceItemDto;
import com.marinboy.reservation.tool.ReservationScheduleTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 시간 검사와 등록, 수정, 취소의 실제 처리 순서를 담당합니다.
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationDao reservationDao;
    private final ServiceItemDao serviceItemDao;
    private final ReservationScheduleTool reservationScheduleTool;

    public ReservationServiceImpl(ReservationDao reservationDao,
                                  ServiceItemDao serviceItemDao,
                                  ReservationScheduleTool reservationScheduleTool) {
        this.reservationDao = reservationDao;
        this.serviceItemDao = serviceItemDao;
        this.reservationScheduleTool = reservationScheduleTool;
    }

    /**
     * 시술 시간과 기존 예약을 확인한 뒤 접수 상태로 저장합니다.
     */
    @Override
    @Transactional
    public void insert(ReservationDto reservationDto) {
        validateReservationTime(reservationDto, null);
        reservationDto.setStatus("REQUESTED");
        reservationDao.insert(reservationDto);
    }

    @Override
    public List<ReservationDto> getMyList(Long userId) {
        return reservationDao.findByUserId(userId);
    }

    @Override
    public List<ReservationDto> getList() {
        return reservationDao.findAll();
    }

    @Override
    public ReservationDto getMyReservation(Long id, Long userId) {
        ReservationDto reservationDto = reservationDao.findByIdAndUserId(id, userId);

        if (reservationDto == null) {
            throw new IllegalArgumentException("본인의 예약만 확인할 수 있습니다.");
        }

        return reservationDto;
    }

    /**
     * 수정할 예약을 먼저 소유권 확인한 뒤 시간 중복을 다시 검사합니다.
     */
    @Override
    @Transactional
    public void update(ReservationDto reservationDto) {
        getMyReservation(reservationDto.getId(), reservationDto.getUserId());
        validateReservationTime(reservationDto, reservationDto.getId());

        int updatedCount = reservationDao.update(reservationDto);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("접수 상태의 예약만 수정할 수 있습니다.");
        }
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId) {
        int updatedCount = reservationDao.cancelByIdAndUserId(id, userId);

        if (updatedCount == 0) {
            throw new IllegalArgumentException("접수 상태의 본인 예약만 취소할 수 있습니다.");
        }
    }

    /**
     * 관리자가 변경할 수 있는 예약 상태만 명시적으로 허용합니다.
     */
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        boolean validStatus = "REQUESTED".equals(status)
                || "CONFIRMED".equals(status)
                || "COMPLETED".equals(status)
                || "CANCELLED".equals(status);

        if (!validStatus) {
            throw new IllegalArgumentException("허용되지 않은 예약 상태입니다.");
        }

        int updatedCount = reservationDao.updateStatus(id, status);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("상태를 변경할 예약을 찾을 수 없습니다.");
        }
    }

    /**
     * 새 예약 시작 시간부터 시술 종료 시간까지 다른 예약과 겹치는지 검사합니다.
     */
    private void validateReservationTime(ReservationDto reservationDto, Long excludeId) {
        LocalDateTime reservationStart = reservationDto.getReservationStart();

        if (reservationStart == null || !reservationStart.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("현재 시간 이후의 예약 시간을 선택해 주세요.");
        }

        ServiceItemDto serviceItemDto = serviceItemDao.findById(reservationDto.getServiceId());
        if (serviceItemDto == null || !serviceItemDto.isActive()) {
            throw new IllegalArgumentException("예약할 수 없는 시술입니다.");
        }

        LocalDateTime reservationEnd = reservationStart.plusMinutes(serviceItemDto.getDurationMinutes());
        reservationScheduleTool.validateAvailableTime(reservationStart, reservationEnd);
        int conflictCount = reservationDao.countTimeConflict(reservationStart, reservationEnd, excludeId);

        if (conflictCount > 0) {
            throw new IllegalArgumentException("이미 다른 예약이 있는 시간입니다.");
        }
    }
}
