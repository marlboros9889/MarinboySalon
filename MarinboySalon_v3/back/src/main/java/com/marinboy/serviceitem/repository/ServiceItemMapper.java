package com.marinboy.serviceitem.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.marinboy.serviceitem.entity.ServiceItem;
import com.marinboy.serviceitem.entity.ServiceItemImage;

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

    List<ServiceItemImage> selectImagesByServiceItemIds(
            @Param("serviceItemIds") List<Long> serviceItemIds);

    int insertImage(
            @Param("serviceItemId") Long serviceItemId,
            @Param("imageUrl") String imageUrl,
            @Param("displayOrder") Integer displayOrder);

    int deleteImagesByServiceItemId(Long serviceItemId);
}
