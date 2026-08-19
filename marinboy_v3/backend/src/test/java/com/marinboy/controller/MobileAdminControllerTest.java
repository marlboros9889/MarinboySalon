package com.marinboy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marinboy.dto.NotificationDto;
import com.marinboy.dto.UserDto;
import com.marinboy.security.FirebaseIdentityService;
import com.marinboy.service.MobilePushService;
import com.marinboy.service.NotificationService;
import com.marinboy.service.ReservationService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MobileAdminControllerTest {

    @Test
    void firebaseAdminCanReadDashboardAndRegisterDevice() {
        FirebaseIdentityService identity = mock(FirebaseIdentityService.class);
        ReservationService reservations = mock(ReservationService.class);
        NotificationService notifications = mock(NotificationService.class);
        MobilePushService push = mock(MobilePushService.class);
        MobileAdminController controller = new MobileAdminController(identity, reservations, notifications, push);
        UserDto admin = new UserDto();
        admin.setId(8L);
        when(identity.requireAdmin("Bearer valid")).thenReturn(admin);
        when(reservations.getReservationsPage(0, 20)).thenReturn(List.of());
        when(reservations.countReservations()).thenReturn(0);
        when(notifications.getRecent(8L)).thenReturn(List.of(new NotificationDto()));

        assertThat(controller.reservations("Bearer valid", 0, 20)).isEqualTo(
                Map.of("items", List.of(), "total", 0, "page", 0, "size", 20));
        assertThat((List<?>) controller.notifications("Bearer valid")).hasSize(1);
        assertThat(controller.registerDevice("Bearer valid",
                new MobileAdminController.DeviceTokenRequest("ExponentPushToken[test]", "android"))
                .getStatusCode().value()).isEqualTo(204);
        verify(push).register(8L, "ExponentPushToken[test]", "android");
    }
}
