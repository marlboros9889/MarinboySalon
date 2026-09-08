package com.marinboy.serviceitem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.marinboy.serviceitem.dto.request.ServiceItemRequestDto;
import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.repository.ServiceItemMapper;

/** 신규 메뉴의 표시 여부가 관리자 입력값대로 저장되는지 확인합니다. */
class ServiceItemServiceImplTest {

    @Test
    void savesNewMenuAsInactiveWhenAdminTurnsOffCustomerDisplay() {
        ServiceItemMapper mapper = mock(ServiceItemMapper.class);
        ServiceItem savedItem = new ServiceItem();
        savedItem.setId(1L);
        savedItem.setName("테스트 메뉴");
        savedItem.setPrice(30000);
        savedItem.setDurationMinutes(30);
        savedItem.setActive(false);
        when(mapper.selectById(1L)).thenReturn(savedItem);
        when(mapper.selectImagesByServiceItemIds(any())).thenReturn(List.of());
        doAnswer(invocation -> {
            invocation.getArgument(0, ServiceItem.class).setId(1L);
            return 1;
        }).when(mapper).insert(any(ServiceItem.class));

        ServiceItemRequestDto request = new ServiceItemRequestDto();
        request.setName("테스트 메뉴");
        request.setPrice(30000);
        request.setDurationMinutes(30);
        request.setActive(false);
        request.setImageUrls(List.of());

        new ServiceItemServiceImpl(mapper).insert(request);

        ArgumentCaptor<ServiceItem> capturedItem = ArgumentCaptor.forClass(ServiceItem.class);
        verify(mapper).insert(capturedItem.capture());
        assertThat(capturedItem.getValue().getActive()).isFalse();
    }
}
