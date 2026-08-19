package com.marinboy.controller;

import com.marinboy.dto.UserDto;
import com.marinboy.security.FirebaseIdentityService;
import com.marinboy.service.MobilePushService;
import com.marinboy.service.NotificationService;
import com.marinboy.service.ReservationService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Firebase 로그인 관리자 앱에서 사용하는 모바일 전용 API입니다. */
@RestController
@RequestMapping("/api/mobile/admin")
public class MobileAdminController {
    private final FirebaseIdentityService identityService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    private final MobilePushService mobilePushService;

    public MobileAdminController(FirebaseIdentityService identityService, ReservationService reservationService,
            NotificationService notificationService, MobilePushService mobilePushService) {
        this.identityService = identityService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.mobilePushService = mobilePushService;
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader("Authorization") String authorization) {
        return identityService.requireAdmin(authorization);
    }

    @GetMapping("/reservations")
    public Object reservations(@RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        identityService.requireAdmin(authorization);
        return Map.of("items", reservationService.getReservationsPage(page, size),
                "total", reservationService.countReservations(), "page", page, "size", size);
    }

    @GetMapping("/notifications")
    public Object notifications(@RequestHeader("Authorization") String authorization) {
        UserDto admin = identityService.requireAdmin(authorization);
        return notificationService.getRecent(admin.getId());
    }

    @PostMapping("/devices")
    public ResponseEntity<Void> registerDevice(@RequestHeader("Authorization") String authorization,
            @RequestBody DeviceTokenRequest request) {
        UserDto admin = identityService.requireAdmin(authorization);
        mobilePushService.register(admin.getId(), request.pushToken(), request.platform());
        return ResponseEntity.noContent().build();
    }

    public record DeviceTokenRequest(String pushToken, String platform) { }
}
