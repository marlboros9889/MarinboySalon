package com.marinboy.config.interceptor;

import com.marinboy.user.tool.LoginSessionTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 관리자 URL에 대한 서버 측 권한 검사를 한 곳에서 수행합니다.
 * Controller의 isAdmin 검사와 이중으로 막아 직접 URL 접근을 차단합니다.
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final LoginSessionTool loginSessionTool;

    public AdminAuthInterceptor(LoginSessionTool loginSessionTool) {
        this.loginSessionTool = loginSessionTool;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (loginSessionTool.isAdmin(request.getSession(false))) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + "/user/loginForm");
        return false;
    }
}
