package com.marinboy.dto.v3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.marinboy.entity.ServiceItemEntity;

/** v3 API가 Entity 내부 구조를 노출하지 않도록 하는 응답 DTO입니다. */
public record ServiceItemResponseDto(Long id, String name, String category, Integer durationMinutes,
        BigDecimal price, String description, Integer topRank, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ServiceItemResponseDto from(ServiceItemEntity entity) {
        return new ServiceItemResponseDto(entity.getId(), entity.getName(), entity.getCategory(),
                entity.getDurationMinutes(), entity.getPrice(), entity.getDescription(), entity.getTopRank(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
