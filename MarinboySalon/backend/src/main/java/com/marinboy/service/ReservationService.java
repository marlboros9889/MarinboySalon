package com.marinboy.service;

import com.marinboy.dto.ReservationDto;
import com.marinboy.mapper.ReservationMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 고객 예약의 생성·수정·취소와 관리자 상태 변경을 담당합니다. */
@Service
public class ReservationService {
    private final ReservationMapper reservationMapper;
    private final ServiceItemService serviceItemService;
    private final ReservationScheduleService reservationScheduleService;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationService(
            ReservationMapper reservationMapper,
            ServiceItemService serviceItemService,
            ReservationScheduleService reservationScheduleService,
            ApplicationEventPublisher eventPublisher) {
        this.reservationMapper = reservationMapper;
        this.serviceItemService = serviceItemService;
        this.reservationScheduleService = reservationScheduleService;
        this.eventPublisher = eventPublisher;
    }

    /** JWT 사용자 ID를 소유자로 고정하고 DB 저장 뒤 Calendar를 동기화합니다. */
    @Transactional
    public void createReservation(ReservationDto request, Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("로그인한 고객만 예약할 수 있습니다.");
        }
        validatePolicy(request);
        reservationScheduleService.validateBookable(request.getServiceId(), request.getReservationDateTime());

        // 한 명의 디자이너가 모든 시술을 맡으므로 공통 일정 행을 잠근 뒤 중복을 다시 확인합니다.
        if (reservationMapper.lockReservationSchedule(request.getServiceId()) == null) {
            throw new IllegalArgumentException("선택한 시술 메뉴가 없거나 삭제되었습니다.");
        }
        if (reservationMapper.countOverlappingReservation(
                request.getServiceId(), request.getReservationDateTime()) > 0) {
            throw new IllegalArgumentException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }

        reservationMapper.insertReservation(
                request.getServiceId(), customerId,
                request.getCustomerName(), request.getCustomerEmail(), request.getCustomerPhone(),
                request.getReservationDateTime(), 1, request.getMemo());

        String serviceName = serviceItemService.getServiceName(request.getServiceId());
        Integer durationMinutes = serviceItemService.getDurationMinutes(request.getServiceId());
        GoogleCalendarReservationEvent calendarEvent = new GoogleCalendarReservationEvent(
                request.getCustomerName(), request.getCustomerPhone(), serviceName,
                request.getReservationDateTime(), durationMinutes == null ? 60 : durationMinutes);
        // 예약 서비스는 외부 API를 모르고 커밋 뒤 필요한 사실만 이벤트로 알립니다.
        eventPublisher.publishEvent(calendarEvent);
    }

    /** 로그인 고객 ID로만 조회해 전화번호 변경과 무관하게 예약 소유권을 유지합니다. */
    public List<ReservationDto> getCustomerActiveReservations(Long customerId) {
        return reservationMapper.findCustomerReservationsByCustomerId(customerId)
                .stream()
                .filter(item -> List.of("REQUESTED", "CONFIRMED").contains(item.getStatus()))
                .toList();
    }

    /** 본인 예약만 수정하며 새 예약과 같은 영업시간·중복 규칙을 다시 적용합니다. */
    @Transactional
    public void updateCustomerReservation(Long reservationId, Long customerId, ReservationDto request) {
        validatePolicy(request);
        normalizeReservationTime(request);
        reservationScheduleService.validateBookable(request.getServiceId(), request.getReservationDateTime());

        ReservationDto current = reservationMapper.findCustomerReservationByCustomerId(reservationId, customerId);
        validateOwnedReservationForUpdate(current, request, reservationId);
        int updated = reservationMapper.updateCustomerReservationByCustomerId(
                reservationId, customerId, request.getServiceId(),
                request.getReservationDateTime(), request.getMemo());
        if (updated == 0) {
            throw new IllegalArgumentException("예약 수정에 실패했습니다.");
        }
    }

    /** 고객은 아직 승인되지 않은 미래 예약만 취소할 수 있습니다. */
    @Transactional
    public void cancelCustomerReservation(Long reservationId, Long customerId) {
        ReservationDto current = reservationMapper.findCustomerReservationByCustomerId(reservationId, customerId);
        if (current == null || !"REQUESTED".equals(current.getStatus())) {
            throw new IllegalArgumentException("예약 대기 상태인 본인 예약만 취소할 수 있습니다.");
        }
        if (!current.getReservationDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("지난 예약은 취소할 수 없습니다.");
        }
        if (reservationMapper.cancelCustomerReservationByCustomerId(reservationId, customerId) == 0) {
            throw new IllegalArgumentException("예약 상태가 변경되었습니다. 새로고침 후 다시 확인해 주세요.");
        }
    }

    public int countReservations() {
        return reservationMapper.countReservations();
    }

    public List<ReservationDto> getReservationsPage(int page, int size) {
        int validPage = Math.max(page, 0);
        int validSize = Math.min(Math.max(size, 1), 50);
        return reservationMapper.findReservationsPage(validPage * validSize, validSize);
    }


    /** 예약 상태는 대기→승인/종료, 승인→완료/종료 순서만 허용합니다. */
    @Transactional
    public void updateReservationStatus(Long reservationId, String status) {
        ReservationDto reservation = requireReservation(reservationId);
        boolean allowedFromRequested = "REQUESTED".equals(reservation.getStatus())
                && List.of("CONFIRMED", "REJECTED", "CANCELED").contains(status);
        boolean allowedFromConfirmed = "CONFIRMED".equals(reservation.getStatus())
                && List.of("REJECTED", "CANCELED", "COMPLETED").contains(status);
        if (!allowedFromRequested && !allowedFromConfirmed) {
            throw new IllegalArgumentException("현재 예약 상태에서는 해당 처리를 할 수 없습니다.");
        }
        if (reservationMapper.updateReservationStatus(reservationId, status) == 0) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
    }

    private void validatePolicy(ReservationDto request) {
        if (request == null || !Boolean.TRUE.equals(request.getNoShowPolicyAgreed())) {
            throw new IllegalArgumentException("노쇼 방지 안내에 동의해야 예약할 수 있습니다.");
        }
    }

    private void normalizeReservationTime(ReservationDto request) {
        LocalDateTime requestedTime = request.getReservationDateTime();
        if (requestedTime != null) {
            request.setReservationDateTime(requestedTime
                    .withMinute((requestedTime.getMinute() / 30) * 30)
                    .withSecond(0)
                    .withNano(0));
        }
    }

    private void validateOwnedReservationForUpdate(
            ReservationDto current, ReservationDto request, Long reservationId) {
        if (current == null || !List.of("REQUESTED", "CONFIRMED").contains(current.getStatus())) {
            throw new IllegalArgumentException("수정할 수 있는 예약이 없습니다.");
        }
        if (!current.getReservationDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("지난 예약은 수정할 수 없습니다.");
        }
        if (reservationMapper.lockReservationSchedule(request.getServiceId()) == null) {
            throw new IllegalArgumentException("선택한 시술 메뉴가 없거나 삭제되었습니다.");
        }
        int overlapCount = reservationMapper.countOverlappingReservationExcept(
                request.getServiceId(), request.getReservationDateTime(), reservationId);
        if (overlapCount > 0) {
            throw new IllegalArgumentException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }
    }

    private ReservationDto requireReservation(Long reservationId) {
        ReservationDto reservation = reservationMapper.findReservationById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
        return reservation;
    }
}
