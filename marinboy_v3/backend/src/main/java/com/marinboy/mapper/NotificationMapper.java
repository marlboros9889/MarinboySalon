package com.marinboy.mapper;

import com.marinboy.dto.NotificationDto;
import com.marinboy.dto.UserDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 관리자별 예약 알림을 저장하고 조회하는 MyBatis Mapper입니다. */
@Mapper
public interface NotificationMapper {
    List<UserDto> findAdmins();
    int insertNotification(NotificationDto notification);
    int countUnread(@Param("adminId") Long adminId);
    List<NotificationDto> findRecent(@Param("adminId") Long adminId);
    int markRead(@Param("notificationId") Long notificationId, @Param("adminId") Long adminId);
    int markAllRead(@Param("adminId") Long adminId);
}
