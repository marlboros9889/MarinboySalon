package com.marinboy.service;

import com.marinboy.dto.NotificationDto;
import com.marinboy.mapper.NotificationMapper;
import com.marinboy.sse.SseEmitterManager;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 예약 커밋 뒤 관리자별 DB·SSE·이메일 알림을 처리합니다. */
@Service
public class NotificationService {
    private final NotificationMapper mapper;
    private final SseEmitterManager emitterManager;
    private final ObjectProvider<NotificationMailService> mailService;
    private final MobilePushService mobilePushService;

    public NotificationService(NotificationMapper mapper, SseEmitterManager emitterManager,
            ObjectProvider<NotificationMailService> mailService, MobilePushService mobilePushService) {
        this.mapper = mapper;
        this.emitterManager = emitterManager;
        this.mailService = mailService;
        this.mobilePushService = mobilePushService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyNewReservation(ReservationCreatedEvent event) {
        String message = event.customerName() + "님이 " + event.serviceName() + " 예약을 신청했습니다.";
        mapper.findAdmins().forEach(admin -> {
            NotificationDto notification = new NotificationDto();
            notification.setReservationId(event.reservationId());
            notification.setAdminId(admin.getId());
            notification.setMessage(message);
            mapper.insertNotification(notification);
            int unreadCount = mapper.countUnread(admin.getId());
            emitterManager.send(admin.getId(), Map.of(
                    "message", message,
                    "reservationId", event.reservationId(),
                    "unreadCount", unreadCount
            ));
            NotificationMailService sender = mailService.getIfAvailable();
            if (sender != null) sender.send(admin.getEmail(), message);
            mobilePushService.sendNewReservation(admin.getId(), message, event.reservationId());
        });
    }

    public int getUnreadCount(Long adminId) { return mapper.countUnread(adminId); }
    public List<NotificationDto> getRecent(Long adminId) { return mapper.findRecent(adminId); }
    @Transactional public void read(Long notificationId, Long adminId) { mapper.markRead(notificationId, adminId); }
    @Transactional public void readAll(Long adminId) { mapper.markAllRead(adminId); }
}
