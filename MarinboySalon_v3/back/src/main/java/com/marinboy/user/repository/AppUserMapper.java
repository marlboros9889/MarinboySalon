package com.marinboy.user.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.marinboy.user.entity.AppUser;

/**
 * 회원 SQL과 연결되는 MyBatis Mapper입니다.
 */
public interface AppUserMapper {

    AppUser selectById(Long id);

    AppUser selectByEmail(String email);

    int insert(AppUser user);

    int update(AppUser user);

    int countByEmail(@Param("email") String email);

    List<AppUser> selectAll();

    int updateRole(@Param("id") Long id, @Param("role") String role);

    int countReservations(@Param("userId") Long userId);

    int deleteById(@Param("id") Long id);
}
