package com.marinboy.serviceitem.service;

import com.marinboy.serviceitem.dto.ServiceItemDto;

import java.util.List;

/**
 * 시술 메뉴 기능에서 Controller가 사용할 작업 목록입니다.
 */
public interface ServiceItemService {

    List<ServiceItemDto> getActiveList();

    List<ServiceItemDto> getList();

    ServiceItemDto getServiceItem(Long id);

    void insert(ServiceItemDto serviceItemDto);

    void update(ServiceItemDto serviceItemDto);

    void delete(Long id);
}
