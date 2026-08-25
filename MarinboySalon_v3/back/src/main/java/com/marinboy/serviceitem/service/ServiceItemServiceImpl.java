package com.marinboy.serviceitem.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.serviceitem.dto.request.ServiceItemRequestDto;
import com.marinboy.serviceitem.dto.response.ServiceItemResponseDto;
import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.repository.ServiceItemMapper;

import lombok.RequiredArgsConstructor;

/**
 * 시술 항목 조회와 관리자 CRUD를 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceItemResponseDto> getActiveList() {
        List<ServiceItem> items = serviceItemMapper.selectActiveAll();
        return toResponseList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceItemResponseDto> getAdminList() {
        return toResponseList(serviceItemMapper.selectAll());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceItemResponseDto getDetail(Long id) {
        ServiceItem item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("시술 항목을 찾을 수 없습니다.");
        }
        return ServiceItemResponseDto.from(item);
    }

    @Override
    public ServiceItemResponseDto insert(ServiceItemRequestDto request) {
        ServiceItem item = new ServiceItem();
        copyRequest(item, request);
        item.setActive(true);
        serviceItemMapper.insert(item);
        return getDetail(item.getId());
    }

    @Override
    public ServiceItemResponseDto update(Long id, ServiceItemRequestDto request) {
        ServiceItem item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("시술 항목을 찾을 수 없습니다.");
        }
        copyRequest(item, request);
        if (request.getActive() != null) {
            item.setActive(request.getActive());
        }
        serviceItemMapper.update(item);
        return getDetail(id);
    }

    @Override
    public void delete(Long id) {
        getDetail(id);
        serviceItemMapper.deactivate(id);
    }

    private void copyRequest(ServiceItem item, ServiceItemRequestDto request) {
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setDurationMinutes(request.getDurationMinutes());
        item.setDescription(request.getDescription());
    }

    private List<ServiceItemResponseDto> toResponseList(List<ServiceItem> items) {
        List<ServiceItemResponseDto> responses = new ArrayList<>();
        for (ServiceItem item : items) {
            responses.add(ServiceItemResponseDto.from(item));
        }
        return responses;
    }
}
