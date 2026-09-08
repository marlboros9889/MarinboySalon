package com.marinboy.discountevent.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.discountevent.dto.request.DiscountEventRequestDto;
import com.marinboy.discountevent.dto.response.DiscountEventResponseDto;
import com.marinboy.discountevent.entity.DiscountEvent;
import com.marinboy.discountevent.repository.DiscountEventMapper;

import lombok.RequiredArgsConstructor;

/** 기간과 할인율을 검증해 한 시점에 하나의 이벤트만 적용되게 합니다. */
@Service
@RequiredArgsConstructor
@Transactional
public class DiscountEventServiceImpl implements DiscountEventService {

    private final DiscountEventMapper discountEventMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DiscountEventResponseDto> getAll() {
        List<DiscountEventResponseDto> responses = new ArrayList<>();
        for (DiscountEvent event : discountEventMapper.selectAll()) {
            responses.add(DiscountEventResponseDto.from(event));
        }
        return responses;
    }

    @Override
    public DiscountEventResponseDto create(DiscountEventRequestDto request) {
        DiscountEvent event = createValidatedEvent(request, null);
        discountEventMapper.insert(event);
        return DiscountEventResponseDto.from(event);
    }

    @Override
    public DiscountEventResponseDto update(Long id, DiscountEventRequestDto request) {
        if (discountEventMapper.selectById(id) == null) {
            throw new IllegalArgumentException("할인 이벤트를 찾을 수 없습니다.");
        }
        DiscountEvent event = createValidatedEvent(request, id);
        event.setId(id);
        discountEventMapper.update(event);
        return DiscountEventResponseDto.from(event);
    }

    @Override
    public void delete(Long id) {
        if (discountEventMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("할인 이벤트를 찾을 수 없습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountEvent findActiveEvent(LocalDate date) {
        return discountEventMapper.selectActiveByDate(date);
    }

    /** 고객 화면은 서버 기준 오늘 날짜로만 현재 적용 이벤트를 확인합니다. */
    @Override
    @Transactional(readOnly = true)
    public DiscountEventResponseDto getCurrentEvent() {
        DiscountEvent event = findActiveEvent(LocalDate.now());
        return event == null ? null : DiscountEventResponseDto.from(event);
    }

    private DiscountEvent createValidatedEvent(DiscountEventRequestDto request, Long excludeId) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }
        if (discountEventMapper.countOverlappingPeriod(request.getStartDate(), request.getEndDate(), excludeId) > 0) {
            throw new IllegalArgumentException("이미 적용 기간이 겹치는 할인 이벤트가 있습니다.");
        }
        DiscountEvent event = new DiscountEvent();
        event.setName(request.getName().trim());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setDiscountRate(request.getDiscountRate());
        return event;
    }
}
