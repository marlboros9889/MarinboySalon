package com.marinboy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marinboy.dto.NotificationDto;
import com.marinboy.dto.UserDto;
import com.marinboy.security.SecurityConstants;
import com.marinboy.service.NotificationService;
import com.marinboy.sse.SseEmitterManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationControllerTest {
    private NotificationService notificationService;
    private SseEmitterManager emitterManager;
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        emitterManager = mock(SseEmitterManager.class);
        controller = new NotificationController(notificationService, emitterManager);
    }

    @Test
    void adminCanUseNotificationEndpointsWithOwnId() {
        MockHttpSession session = adminSession(12L);
        NotificationDto notification = new NotificationDto();
        SseEmitter emitter = new SseEmitter();
        when(notificationService.getUnreadCount(12L)).thenReturn(4);
        when(notificationService.getRecent(12L)).thenReturn(List.of(notification));
        when(emitterManager.subscribe(12L)).thenReturn(emitter);

        assertThat(controller.count(session)).isEqualTo(4);
        assertThat(controller.list(session)).containsExactly(notification);
        assertThat(controller.subscribe(session)).isSameAs(emitter);
        assertThat(controller.read(7L, session).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.readAll(session).getStatusCode().value()).isEqualTo(204);

        verify(notificationService).read(7L, 12L);
        verify(notificationService).readAll(12L);
    }

    @Test
    void customerCannotUseNotificationEndpoints() {
        MockHttpSession session = new MockHttpSession();
        UserDto customer = new UserDto();
        customer.setId(3L);
        customer.setRole(SecurityConstants.ROLE_CUSTOMER);
        session.setAttribute(SecurityConstants.LOGIN_USER, customer);

        assertThatThrownBy(() -> controller.count(session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("관리자 로그인이 필요합니다.");
    }

    private MockHttpSession adminSession(Long adminId) {
        MockHttpSession session = new MockHttpSession();
        UserDto admin = new UserDto();
        admin.setId(adminId);
        admin.setRole(SecurityConstants.ROLE_ADMIN);
        session.setAttribute(SecurityConstants.LOGIN_USER, admin);
        return session;
    }
}
