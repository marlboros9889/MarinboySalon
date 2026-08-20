package com.marinboy.repository;

import com.marinboy.entity.ServiceItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** 단순 시술 메뉴 CRUD는 JPA Repository로 처리합니다. */
public interface ServiceItemRepository extends JpaRepository<ServiceItemEntity, Long> {
}
