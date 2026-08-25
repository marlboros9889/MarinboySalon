package com.marinboy.serviceitem.service;

import java.util.List;

import com.marinboy.serviceitem.dto.request.ServiceItemRequestDto;
import com.marinboy.serviceitem.dto.response.ServiceItemResponseDto;

public interface ServiceItemService {

    List<ServiceItemResponseDto> getActiveList();

    List<ServiceItemResponseDto> getAdminList();

    ServiceItemResponseDto getDetail(Long id);

    ServiceItemResponseDto insert(ServiceItemRequestDto request);

    ServiceItemResponseDto update(Long id, ServiceItemRequestDto request);

    void delete(Long id);
}
