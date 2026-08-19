package com.marinboy.sse;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 관리자별 여러 브라우저 탭의 SSE 연결을 안전하게 관리합니다. */
@Component
public class SseEmitterManager {
    private static final long TIMEOUT_MILLIS = 10L * 60 * 1000;
    private final ConcurrentHashMap<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long adminId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        emitters.computeIfAbsent(adminId, key -> ConcurrentHashMap.newKeySet()).add(emitter);
        Runnable remove = () -> remove(adminId, emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(error -> remove.run());
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException exception) {
            remove.run();
        }
        return emitter;
    }

    public void send(Long adminId, Object data) {
        for (SseEmitter emitter : emitters.getOrDefault(adminId, Set.of())) {
            try {
                emitter.send(SseEmitter.event().name("newReservation").data(data));
            } catch (IOException | IllegalStateException exception) {
                remove(adminId, emitter);
            }
        }
    }

    private void remove(Long adminId, SseEmitter emitter) {
        Set<SseEmitter> adminEmitters = emitters.get(adminId);
        if (adminEmitters == null) return;
        adminEmitters.remove(emitter);
        if (adminEmitters.isEmpty()) emitters.remove(adminId, adminEmitters);
    }
}
