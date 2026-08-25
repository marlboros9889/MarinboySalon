package com.marinboy.user.service;

import com.marinboy.user.dto.UserDto;

/**
 * 회원 기능에서 Controller가 사용할 작업 목록입니다.
 */
public interface UserService {

    void signup(UserDto userDto);

    UserDto login(String email, String password);

    UserDto getUser(Long id);

    void update(UserDto userDto);
}
