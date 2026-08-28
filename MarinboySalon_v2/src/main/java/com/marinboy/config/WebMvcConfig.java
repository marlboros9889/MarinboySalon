package com.marinboy.config;

import com.marinboy.config.interceptor.AdminAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 관리자 전용 경로에 AdminAuthInterceptor를 등록합니다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    public WebMvcConfig(AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns(
                        "/serviceItem/adminList",
                        "/serviceItem/insertForm",
                        "/serviceItem/insert",
                        "/serviceItem/updateForm",
                        "/serviceItem/update",
                        "/serviceItem/delete",
                        "/businessHour/**",
                        "/holiday/**",
                        "/reservation/adminList",
                        "/reservation/statusUpdate"
                );
    }
}
