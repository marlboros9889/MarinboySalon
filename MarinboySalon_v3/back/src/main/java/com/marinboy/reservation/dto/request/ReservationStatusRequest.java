package com.marinboy.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자가 예약 상태를 바꿀 때 보내는 값입니다.
 */
@Getter
@Setter
public class ReservationStatusRequest {

    @NotBlank(message = "예약 상태는 필수입니다.")
    private String status;
}
