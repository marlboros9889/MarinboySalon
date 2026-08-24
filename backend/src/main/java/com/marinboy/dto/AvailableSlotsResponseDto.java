package com.marinboy.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 예약 가능 시간 API는 예약 저장 DTO와 분리된 시간 목록만 반환합니다. */
public record AvailableSlotsResponseDto(List<LocalDateTime> availableSlots) {
}
