package com.marinboy.dao;

import com.marinboy.dto.ServiceDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 시술 메뉴와 이미지 정보를 조회하는 MyBatis DAO입니다.
@Mapper
public interface SalonServiceDao {

    // 고객 화면에 표시할 전체 시술 메뉴를 조회합니다.
    List<ServiceDto> findAllServices();

    // 시술별 대표 이미지와 추가 이미지를 조회합니다.
    List<ServiceDto> findAllServiceImages();

    // 예약 가능 시간 계산에 사용할 시술 소요 시간을 조회합니다.
    Integer findDurationMinutesById(@Param("serviceId") Long serviceId);
    // 관리자가 입력한 시술 메뉴 기본 정보를 저장합니다.
    int insertService(ServiceDto service);
    // 기존 시술 메뉴의 이름, 분류, 시간, 가격과 설명을 수정합니다.
    int updateService(@Param("id") Long id, @Param("name") String name, @Param("category") String category, @Param("durationMinutes") int durationMinutes, @Param("price") int price, @Param("description") String description);
    // 대표 이미지 교체 전에 기존 대표 이미지 행을 제거합니다.
    int deleteRepresentativeImage(@Param("serviceId") Long serviceId);
    // 업로드된 대표 이미지의 웹 경로를 저장합니다.
    int insertRepresentativeImage(@Param("serviceId") Long serviceId, @Param("imageUrl") String imageUrl);
    // 수정 시 새 묶음이 전달되면 기존 상세 이미지 묶음을 교체합니다.
    int deleteDetailImages(@Param("serviceId") Long serviceId);
    // 상세 이미지를 관리자가 선택한 순서와 함께 저장합니다.
    int insertDetailImage(@Param("serviceId") Long serviceId, @Param("imageUrl") String imageUrl, @Param("sortOrder") int sortOrder);
    // 예약 이력은 유지하면서 메뉴를 논리 삭제 상태로 변경합니다.
    int deleteService(@Param("id") Long id);
}
