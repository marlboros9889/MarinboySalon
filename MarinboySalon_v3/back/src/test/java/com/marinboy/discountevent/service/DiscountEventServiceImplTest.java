package com.marinboy.discountevent.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.marinboy.discountevent.dto.request.DiscountEventRequestDto;
import com.marinboy.discountevent.repository.DiscountEventMapper;

/** 기간과 할인율의 잘못된 입력이 DB 저장 전에 차단되는지 확인합니다. */
class DiscountEventServiceImplTest {

    @Test
    void rejectsReversedPeriodBeforeSaving() {
        DiscountEventMapper mapper = mock(DiscountEventMapper.class);
        DiscountEventServiceImpl service = new DiscountEventServiceImpl(mapper);
        DiscountEventRequestDto request = request(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 9), "10.0");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료일은 시작일보다 빠를 수 없습니다.");
    }

    @Test
    void rejectsOverlappingPeriodBeforeSaving() {
        DiscountEventMapper mapper = mock(DiscountEventMapper.class);
        when(mapper.countOverlappingPeriod(any(), any(), isNull())).thenReturn(1);
        DiscountEventServiceImpl service = new DiscountEventServiceImpl(mapper);
        DiscountEventRequestDto request = request(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 20), "15.0");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 적용 기간이 겹치는 할인 이벤트가 있습니다.");
    }

    private DiscountEventRequestDto request(LocalDate startDate, LocalDate endDate, String rate) {
        DiscountEventRequestDto request = new DiscountEventRequestDto();
        request.setName("첫 방문 할인");
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setDiscountRate(new BigDecimal(rate));
        return request;
    }
}
