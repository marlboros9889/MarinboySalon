package com.marinboy.service;

import com.marinboy.dao.SalonReservationDao;
import com.marinboy.dto.ReservationDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 고객 예약 신청, 예약 가능 시간 계산, 고객 이력 조회를 담당하는 서비스입니다.
@Service
public class SalonReservationService {

    private static final LocalTime OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(19, 0);
    private static final int MAX_BOOKING_DAYS = 7;

    private final SalonReservationDao salonReservationDao;
    private final SalonServiceService salonServiceService;

    public SalonReservationService(SalonReservationDao salonReservationDao, SalonServiceService salonServiceService) {
        // 예약 SQL과 시술 정보 조회를 각각 DAO/서비스에 위임합니다.
        this.salonReservationDao = salonReservationDao;
        this.salonServiceService = salonServiceService;
    }

    public ReservationDto getAvailableSlots(Long serviceId, LocalDate date) {
        // 당일 예약 준비 시간을 확보하기 위해 현재 시각에서 30분 이후부터 허용합니다.
        LocalDateTime minimumTime = LocalDateTime.now().plusMinutes(30);
        if (date.isBefore(LocalDate.now()) || date.isAfter(LocalDate.now().plusDays(MAX_BOOKING_DAYS))) {
            return availableSlots(List.of());
        }
        // 휴무일이거나 일요일이면 고객에게 예약 가능한 시간을 보여주지 않습니다.
        if (date.getDayOfWeek().getValue() == 7 || salonReservationDao.countHoliday(date) > 0) {
            return availableSlots(List.of());
        }

        Integer durationMinutes = salonServiceService.getDurationMinutes(serviceId);
        if (durationMinutes == null) {
            return availableSlots(List.of());
        }

        List<LocalDateTime> slots = new ArrayList<>();
        LocalTime cursor = OPEN_TIME;
        // 30분 간격으로 영업 종료 전에 시술을 마칠 수 있는 시간만 검사합니다.
        while (!cursor.plusMinutes(durationMinutes).isAfter(CLOSE_TIME)) {
            LocalDateTime reservationDateTime = LocalDateTime.of(date, cursor);
            if (!reservationDateTime.isBefore(minimumTime)
                    && salonReservationDao.countOverlappingReservation(serviceId, reservationDateTime) == 0) {
                slots.add(reservationDateTime);
            }
            cursor = cursor.plusMinutes(30);
        }
        return availableSlots(slots);
    }

    @Transactional
    public void createReservation(ReservationDto request) {
        validateReservationDateTime(request.getServiceId(), request.getReservationDateTime());
        // 저장 직전에 다시 검증하여 화면 조회 이후 생긴 중복 예약도 차단합니다.
        if (request.getReservationDateTime() == null
                || request.getReservationDateTime().isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new IllegalArgumentException("예약은 현재 시간보다 최소 30분 이후부터 가능합니다.");
        }
        // 필수 동의와 중복 시간 검사를 통과한 경우에만 예약을 저장합니다.
        if (!Boolean.TRUE.equals(request.getNoShowPolicyAgreed())) {
            throw new IllegalArgumentException("노쇼 방지 안내에 동의해야 예약할 수 있습니다.");
        }
        if (salonReservationDao.countHoliday(request.getReservationDateTime().toLocalDate()) > 0) {
            throw new IllegalArgumentException("선택한 날짜는 휴무일입니다.");
        }
        if (salonReservationDao.countOverlappingReservation(request.getServiceId(), request.getReservationDateTime()) > 0) {
            throw new IllegalArgumentException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }

        Long customerId = salonReservationDao.findCustomerIdByPhone(request.getCustomerPhone());
        // 등록되지 않은 고객이면 입력받은 연락처로 고객을 생성한 후 예약과 연결합니다.
        if (customerId == null) {
            salonReservationDao.insertCustomer(
                    "customer_" + System.currentTimeMillis(),
                    "$2a$10$Zm3EwK1FLMZm64ojzpp/QuGvl28HcVXwMG86.XLQ0DGgILiOU.L8.",
                    request.getCustomerName(),
                    request.getCustomerEmail(),
                    request.getCustomerPhone()
            );
            customerId = salonReservationDao.findInsertedCustomerId(request.getCustomerPhone());
        }

        salonReservationDao.insertReservation(
                request.getServiceId(),
                customerId,
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getCustomerPhone(),
                request.getReservationDateTime(),
                1,
                request.getMemo()
        );
    }

    public List<ReservationDto> getCustomerHistory(String customerPhone) {
        // 고객 연락처 기준으로 과거 예약/시술 기록을 조회합니다.
        return salonReservationDao.findCustomerHistory(customerPhone)
                .stream()
                .filter(history -> List.of("COMPLETED", "CANCELED", "REJECTED").contains(history.getStatus()))
                .peek(this::normalizeSeedHistoryText)
                .peek(this::translateHistoryStatus)
                .toList();
    }

    public List<ReservationDto> getCustomerActiveReservations(String customerPhone) {
        // 고객 화면에는 접수 대기 또는 승인 상태인 예약만 표시합니다.
        return salonReservationDao.findCustomerHistory(customerPhone)
                .stream()
                .filter(reservation -> List.of("REQUESTED", "CONFIRMED").contains(reservation.getStatus()))
                .peek(this::normalizeSeedHistoryText)
                .peek(reservation -> {
                    if ("REQUESTED".equals(reservation.getStatus())) reservation.setStatus("예약 대기");
                    else if ("CONFIRMED".equals(reservation.getStatus())) reservation.setStatus("예약 승인");
                })
                .toList();
    }

    @Transactional
    public void updateCustomerReservation(Long reservationId, String customerPhone, ReservationDto request) {
        validateReservationDateTime(request.getServiceId(), request.getReservationDateTime());
        ReservationDto current = salonReservationDao.findCustomerReservation(reservationId, customerPhone);
        if (current == null || !List.of("REQUESTED", "CONFIRMED").contains(current.getStatus()))
            throw new IllegalArgumentException("수정할 수 있는 예약이 없습니다.");
        if (!current.getReservationDateTime().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("지난 예약은 수정할 수 없습니다.");
        if (request.getReservationDateTime() == null || request.getReservationDateTime().isBefore(LocalDateTime.now().plusMinutes(30)))
            throw new IllegalArgumentException("예약은 현재 시간보다 최소 30분 이후부터 가능합니다.");
        if (salonReservationDao.countHoliday(request.getReservationDateTime().toLocalDate()) > 0)
            throw new IllegalArgumentException("선택한 날짜는 휴무일입니다.");
        if (salonReservationDao.countOverlappingReservationExcept(request.getServiceId(), request.getReservationDateTime(), reservationId) > 0)
            throw new IllegalArgumentException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        if (salonReservationDao.updateCustomerReservation(reservationId, customerPhone, request.getServiceId(), request.getReservationDateTime(), request.getMemo()) == 0)
            throw new IllegalArgumentException("예약 수정에 실패했습니다.");
    }

    @Transactional
    public void cancelCustomerReservation(Long reservationId, String customerPhone) {
        ReservationDto current = salonReservationDao.findCustomerReservation(reservationId, customerPhone);
        if (current == null || !"REQUESTED".equals(current.getStatus()))
            throw new IllegalArgumentException("예약 대기 상태인 본인 예약만 취소할 수 있습니다.");
        if (!current.getReservationDateTime().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("지난 예약은 취소할 수 없습니다.");
        if (salonReservationDao.cancelCustomerReservation(reservationId, customerPhone) == 0) {
            throw new IllegalArgumentException("예약 취소 처리 중 상태가 변경되었습니다. 새로고침 후 다시 확인해 주세요.");
        }
    }

    public List<ReservationDto> getReservations() {
        // 관리자 화면에서 전체 예약을 확인할 수 있게 최신순 목록을 반환합니다.
        return salonReservationDao.findAllReservations();
    }
    public int countReservations() { return salonReservationDao.countReservations(); }
    public List<ReservationDto> getReservationsPage(int page, int size) { return salonReservationDao.findReservationsPage(Math.max(page, 0) * size, size); }
    public ReservationDto getReservation(Long reservationId) { return salonReservationDao.findReservationById(reservationId); }
    public List<ReservationDto> getReminderTargets() { return salonReservationDao.findReminderTargets(); }

    public List<LocalDate> getHolidays() { return salonReservationDao.findHolidays(); }

    @Transactional
    public void saveHoliday(LocalDate holidayDate, String reason) {
        if (holidayDate == null) throw new IllegalArgumentException("휴무일을 선택하세요.");
        salonReservationDao.saveHoliday(holidayDate, reason == null ? "" : reason);
    }

    @Transactional
    public void deleteHoliday(LocalDate holidayDate) { salonReservationDao.deleteHoliday(holidayDate); }

    @Transactional
    public void updateReservationStatus(Long reservationId, String status) {
        // 현재 상태에서 허용된 다음 상태로만 변경해 잘못된 처리 순서를 막습니다.
        ReservationDto reservation = requireReservation(reservationId);
        boolean allowed = "REQUESTED".equals(reservation.getStatus())
                ? List.of("CONFIRMED", "REJECTED", "CANCELED").contains(status)
                : "CONFIRMED".equals(reservation.getStatus())
                    && List.of("REJECTED", "CANCELED", "COMPLETED").contains(status);
        if (!allowed) {
            throw new IllegalArgumentException("현재 예약 상태에서는 해당 처리를 할 수 없습니다.");
        }
        if (!List.of("REQUESTED", "CONFIRMED", "REJECTED", "CANCELED", "COMPLETED", "NO_SHOW").contains(status)) {
            throw new IllegalArgumentException("허용되지 않은 예약 상태입니다.");
        }
        if (salonReservationDao.updateReservationStatus(reservationId, status, null) == 0) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void rejectReservation(Long reservationId, String reason) {
        // 거절은 진행 중인 예약에만 허용하며 사유를 필수로 기록합니다.
        ReservationDto reservation = requireReservation(reservationId);
        if (!List.of("REQUESTED", "CONFIRMED").contains(reservation.getStatus())) {
            throw new IllegalArgumentException("대기 또는 승인 상태의 예약만 거절할 수 있습니다.");
        }
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("거절 사유를 입력하세요.");
        if (salonReservationDao.updateReservationStatus(reservationId, "REJECTED", reason) == 0) throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
    }

    public Map<String, Integer> getRetentionSummary() {
        return Map.of(
                "reservations", salonReservationDao.countOldTerminalReservations(),
                "customers", salonReservationDao.countOldOrphanCustomers(),
                "services", salonReservationDao.countOldDeletedServices());
    }

    @Transactional
    public Map<String, Integer> cleanupOldData(String confirmation) {
        if (!"2년 이전 데이터 정리".equals(confirmation)) {
            throw new IllegalArgumentException("확인 문구를 정확히 입력하세요.");
        }
        int reservations = salonReservationDao.deleteOldTerminalReservations();
        int customers = salonReservationDao.deleteOldOrphanCustomers();
        int services = salonReservationDao.deleteOldDeletedServices();
        return Map.of("reservations", reservations, "customers", customers, "services", services);
    }

    private ReservationDto requireReservation(Long reservationId) {
        ReservationDto reservation = salonReservationDao.findReservationById(reservationId);
        if (reservation == null) throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        return reservation;
    }

    private void validateReservationDateTime(Long serviceId, LocalDateTime reservationDateTime) {
        if (serviceId == null || reservationDateTime == null
                || reservationDateTime.isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new IllegalArgumentException("Reservation time must be at least 30 minutes in the future.");
        }
        Integer durationMinutes = salonServiceService.getDurationMinutes(serviceId);
        if (durationMinutes == null) {
            throw new IllegalArgumentException("Selected service does not exist.");
        }
        LocalDate date = reservationDateTime.toLocalDate();
        LocalTime time = reservationDateTime.toLocalTime();
        if (date.isAfter(LocalDate.now().plusDays(MAX_BOOKING_DAYS))
                || date.getDayOfWeek().getValue() == 7
                || time.getMinute() % 30 != 0
                || time.isBefore(OPEN_TIME)
                || time.plusMinutes(durationMinutes).isAfter(CLOSE_TIME)) {
            throw new IllegalArgumentException("Reservation time must be within business hours in 30-minute increments.");
        }
    }

    private ReservationDto availableSlots(List<LocalDateTime> slots) {
        ReservationDto response = new ReservationDto();
        response.setAvailableSlots(slots);
        return response;
    }

    private void normalizeSeedHistoryText(ReservationDto history) {
        // 초기 SQLPlus 샘플 데이터가 깨져 있어도 고객 이력 화면은 정상 한글로 보여줍니다.
        if (history.getServiceId() == null) {
            return;
        }

        switch (history.getServiceId().intValue()) {
            case 1 -> {
                history.setServiceName("웨이브 펌");
                history.setServiceCategory("펌");
            }
            case 2 -> {
                history.setServiceName("시그니처 컷");
                history.setServiceCategory("컷");
            }
            case 3 -> {
                history.setServiceName("두피 클리닉");
                history.setServiceCategory("클리닉");
            }
            case 4 -> {
                history.setServiceName("젤 네일 기본");
                history.setServiceCategory("네일");
            }
            case 5 -> {
                history.setServiceName("꾸미기 화장");
                history.setServiceCategory("메이크업");
            }
            case 6 -> {
                history.setServiceName("신부 화장");
                history.setServiceCategory("웨딩");
            }
            default -> {
                // 관리자가 새로 등록한 메뉴는 DB 값을 그대로 사용합니다.
            }
        }
    }

    private void translateHistoryStatus(ReservationDto history) {
        if ("COMPLETED".equals(history.getStatus())) history.setStatus("완료");
        else if ("CANCELED".equals(history.getStatus()) || "REJECTED".equals(history.getStatus())) history.setStatus("취소");
    }
}
