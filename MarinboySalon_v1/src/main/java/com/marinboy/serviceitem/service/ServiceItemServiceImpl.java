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
    public ServiceItemDto getServiceItem(Long id) {
        ServiceItemDto serviceItemDto = serviceItemDao.findById(id);

        if (serviceItemDto == null || !serviceItemDto.isActive()) {
            throw new IllegalArgumentException("예약할 수 없는 시술입니다.");
        }

        return serviceItemDto;
    }
}
