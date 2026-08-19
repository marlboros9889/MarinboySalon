package com.marinboy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 관리자 모바일 기기의 Expo/FCM 푸시 토큰을 저장합니다. */
@Mapper
public interface DeviceTokenMapper {
    int save(@Param("adminId") Long adminId, @Param("pushToken") String pushToken,
            @Param("platform") String platform);
    List<String> findByAdminId(@Param("adminId") Long adminId);
    int deleteByToken(@Param("pushToken") String pushToken);
}
