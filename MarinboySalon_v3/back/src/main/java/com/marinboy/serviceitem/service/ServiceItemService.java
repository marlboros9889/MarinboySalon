package com.marinboy.serviceitem.service;

import java.util.List;

import com.marinboy.serviceitem.dto.request.ServiceItemRequestDto;
import com.marinboy.serviceitem.dto.response.ServiceItemResponseDto;

// 고객 메뉴 조회와 관리자 메뉴 관리 기능의 업무 규칙을 정의합니다.
public interface ServiceItemService {

    List<ServiceItemResponseDto> getActiveList();

    List<ServiceItemResponseDto> getAdminList();

    ServiceItemResponseDto getDetail(Long id);

    ServiceItemResponseDto insert(ServiceItemRequestDto request);

    ServiceItemResponseDto update(Long id, ServiceItemRequestDto request);

    void delete(Long id);
}
