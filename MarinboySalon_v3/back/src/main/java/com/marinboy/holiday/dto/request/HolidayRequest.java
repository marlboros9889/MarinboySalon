package com.marinboy.holiday.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자가 임시 휴무일을 등록할 때 보내는 값입니다.
 */
@Getter
@Setter
public class HolidayRequest {

    @NotNull(message = "휴무 날짜는 필수입니다.")
    @FutureOrPresent(message = "오늘 이후 날짜를 선택해 주세요.")
    private LocalDate holidayDate;
    private String reason;
}
