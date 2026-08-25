package com.marinboy.serviceitem.service;

import com.marinboy.serviceitem.dao.ServiceItemDao;
import com.marinboy.serviceitem.dto.ServiceItemDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 고객에게 공개할 시술 메뉴 조회를 담당합니다.
 */
@Service
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemDao serviceItemDao;

    public ServiceItemServiceImpl(ServiceItemDao serviceItemDao) {
        this.serviceItemDao = serviceItemDao;
    }

    @Override
    public List<ServiceItemDto> getActiveList() {
        return serviceItemDao.findActiveList();
    }

    @Override
    public List<ServiceItemDto> getList() {
        return serviceItemDao.findAll();
    }

    @Override
    public ServiceItemDto getServiceItem(Long id) {
        ServiceItemDto serviceItemDto = serviceItemDao.findById(id);

        if (serviceItemDto == null) {
            throw new IllegalArgumentException("시술 메뉴를 찾을 수 없습니다.");
        }

        return serviceItemDto;
    }

    /**
     * 가격과 소요 시간을 검사한 뒤 새 시술 메뉴를 등록합니다.
     */
    @Override
    public void insert(ServiceItemDto serviceItemDto) {
        validateServiceItem(serviceItemDto);
        serviceItemDao.insert(serviceItemDto);
    }

    @Override
    public void update(ServiceItemDto serviceItemDto) {
        validateServiceItem(serviceItemDto);

        int updatedCount = serviceItemDao.update(serviceItemDto);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("수정할 시술 메뉴를 찾을 수 없습니다.");
        }
    }

    @Override
    public void delete(Long id) {
        int deletedCount = serviceItemDao.delete(id);

        if (deletedCount == 0) {
            throw new IllegalArgumentException("비활성화할 시술 메뉴를 찾을 수 없습니다.");
        }
    }

    private void validateServiceItem(ServiceItemDto serviceItemDto) {
        if (serviceItemDto.getName() == null || serviceItemDto.getName().isBlank()) {
            throw new IllegalArgumentException("시술명을 입력해 주세요.");
        }

        if (serviceItemDto.getPrice() < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }

        if (serviceItemDto.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("소요 시간은 1분 이상이어야 합니다.");
        }
    }
}
