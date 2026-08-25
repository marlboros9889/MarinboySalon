package com.marinboy.reservation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 고객이 예약을 등록하거나 변경할 때 보내는 값입니다.
 */
@Getter
@Setter
public class ReservationRequestDto {

    @NotNull(message = "시술 항목은 필수입니다.")
    private Long serviceId;

    @NotNull(message = "예약 일시는 필수입니다.")
    @Future(message = "예약 일시는 현재보다 이후여야 합니다.")
    private LocalDateTime reservationStart;

    @Size(max = 500, message = "요청사항은 500자 이하로 입력해 주세요.")
    private String requestMemo;
}
