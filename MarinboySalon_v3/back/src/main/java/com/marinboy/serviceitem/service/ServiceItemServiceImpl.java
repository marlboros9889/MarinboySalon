package com.marinboy.serviceitem.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.serviceitem.dto.request.ServiceItemRequestDto;
import com.marinboy.serviceitem.dto.response.ServiceItemResponseDto;
import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.entity.ServiceItemImage;
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
        List<ServiceItem> items = new ArrayList<>();
        items.add(item);
        return toResponseList(items).get(0);
    }

    @Override
    public ServiceItemResponseDto insert(ServiceItemRequestDto request) {
        ServiceItem item = new ServiceItem();
        copyRequest(item, request);
        item.setActive(true);
        serviceItemMapper.insert(item);
        replaceImages(item.getId(), request.getImageUrls());
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
        replaceImages(id, request.getImageUrls());
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
        Map<Long, List<String>> imageUrlsByServiceItemId = loadImageUrls(items);
        for (ServiceItem item : items) {
            List<String> imageUrls = imageUrlsByServiceItemId.getOrDefault(item.getId(), new ArrayList<>());
            responses.add(ServiceItemResponseDto.from(item, imageUrls));
        }
        return responses;
    }

    /** 메뉴 목록을 조회할 때 이미지도 한 번의 쿼리로 묶어서 가져옵니다. */
    private Map<Long, List<String>> loadImageUrls(List<ServiceItem> items) {
        Map<Long, List<String>> imageUrlsByServiceItemId = new LinkedHashMap<>();
        if (items.isEmpty()) {
            return imageUrlsByServiceItemId;
        }

        List<Long> serviceItemIds = new ArrayList<>();
        for (ServiceItem item : items) {
            serviceItemIds.add(item.getId());
        }

        List<ServiceItemImage> images = serviceItemMapper.selectImagesByServiceItemIds(serviceItemIds);
        for (ServiceItemImage image : images) {
            List<String> imageUrls = imageUrlsByServiceItemId.get(image.getServiceItemId());
            if (imageUrls == null) {
                imageUrls = new ArrayList<>();
                imageUrlsByServiceItemId.put(image.getServiceItemId(), imageUrls);
            }
            imageUrls.add(image.getImageUrl());
        }
        return imageUrlsByServiceItemId;
    }

    /** 빈 값과 중복을 제거한 뒤 표시 순서대로 최대 4장을 저장합니다. */
    private void replaceImages(Long serviceItemId, List<String> requestedImageUrls) {
        if (requestedImageUrls == null) {
            return;
        }

        Set<String> normalizedImageUrls = new LinkedHashSet<>();
        for (String imageUrl : requestedImageUrls) {
            normalizedImageUrls.add(imageUrl.trim());
        }
        if (normalizedImageUrls.size() > 4) {
            throw new IllegalArgumentException("메뉴 이미지는 최대 4개까지 등록할 수 있습니다.");
        }

        serviceItemMapper.deleteImagesByServiceItemId(serviceItemId);
        int displayOrder = 0;
        for (String imageUrl : normalizedImageUrls) {
            serviceItemMapper.insertImage(serviceItemId, imageUrl, displayOrder);
            displayOrder++;
        }
    }
}
