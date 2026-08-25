package com.marinboy.businesshour.dto.request;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자가 요일별 영업시간을 수정할 때 보내는 값입니다.
 */
@Getter
@Setter
public class BusinessHourRequest {

    @NotNull(message = "영업시간 번호는 필수입니다.")
    private Long id;
    private LocalTime openTime;
    private LocalTime closeTime;

    @NotNull(message = "휴무 여부는 필수입니다.")
    private Boolean closed;
}
