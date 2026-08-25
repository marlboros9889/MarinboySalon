package com.marinboy.serviceitem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자가 시술 항목을 등록하거나 수정할 때 보내는 값입니다.
 */
@Getter
@Setter
public class ServiceItemRequestDto {

    @NotBlank(message = "시술명은 필수입니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "소요 시간은 필수입니다.")
    @Min(value = 10, message = "소요 시간은 10분 이상이어야 합니다.")
    private Integer durationMinutes;

    private String description;
    private Boolean active;
}
