package com.marinboy.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import com.marinboy.user.dto.UserDto;
import com.marinboy.user.service.UserService;
import com.marinboy.user.tool.LoginSessionTool;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** 로그인 성공 시 기존 세션 대신 새 세션을 사용하는지 확인합니다. */
class UserControllerLoginTest {

    @Test
    void replacesSessionAfterSuccessfulLogin() {
        UserService userService = mock(UserService.class);
        LoginSessionTool loginSessionTool = mock(LoginSessionTool.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession oldSession = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);
        Model model = mock(Model.class);
        UserDto user = new UserDto();
        user.setId(1L);
        user.setEmail("user@example.com");

        when(userService.login("user@example.com", "password")).thenReturn(user);
        when(request.getSession(false)).thenReturn(oldSession);
        when(request.getSession(true)).thenReturn(newSession);

        UserController controller = new UserController(userService, loginSessionTool);
        String viewName = controller.login(
                "user@example.com", "password", "/reservation/list", request, model);

        verify(oldSession).invalidate();
        verify(loginSessionTool).saveLoginUser(newSession, user);
        assertThat(viewName).isEqualTo("redirect:/reservation/list");
    }
}
