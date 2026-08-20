package com.marinboy.service;

import java.util.List;
import java.util.NoSuchElementException;

import com.marinboy.dto.v3.ServiceItemRequestDto;
import com.marinboy.dto.v3.ServiceItemResponseDto;
import com.marinboy.entity.ServiceItemEntity;
import com.marinboy.repository.ServiceItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** v3 시술 메뉴의 Entity·DTO 변환과 트랜잭션 규칙을 담당합니다. */
@Service
public class ServiceItemJpaService {
    private final ServiceItemRepository serviceItemRepository;

    public ServiceItemJpaService(ServiceItemRepository serviceItemRepository) {
        this.serviceItemRepository = serviceItemRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceItemResponseDto> findAll() {
        // 고객에게는 논리 삭제된 메뉴를 제외한 활성 메뉴만 반환합니다.
        return serviceItemRepository.findAll().stream()
                .filter(this::isActive)
                .map(ServiceItemResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceItemResponseDto findById(Long id) {
        return ServiceItemResponseDto.from(findEntity(id));
    }

    @Transactional
    public ServiceItemResponseDto create(ServiceItemRequestDto request) {
        validateBusinessRules(request);
        ServiceItemEntity entity = new ServiceItemEntity(request.name().trim(), request.category().trim(),
                request.durationMinutes(), request.price(), request.description().trim(), request.topRank());
        return ServiceItemResponseDto.from(serviceItemRepository.save(entity));
    }

    @Transactional
    public ServiceItemResponseDto update(Long id, ServiceItemRequestDto request) {
        validateBusinessRules(request);
        ServiceItemEntity entity = findEntity(id);
        entity.change(request.name().trim(), request.category().trim(), request.durationMinutes(), request.price(),
                request.description().trim(), request.topRank());
        return ServiceItemResponseDto.from(entity);
    }

    private ServiceItemEntity findEntity(Long id) {
        ServiceItemEntity entity = serviceItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 시술 메뉴입니다."));
        if (!isActive(entity)) {
            throw new NoSuchElementException("존재하지 않는 시술 메뉴입니다.");
        }
        return entity;
    }

    private boolean isActive(ServiceItemEntity entity) {
        return !"DELETED".equals(entity.getCategory());
    }

    private void validateBusinessRules(ServiceItemRequestDto request) {
        // Oracle 제약 조건과 동일하게 10분·1,000원 단위만 저장합니다.
        if (request.durationMinutes() % 10 != 0 || request.price().remainder(java.math.BigDecimal.valueOf(1000)).signum() != 0) {
            throw new IllegalArgumentException("시술 시간은 10분, 금액은 1,000원 단위로 입력해야 합니다.");
        }
        if (request.topRank() != null && (request.topRank() < 1 || request.topRank() > 3)) {
            throw new IllegalArgumentException("TOP 순위는 1~3만 입력할 수 있습니다.");
        }
    }
}
