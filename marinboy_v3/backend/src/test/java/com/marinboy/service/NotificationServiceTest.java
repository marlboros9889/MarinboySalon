package com.marinboy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marinboy.dto.NotificationDto;
import com.marinboy.dto.UserDto;
import com.marinboy.mapper.NotificationMapper;
import com.marinboy.sse.SseEmitterManager;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

class NotificationServiceTest {
    private NotificationMapper mapper;
    private SseEmitterManager emitterManager;
    private NotificationMailService mailService;
    private MobilePushService mobilePushService;
    private NotificationService notificationService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mapper = mock(NotificationMapper.class);
        emitterManager = mock(SseEmitterManager.class);
        mailService = mock(NotificationMailService.class);
        mobilePushService = mock(MobilePushService.class);
        ObjectProvider<NotificationMailService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mailService);
        notificationService = new NotificationService(mapper, emitterManager, provider, mobilePushService);
    }

    @Test
    void createsAndSendsOneNotificationForEachAdmin() {
        UserDto firstAdmin = admin(1L, "first@marinboy.test");
        UserDto secondAdmin = admin(2L, "second@marinboy.test");
        when(mapper.findAdmins()).thenReturn(List.of(firstAdmin, secondAdmin));
        when(mapper.countUnread(1L)).thenReturn(3);
        when(mapper.countUnread(2L)).thenReturn(5);

        notificationService.notifyNewReservation(
                new ReservationCreatedEvent(77L, "김고객", "레이어드 컷"));

        ArgumentCaptor<NotificationDto> notificationCaptor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(mapper, org.mockito.Mockito.times(2)).insertNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getAllValues())
                .extracting(NotificationDto::getAdminId)
                .containsExactly(1L, 2L);
        assertThat(notificationCaptor.getAllValues())
                .allSatisfy(notification -> {
                    assertThat(notification.getReservationId()).isEqualTo(77L);
                    assertThat(notification.getMessage()).isEqualTo("김고객님이 레이어드 컷 예약을 신청했습니다.");
                });

        verify(emitterManager).send(eq(1L), eq(Map.of(
                "message", "김고객님이 레이어드 컷 예약을 신청했습니다.",
                "reservationId", 77L,
                "unreadCount", 3)));
        verify(emitterManager).send(eq(2L), eq(Map.of(
                "message", "김고객님이 레이어드 컷 예약을 신청했습니다.",
                "reservationId", 77L,
                "unreadCount", 5)));
        verify(mailService).send("first@marinboy.test", "김고객님이 레이어드 컷 예약을 신청했습니다.");
        verify(mailService).send("second@marinboy.test", "김고객님이 레이어드 컷 예약을 신청했습니다.");
        verify(mobilePushService).sendNewReservation(1L, "김고객님이 레이어드 컷 예약을 신청했습니다.", 77L);
        verify(mobilePushService).sendNewReservation(2L, "김고객님이 레이어드 컷 예약을 신청했습니다.", 77L);
    }

    @Test
    void delegatesAdminScopedReadOperationsToMapper() {
        NotificationDto recent = new NotificationDto();
        when(mapper.countUnread(9L)).thenReturn(2);
        when(mapper.findRecent(9L)).thenReturn(List.of(recent));

        assertThat(notificationService.getUnreadCount(9L)).isEqualTo(2);
        assertThat(notificationService.getRecent(9L)).containsExactly(recent);
        notificationService.read(31L, 9L);
        notificationService.readAll(9L);

        verify(mapper).markRead(31L, 9L);
        verify(mapper).markAllRead(9L);
    }

    @Test
    void reservationNotificationRunsAfterCommitInANewTransaction() throws NoSuchMethodException {
        Method listener = NotificationService.class.getMethod(
                "notifyNewReservation", ReservationCreatedEvent.class);

        assertThat(listener.getAnnotation(TransactionalEventListener.class).phase())
                .isEqualTo(TransactionPhase.AFTER_COMMIT);
        assertThat(listener.getAnnotation(Transactional.class).propagation())
                .isEqualTo(Propagation.REQUIRES_NEW);
    }

    private UserDto admin(Long id, String email) {
        UserDto admin = new UserDto();
        admin.setId(id);
        admin.setEmail(email);
        return admin;
    }
}
