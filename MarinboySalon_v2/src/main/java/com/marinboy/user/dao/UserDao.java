package com.marinboy.user.dao;

import com.marinboy.user.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * 회원 SQL을 UserMapper.xml과 연결합니다.
 */
@Mapper
public interface UserDao {

    int countByEmail(String email);

    int insert(UserDto userDto);

    UserDto findByEmail(String email);

    UserDto findById(Long id);

    int update(UserDto userDto);
}
