package com.marinboy.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.businesshour.entity.BusinessHour;
import com.marinboy.businesshour.repository.BusinessHourMapper;
import com.marinboy.holiday.repository.HolidayMapper;
import com.marinboy.reservation.dto.request.ReservationRequestDto;
import com.marinboy.reservation.dto.response.ReservationResponseDto;
import com.marinboy.reservation.entity.Reservation;
import com.marinboy.reservation.repository.ReservationMapper;
import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.repository.ServiceItemMapper;

import lombok.RequiredArgsConstructor;

/**
 * 예약 소유권, 영업시간, 휴무일, 시간 중복을 한 곳에서 검사합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private static final Set<String> ADMIN_STATUS =
            Set.of("REQUESTED", "CONFIRMED", "COMPLETED", "CANCELED");

    private final ReservationMapper reservationMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final BusinessHourMapper businessHourMapper;
    private final HolidayMapper holidayMapper;

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
        reservation.setStatus("REQUESTED");
        reservationMapper.insert(reservation);
        return ReservationResponseDto.from(reservationMapper.selectById(reservation.getId()));
    }

    @Override
    public ReservationResponseDto update(Long id, Long userId, ReservationRequestDto request) {
        Reservation reservation = getOwnedReservation(id, userId, false);
        if (!"REQUESTED".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("접수 상태의 예약만 변경할 수 있습니다.");
        }

        validateSchedule(request, id);
        reservation.setServiceId(request.getServiceId());
        reservation.setReservationStart(request.getReservationStart());
        reservation.setRequestMemo(request.getRequestMemo());
        reservationMapper.update(reservation);
        return ReservationResponseDto.from(reservationMapper.selectById(id));
    }

    @Override
    public void cancel(Long id, Long userId, boolean admin) {
        Reservation reservation = getOwnedReservation(id, userId, admin);
        if ("COMPLETED".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("완료된 예약은 취소할 수 없습니다.");
        }
        reservationMapper.updateStatus(id, "CANCELED");
    }

    @Override
    public ReservationResponseDto updateStatus(Long id, String status) {
        if (!ADMIN_STATUS.contains(status)) {
            throw new IllegalArgumentException("사용할 수 없는 예약 상태입니다.");
        }
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
        reservationMapper.updateStatus(id, status);
        return ReservationResponseDto.from(reservationMapper.selectById(id));
    }

    private void validateSchedule(ReservationRequestDto request, Long excludeId) {
        LocalDateTime start = request.getReservationStart();
        if (!start.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("지난 시간은 예약할 수 없습니다.");
        }

        LocalDate reservationDate = start.toLocalDate();
        int dayOfWeek = reservationDate.getDayOfWeek().getValue();

        // 다른 조회보다 먼저 잠가 대기 후 생성되는 DB 읽기 스냅샷이 최신 예약을 포함하게 합니다.
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
        if (startTime.isBefore(businessHour.getOpenTime()) || endTime.isAfter(businessHour.getCloseTime())) {
            throw new IllegalArgumentException("영업시간 안에서 시술이 끝나는 시간을 선택해 주세요.");
        }

        if (reservationMapper.countOverlap(start, end, excludeId) > 0) {
            throw new IllegalArgumentException("이미 예약된 시간과 겹칩니다.");
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
