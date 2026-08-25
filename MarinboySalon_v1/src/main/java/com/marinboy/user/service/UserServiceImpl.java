package com.marinboy.user.service;

import com.marinboy.user.dao.UserDao;
import com.marinboy.user.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 가입, 로그인, 정보 수정의 실제 처리 순서를 담당합니다.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 이메일 중복을 확인하고 비밀번호를 암호화한 뒤 회원을 저장합니다.
     */
    @Override
    @Transactional
    public void signup(UserDto userDto) {
        int emailCount = userDao.countByEmail(userDto.getEmail());

        if (emailCount > 0) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);
        userDto.setRole("CUSTOMER");

        userDao.insert(userDto);
    }

    /**
     * 이메일로 회원을 찾고 입력 비밀번호와 암호화된 비밀번호를 비교합니다.
     */
    @Override
    public UserDto login(String email, String password) {
        UserDto userDto = userDao.findByEmail(email);

        if (userDto == null) {
            return null;
        }

        boolean passwordMatches = passwordEncoder.matches(password, userDto.getPassword());
        if (!passwordMatches) {
            return null;
        }

        return userDto;
    }

    @Override
    public UserDto getUser(Long id) {
        return userDao.findById(id);
    }

    /**
     * Controller에서 로그인 회원 id를 넣어 전달하므로 본인 정보만 수정됩니다.
     */
    @Override
    @Transactional
    public void update(UserDto userDto) {
        int updatedCount = userDao.update(userDto);

        if (updatedCount == 0) {
            throw new IllegalArgumentException("수정할 회원을 찾을 수 없습니다.");
        }
    }
}
