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

    /**
     * 관리자 역할 여부. v1에는 관리자 화면이 없어도 role 필드를 일관되게 다룹니다.
     */
    public boolean isAdmin(HttpSession session) {
        LoginUserDto loginUserDto = getLoginUser(session);
        return loginUserDto != null && "ADMIN".equals(loginUserDto.getRole());
    }
}
