package com.marinboy.serviceitem.dao;

import com.marinboy.serviceitem.dto.ServiceItemDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 시술 메뉴 SQL을 ServiceMapper.xml과 연결합니다.
 */
@Mapper
public interface ServiceItemDao {

    List<ServiceItemDto> findActiveList();

    List<ServiceItemDto> findAll();

    ServiceItemDto findById(Long id);

    int insert(ServiceItemDto serviceItemDto);

    int update(ServiceItemDto serviceItemDto);

    int delete(Long id);
}
