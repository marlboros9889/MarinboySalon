package com.marinboy.service;

import com.marinboy.dto.BusinessHourRequestDto;
import com.marinboy.dto.BusinessHourResponseDto;
import com.marinboy.dto.HolidayResponseDto;
import com.marinboy.dto.ReservationDto;
import com.marinboy.mapper.ReservationMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 영업시간·휴무일·기존 예약을 예약 시간 계산 도구에 연결하는 일정 Service입니다. */
@Service
public class ReservationScheduleService {
    private static final LocalTime DEFAULT_OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime DEFAULT_CLOSE_TIME = LocalTime.of(19, 0);
    private static final int MAX_BOOKING_DAYS = 7;

    private final ReservationMapper reservationMapper;
    private final ServiceItemService serviceItemService;
    private final ReservationScheduleTool reservationScheduleTool;

    public ReservationScheduleService(
            ReservationMapper reservationMapper,
            ServiceItemService serviceItemService,
            ReservationScheduleTool reservationScheduleTool) {
        this.reservationMapper = reservationMapper;
        this.serviceItemService = serviceItemService;
        this.reservationScheduleTool = reservationScheduleTool;
    }

    /** 날짜별 예약은 한 번만 조회하고 후보 시간 비교는 계산 도구에 맡깁니다. */
    public ReservationDto getAvailableSlots(Long serviceId, LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date == null || date.isBefore(today) || date.isAfter(today.plusDays(MAX_BOOKING_DAYS))) {
            return availableSlots(List.of());
        }
        if (isHoliday(date)) {
            return availableSlots(List.of());
        }

        BusinessHourResponseDto businessHour = getBusinessHour(date);
        Integer durationMinutes = serviceItemService.getDurationMinutes(serviceId);
        if (!Boolean.TRUE.equals(businessHour.getOpen()) || durationMinutes == null) {
            return availableSlots(List.of());
        }

        List<ReservationDto> existingReservations = reservationMapper.findActiveReservationsForDate(date);
        List<LocalDateTime> slots = reservationScheduleTool.createAvailableSlots(
                date,
                LocalTime.parse(businessHour.getOpenTime()),
                LocalTime.parse(businessHour.getCloseTime()),
                durationMinutes,
                LocalDateTime.now().plusMinutes(30),
                existingReservations);
        return availableSlots(slots);
    }

    /** 저장 직전에도 화면과 같은 날짜·영업시간 규칙을 서버에서 다시 확인합니다. */
    public void validateBookable(Long serviceId, LocalDateTime reservationDateTime) {
        if (serviceId == null || reservationDateTime == null
                || reservationDateTime.isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new IllegalArgumentException("예약은 현재 시간보다 최소 30분 이후여야 합니다.");
        }
        Integer durationMinutes = serviceItemService.getDurationMinutes(serviceId);
        if (durationMinutes == null) {
            throw new IllegalArgumentException("선택한 시술 메뉴가 없습니다.");
        }

        LocalDate date = reservationDateTime.toLocalDate();
        LocalTime time = reservationDateTime.toLocalTime();
        if (isHoliday(date)) {
            throw new IllegalArgumentException("선택한 날짜는 휴무일입니다.");
        }
        BusinessHourResponseDto businessHour = getBusinessHour(date);
        LocalTime openTime = LocalTime.parse(businessHour.getOpenTime());
        LocalTime closeTime = LocalTime.parse(businessHour.getCloseTime());
        boolean outsideBookingPeriod = date.isAfter(LocalDate.now().plusDays(MAX_BOOKING_DAYS));
        boolean outsideBusinessHours = !Boolean.TRUE.equals(businessHour.getOpen())
                || time.getMinute() % 30 != 0
                || time.isBefore(openTime)
                || time.plusMinutes(durationMinutes).isAfter(closeTime);
        if (outsideBookingPeriod || outsideBusinessHours) {
            throw new IllegalArgumentException("선택한 시간은 예약 가능 범위가 아닙니다.");
        }
    }

    private boolean isHoliday(LocalDate date) {
        return date != null && reservationMapper.countHoliday(date) > 0;
    }

    public List<HolidayResponseDto> getHolidays() {
        return reservationMapper.findHolidays();
    }

    @Transactional
    public void saveHoliday(LocalDate holidayDate, String reason) {
        if (holidayDate == null) {
            throw new IllegalArgumentException("휴무일을 선택하세요.");
        }
        reservationMapper.saveHoliday(holidayDate, reason == null ? "" : reason.trim());
    }

    @Transactional
    public void deleteHoliday(LocalDate holidayDate) {
        reservationMapper.deleteHoliday(holidayDate);
    }

    public List<BusinessHourResponseDto> getBusinessHours() {
        return reservationMapper.findBusinessHours();
    }

    /** 관리자 영업시간을 검증한 뒤 예약 계산의 단일 기준으로 저장합니다. */
    @Transactional
    public void saveBusinessHour(BusinessHourRequestDto request) {
        if (request == null || request.getDayOfWeek() == null
                || request.getDayOfWeek() < 1 || request.getDayOfWeek() > 7) {
            throw new IllegalArgumentException("요일 값은 월요일 1부터 일요일 7 사이여야 합니다.");
        }
        if (request.getOpen() == null) {
            throw new IllegalArgumentException("영업 여부를 선택하세요.");
        }

        LocalTime openTime = parseBusinessTime(request.getOpenTime(), "영업 시작 시간을 확인하세요.");
        LocalTime closeTime = parseBusinessTime(request.getCloseTime(), "영업 종료 시간을 확인하세요.");
        if (openTime.getMinute() % 30 != 0 || closeTime.getMinute() % 30 != 0) {
            throw new IllegalArgumentException("영업시간은 30분 단위로 설정하세요.");
        }
        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("영업 종료 시간은 시작 시간보다 늦어야 합니다.");
        }

        reservationMapper.saveBusinessHour(
                request.getDayOfWeek(), Boolean.TRUE.equals(request.getOpen()) ? 1 : 0,
                openTime.toString(), closeTime.toString());
    }

    private BusinessHourResponseDto getBusinessHour(LocalDate date) {
        BusinessHourResponseDto businessHour = reservationMapper.findBusinessHour(date.getDayOfWeek().getValue());
        if (businessHour != null) {
            return businessHour;
        }

        BusinessHourResponseDto defaultHour = new BusinessHourResponseDto();
        defaultHour.setDayOfWeek(date.getDayOfWeek().getValue());
        defaultHour.setOpen(true);
        defaultHour.setOpenTime(DEFAULT_OPEN_TIME.toString());
        defaultHour.setCloseTime(DEFAULT_CLOSE_TIME.toString());
        return defaultHour;
    }

    private LocalTime parseBusinessTime(String value, String message) {
        try {
            return LocalTime.parse(value);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private ReservationDto availableSlots(List<LocalDateTime> slots) {
        ReservationDto response = new ReservationDto();
        response.setAvailableSlots(slots);
        return response;
    }
}
