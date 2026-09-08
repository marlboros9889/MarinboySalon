package com.marinboy.user.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.marinboy.auth.service.AuthUserJwtService;
import com.marinboy.global.security.TokenStore;
import com.marinboy.user.repository.AppUserMapper;
import com.marinboy.user.service.AppUserService;

/** 예약 이력 보존형 계정 삭제 정책의 경계 조건을 확인합니다. */
class AdminUserControllerTest {

    @Test
    void allowsWithdrawalWhenOnlyCompletedOrCancelledHistoryExists() {
        AppUserMapper mapper = mock(AppUserMapper.class);
        AuthUserJwtService authUserJwtService = mock(AuthUserJwtService.class);
        TokenStore tokenStore = mock(TokenStore.class);
        Authentication authentication = mock(Authentication.class);
        when(authUserJwtService.getCurrentUserId(authentication)).thenReturn(1L);
        when(mapper.countRequestedReservations(2L)).thenReturn(0);
        when(mapper.withdrawById(2L)).thenReturn(1);
        AdminUserController controller = new AdminUserController(
                mapper, authUserJwtService, mock(AppUserService.class), tokenStore);

        controller.delete(2L, authentication);

        verify(mapper).deleteSocialAccountsByUserId(2L);
        verify(tokenStore).deleteRefreshToken("2");
        verify(mapper).withdrawById(2L);
    }

    @Test
    void blocksWithdrawalWhenRequestedReservationExists() {
        AppUserMapper mapper = mock(AppUserMapper.class);
        AuthUserJwtService authUserJwtService = mock(AuthUserJwtService.class);
        Authentication authentication = mock(Authentication.class);
        when(authUserJwtService.getCurrentUserId(authentication)).thenReturn(1L);
        when(mapper.countRequestedReservations(2L)).thenReturn(1);
        AdminUserController controller = new AdminUserController(
                mapper, authUserJwtService, mock(AppUserService.class), mock(TokenStore.class));

        assertThatThrownBy(() -> controller.delete(2L, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("접수 중인 예약이 있어 계정을 삭제할 수 없습니다.");
    }
}
