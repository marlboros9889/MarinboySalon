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

    ServiceItemDto findById(Long id);
}
