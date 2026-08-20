package com.marinboy.dao;

import com.marinboy.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 사용자 로그인 정보를 Oracle에서 조회하는 MyBatis DAO입니다. */
@Mapper
public interface AuthDao {
    // username으로 사용자를 조회하며 비밀번호 검증은 AuthService에서 수행합니다.
    UserDto findByUsername(@Param("username") String username);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
