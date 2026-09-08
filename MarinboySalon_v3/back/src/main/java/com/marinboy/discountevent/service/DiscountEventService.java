package com.marinboy.discountevent.service;

import java.time.LocalDate;
import java.util.List;

import com.marinboy.discountevent.dto.request.DiscountEventRequestDto;
import com.marinboy.discountevent.dto.response.DiscountEventResponseDto;
import com.marinboy.discountevent.entity.DiscountEvent;

/** 관리자 할인 이벤트의 생성·수정·삭제 규칙입니다. */
public interface DiscountEventService {
    List<DiscountEventResponseDto> getAll();
    DiscountEventResponseDto create(DiscountEventRequestDto request);
    DiscountEventResponseDto update(Long id, DiscountEventRequestDto request);
    void delete(Long id);
    DiscountEvent findActiveEvent(LocalDate date);
}
