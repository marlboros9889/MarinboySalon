package com.marinboy.serviceitem.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 4, message = "메뉴 이미지는 최대 4개까지 등록할 수 있습니다.")
    private List<@NotBlank(message = "빈 이미지 주소는 등록할 수 없습니다.")
            @Size(max = 500, message = "이미지 주소는 500자 이하여야 합니다.") String> imageUrls;
}
