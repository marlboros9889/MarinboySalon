package com.marinboy.serviceitem.repository;

import java.util.List;

import com.marinboy.serviceitem.entity.ServiceItem;

/**
 * 시술 항목 SQL과 연결되는 MyBatis Mapper입니다.
 */
public interface ServiceItemMapper {

    List<ServiceItem> selectActiveAll();

    List<ServiceItem> selectAll();

    ServiceItem selectById(Long id);

    int insert(ServiceItem item);

    int update(ServiceItem item);

    int deactivate(Long id);
}
