package com.marinboy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marinboy.dto.NotificationDto;
import com.marinboy.dto.UserDto;
import com.marinboy.security.SecurityConstants;
import com.marinboy.service.AuthenticatedUserService;
import com.marinboy.service.NotificationService;
import com.marinboy.sse.SseEmitterManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationControllerTest {
    private NotificationService notificationService;
    private SseEmitterManager emitterManager;
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        emitterManager = mock(SseEmitterManager.class);
        controller = new NotificationController(notificationService, emitterManager, new AuthenticatedUserService());
    }

    @Test
    void adminCanUseNotificationEndpointsWithOwnId() {
        Authentication authentication = authentication(12L, SecurityConstants.ROLE_ADMIN);
        NotificationDto notification = new NotificationDto();
        SseEmitter emitter = new SseEmitter();
        when(notificationService.getUnreadCount(12L)).thenReturn(4);
        when(notificationService.getRecent(12L)).thenReturn(List.of(notification));
        when(emitterManager.subscribe(12L)).thenReturn(emitter);

        assertThat(controller.count(authentication)).isEqualTo(4);
        assertThat(controller.list(authentication)).containsExactly(notification);
        assertThat(controller.subscribe(authentication)).isSameAs(emitter);
        assertThat(controller.read(7L, authentication).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.readAll(authentication).getStatusCode().value()).isEqualTo(204);

        verify(notificationService).read(7L, 12L);
        verify(notificationService).readAll(12L);
    }

    @Test
    void customerCannotUseNotificationEndpoints() {
        Authentication authentication = authentication(3L, SecurityConstants.ROLE_CUSTOMER);
        assertThatThrownBy(() -> controller.count(authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("관리자 로그인이 필요합니다.");
    }

    private Authentication authentication(Long userId, String role) {
        UserDto user = new UserDto();
        user.setId(userId);
        user.setRole(role);
        return new UsernamePasswordAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
