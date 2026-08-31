package com.marinboy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MarinboySalonApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applicationContextLoads() {
        // 관리자 기능이 추가된 전체 Spring 설정이 함께 시작되는지 확인합니다.
    }

    @Test
    void rejectsPostRequestWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptsPostRequestWithCsrfToken() throws Exception {
        mockMvc.perform(post("/user/logout").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
