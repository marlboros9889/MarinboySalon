package com.marinboy.controller;

import com.marinboy.dto.NotificationDto;
import com.marinboy.dto.UserDto;
import com.marinboy.service.AuthenticatedUserService;
import com.marinboy.service.NotificationService;
import com.marinboy.sse.SseEmitterManager;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 관리자 화면의 실시간 예약 알림 API를 제공합니다. */
@RestController
@RequestMapping("/api/admin/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final SseEmitterManager emitterManager;
    private final AuthenticatedUserService authenticatedUserService;

    public NotificationController(NotificationService notificationService, SseEmitterManager emitterManager,
            AuthenticatedUserService authenticatedUserService) {
        this.notificationService = notificationService;
        this.emitterManager = emitterManager;
        this.authenticatedUserService = authenticatedUserService;
    }

    //1. 관리자 화면 SSE 연결  GET: /api/admin/notifications/subscribe
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(Authentication authentication) {
        return emitterManager.subscribe(requireAdmin(authentication).getId());
    }

    //2. 안 읽은 알림 개수 조회  GET: /api/admin/notifications/count
    @GetMapping("/count")
    public int count(Authentication authentication) {
        return notificationService.getUnreadCount(requireAdmin(authentication).getId());
    }

    //3. 최근 알림 20건 조회  GET: /api/admin/notifications
    @GetMapping
    public List<NotificationDto> list(Authentication authentication) {
        return notificationService.getRecent(requireAdmin(authentication).getId());
    }

    //4. 선택 알림 읽음 처리  PATCH: /api/admin/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> read(@PathVariable Long id, Authentication authentication) {
        notificationService.read(id, requireAdmin(authentication).getId());
        return ResponseEntity.noContent().build();
    }

    //5. 전체 읽음 처리  POST: /api/admin/notifications/read-all
    @PostMapping("/read-all")
    public ResponseEntity<Void> readAll(Authentication authentication) {
        notificationService.readAll(requireAdmin(authentication).getId());
        return ResponseEntity.noContent().build();
    }

    private UserDto requireAdmin(Authentication authentication) {
        return authenticatedUserService.requireAdmin(authentication);
    }
}
