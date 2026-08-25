package com.marinboy.user.tool;

import com.marinboy.user.dto.LoginUserDto;
import com.marinboy.user.dto.UserDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * 여러 Controller에서 반복되는 로그인 세션 저장과 조회를 담당합니다.
 */
@Component
public class LoginSessionTool {

    public static final String LOGIN_USER = "loginUser";

    /**
     * 비밀번호를 제외한 안전한 로그인 정보만 세션에 저장합니다.
     */
    public void saveLoginUser(HttpSession session, UserDto userDto) {
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setId(userDto.getId());
        loginUserDto.setEmail(userDto.getEmail());
        loginUserDto.setName(userDto.getName());
        loginUserDto.setRole(userDto.getRole());

        session.setAttribute(LOGIN_USER, loginUserDto);
    }

    public LoginUserDto getLoginUser(HttpSession session) {
        Object loginUserObject = session.getAttribute(LOGIN_USER);

        if (loginUserObject instanceof LoginUserDto) {
            return (LoginUserDto) loginUserObject;
        }

        return null;
    }

    public void clearLoginUser(HttpSession session) {
        session.invalidate();
    }
}
