package com.marinboy.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.businesshour.entity.BusinessHour;
import com.marinboy.businesshour.repository.BusinessHourMapper;
import com.marinboy.calendar.GoogleCalendarReservationEvent;
import com.marinboy.holiday.repository.HolidayMapper;
import com.marinboy.reservation.domain.ReservationStatus;
import com.marinboy.reservation.dto.request.ReservationRequestDto;
import com.marinboy.reservation.dto.response.ReservationResponseDto;
import com.marinboy.reservation.entity.Reservation;
import com.marinboy.reservation.repository.ReservationMapper;
import com.marinboy.reservation.support.ReservationSlotSupport;
import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.repository.ServiceItemMapper;

import lombok.RequiredArgsConstructor;

/**
 * 예약 소유권, 영업시간, 휴무일, 시간 중복을 한 곳에서 검사합니다.
 * 가능 시간 조회와 등록 검증은 ReservationSlotSupport 규칙을 공유합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationMapper reservationMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final BusinessHourMapper businessHourMapper;
    private final HolidayMapper holidayMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getMyList(Long userId) {
        return toResponseList(reservationMapper.selectMyList(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getAdminList() {
        return toResponseList(reservationMapper.selectAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableTimes(LocalDate date, Long serviceId) {
        List<String> availableTimes = new ArrayList<>();
        if (date == null || date.isBefore(LocalDate.now())) {
            return availableTimes;
        }

        BusinessHour businessHour = businessHourMapper.selectByDayOfWeek(date.getDayOfWeek().getValue());
        ServiceItem item = serviceItemMapper.selectById(serviceId);
        if (businessHour == null || Boolean.TRUE.equals(businessHour.getClosed())
                || item == null || !Boolean.TRUE.equals(item.getActive())
                || holidayMapper.selectByDate(date) != null) {
            return availableTimes;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime candidate = ReservationSlotSupport.alignToNextSlot(
                date.atTime(businessHour.getOpenTime()));
        LocalDateTime closingTime = date.atTime(businessHour.getCloseTime());

        while (!candidate.plusMinutes(item.getDurationMinutes()).isAfter(closingTime)) {
            if (candidate.isAfter(now)
                    && reservationMapper.countOverlap(
                            candidate, candidate.plusMinutes(item.getDurationMinutes()), null) == 0) {
                availableTimes.add(candidate.toLocalTime().toString());
            }
            candidate = ReservationSlotSupport.nextSlot(candidate);
        }
        return availableTimes;
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDto getDetail(Long id, Long userId, boolean admin) {
        Reservation reservation = getOwnedReservation(id, userId, admin);
        return ReservationResponseDto.from(reservation);
    }

    @Override
    public ReservationResponseDto insert(Long userId, ReservationRequestDto request) {
        validateSchedule(request, null);

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setServiceId(request.getServiceId());
        reservation.setReservationStart(request.getReservationStart());
        reservation.setRequestMemo(request.getRequestMemo());
        reservation.setStatus(ReservationStatus.REQUESTED.name());
        reservationMapper.insert(reservation);
        Reservation savedReservation = reservationMapper.selectById(reservation.getId());
        eventPublisher.publishEvent(GoogleCalendarReservationEvent.from(savedReservation));
        log.info("Reservation created id={} userId={} start={}",
                savedReservation.getId(), userId, savedReservation.getReservationStart());
        return ReservationResponseDto.from(savedReservation);
    }

    @Override
    public ReservationResponseDto update(Long id, Long userId, ReservationRequestDto request) {
        Reservation reservation = getOwnedReservation(id, userId, false);
        ReservationStatus current = ReservationStatus.from(reservation.getStatus());
        if (!current.canCustomerEdit()) {
            throw new IllegalArgumentException("접수 상태의 예약만 변경할 수 있습니다.");
        }

        validateSchedule(request, id);
        reservation.setServiceId(request.getServiceId());
        reservation.setReservationStart(request.getReservationStart());
        reservation.setRequestMemo(request.getRequestMemo());
        reservationMapper.update(reservation);
        log.info("Reservation updated id={} userId={}", id, userId);
        return ReservationResponseDto.from(reservationMapper.selectById(id));
    }

    @Override
    public void cancel(Long id, Long userId, boolean admin) {
        Reservation reservation = getOwnedReservation(id, userId, admin);
        ReservationStatus current = ReservationStatus.from(reservation.getStatus());
        if (admin) {
            current.assertTransitionTo(ReservationStatus.CANCELED);
        } else if (!current.canCustomerCancel()) {
            throw new IllegalArgumentException("완료된 예약은 취소할 수 없습니다.");
        }
        reservationMapper.updateStatus(id, ReservationStatus.CANCELED.name());
        log.info("Reservation canceled id={} byAdmin={}", id, admin);
    }

    @Override
    public ReservationResponseDto updateStatus(Long id, String status) {
        ReservationStatus next = ReservationStatus.from(status);
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
        ReservationStatus current = ReservationStatus.from(reservation.getStatus());
        current.assertTransitionTo(next);
        reservationMapper.updateStatus(id, next.name());
        log.info("Reservation status changed id={} {} -> {}", id, current, next);
        return ReservationResponseDto.from(reservationMapper.selectById(id));
    }

    private void validateSchedule(ReservationRequestDto request, Long excludeId) {
        LocalDateTime start = request.getReservationStart();
        ReservationSlotSupport.assertAlignedSlot(start);
        if (!start.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("지난 시간은 예약할 수 없습니다.");
        }

        LocalDate reservationDate = start.toLocalDate();
        int dayOfWeek = reservationDate.getDayOfWeek().getValue();

        BusinessHour businessHour = businessHourMapper.selectByDayOfWeekForUpdate(dayOfWeek);
        if (businessHour == null || Boolean.TRUE.equals(businessHour.getClosed())) {
            throw new IllegalArgumentException("선택한 요일은 정기 휴무일입니다.");
        }

        ServiceItem item = serviceItemMapper.selectById(request.getServiceId());
        if (item == null || !Boolean.TRUE.equals(item.getActive())) {
            throw new IllegalArgumentException("예약할 수 없는 시술 항목입니다.");
        }

        if (holidayMapper.selectByDate(reservationDate) != null) {
            throw new IllegalArgumentException("선택한 날짜는 임시 휴무일입니다.");
        }

        LocalTime startTime = start.toLocalTime();
        LocalDateTime end = start.plusMinutes(item.getDurationMinutes());
        LocalTime endTime = end.toLocalTime();
        if (!ReservationSlotSupport.fitsWithinBusinessHours(
                businessHour.getOpenTime(), businessHour.getCloseTime(), startTime, endTime)) {
            throw new IllegalArgumentException("영업시간 안에서 시술이 끝나는 시간을 선택해 주세요.");
        }

        int overlap = reservationMapper.countOverlapForUpdate(start, end, excludeId);
        if (overlap > 0) {
            throw new IllegalStateException("이미 예약된 시간과 겹칩니다.");
        }
    }

    private Reservation getOwnedReservation(Long id, Long userId, boolean admin) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
        if (!admin && !reservation.getUserId().equals(userId)) {
            throw new SecurityException("본인의 예약만 확인할 수 있습니다.");
        }
        return reservation;
    }

    private List<ReservationResponseDto> toResponseList(List<Reservation> reservations) {
        List<ReservationResponseDto> responses = new ArrayList<>();
        for (Reservation reservation : reservations) {
            responses.add(ReservationResponseDto.from(reservation));
        }
        return responses;
    }
}
