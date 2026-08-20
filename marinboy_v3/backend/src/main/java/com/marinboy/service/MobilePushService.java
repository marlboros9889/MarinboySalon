package com.marinboy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marinboy.mapper.DeviceTokenMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Expo 알림 서비스를 통해 Android FCM 및 iOS APNs 푸시를 전송합니다. */
@Service
public class MobilePushService {
    private static final Logger log = LoggerFactory.getLogger(MobilePushService.class);
    private static final URI EXPO_PUSH_URI = URI.create("https://exp.host/--/api/v2/push/send");
    private final DeviceTokenMapper mapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public MobilePushService(DeviceTokenMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void register(Long adminId, String pushToken, String platform) {
        if (pushToken == null || !pushToken.startsWith("ExponentPushToken[")) {
            throw new IllegalArgumentException("올바른 Expo 푸시 토큰이 아닙니다.");
        }
        String normalizedPlatform = platform == null ? "UNKNOWN" : platform.trim().toUpperCase();
        if (!normalizedPlatform.equals("ANDROID") && !normalizedPlatform.equals("IOS")) {
            throw new IllegalArgumentException("지원하지 않는 모바일 플랫폼입니다.");
        }
        mapper.save(adminId, pushToken, normalizedPlatform);
    }

    @Async
    public void sendNewReservation(Long adminId, String message, Long reservationId) {
        for (String pushToken : mapper.findByAdminId(adminId)) {
            try {
                String body = objectMapper.writeValueAsString(Map.of(
                        "to", pushToken,
                        "title", "새로운 시술 예약",
                        "body", message,
                        "sound", "default",
                        "data", Map.of("reservationId", reservationId)
                ));
                HttpRequest request = HttpRequest.newBuilder(EXPO_PUSH_URI)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 300) {
                    log.warn("모바일 예약 푸시 전송 실패: HTTP {}", response.statusCode());
                }
            } catch (Exception exception) {
                log.warn("모바일 예약 푸시 전송 실패: {}", exception.getMessage());
            }
        }
    }
}
