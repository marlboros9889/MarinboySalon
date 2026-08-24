package com.marinboy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marinboy.dto.UserDto;
import com.marinboy.mapper.AuthMapper;
import com.marinboy.security.oauth.SocialProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** 소셜 계정의 생성·재사용·검증 이메일 연결 규칙을 독립적으로 확인합니다. */
@ExtendWith(MockitoExtension.class)
class SocialAccountServiceTest {
    @Mock AuthMapper authMapper;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void createsSocialCustomerAndReturnsSavedDatabaseUser() {
        when(authMapper.findBySocialAccount("KAKAO", "12345"))
                .thenReturn(null)
                .thenReturn(savedUser());
        when(authMapper.findByEmail("social@example.com")).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded-social-password");
        when(authMapper.findByUsername(any())).thenReturn(savedUser());
        when(authMapper.insertSocialAccount(77L, "KAKAO", "12345")).thenReturn(1);
        SocialAccountService service = new SocialAccountService(authMapper, passwordEncoder);

        UserDto result = service.findOrCreate(
                new SocialProfile("kakao", "12345", "소셜고객", "SOCIAL@EXAMPLE.COM", null, true));

        ArgumentCaptor<UserDto> customer = ArgumentCaptor.forClass(UserDto.class);
        verify(authMapper).insertSocialCustomer(customer.capture());
        assertThat(customer.getValue().getLoginProvider()).isEqualTo("KAKAO");
        assertThat(customer.getValue().getPhone()).isEqualTo("SOCIAL_REQUIRED");
        assertThat(result.getId()).isEqualTo(77L);
    }

    @Test
    void returnsExistingSocialCustomerWithoutCreatingAnotherAccount() {
        UserDto existing = savedUser();
        when(authMapper.findBySocialAccount("NAVER", "naver-id")).thenReturn(existing);
        SocialAccountService service = new SocialAccountService(authMapper, passwordEncoder);

        UserDto result = service.findOrCreate(
                new SocialProfile("naver", "naver-id", "변경 이름", null, null, false));

        assertThat(result).isSameAs(existing);
        assertThat(result.getPassword()).isNull();
        verify(authMapper, never()).insertSocialCustomer(any());
    }

    @Test
    void linksVerifiedEmailToExistingCustomer() {
        UserDto existing = savedUser();
        UserDto linked = savedUser();
        linked.setLoginProvider("GOOGLE");
        when(authMapper.findBySocialAccount("GOOGLE", "google-id"))
                .thenReturn(null)
                .thenReturn(linked);
        when(authMapper.findByEmail("social@example.com")).thenReturn(existing);
        when(authMapper.insertSocialAccount(77L, "GOOGLE", "google-id")).thenReturn(1);
        SocialAccountService service = new SocialAccountService(authMapper, passwordEncoder);

        UserDto result = service.findOrCreate(
                new SocialProfile("google", "google-id", "소셜고객", "social@example.com", null, true));

        assertThat(result.getId()).isEqualTo(77L);
        verify(authMapper).insertSocialAccount(77L, "GOOGLE", "google-id");
    }

    @Test
    void rejectsUnverifiedEmailBeforeExistingAccountLink() {
        when(authMapper.findBySocialAccount("NAVER", "naver-id")).thenReturn(null);
        when(authMapper.findByEmail("social@example.com")).thenReturn(savedUser());
        SocialAccountService service = new SocialAccountService(authMapper, passwordEncoder);

        assertThatThrownBy(() -> service.findOrCreate(
                new SocialProfile("naver", "naver-id", "소셜고객", "social@example.com", null, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기존 계정으로 로그인");

        verify(authMapper, never()).insertSocialAccount(any(), any(), any());
    }

    private UserDto savedUser() {
        UserDto user = new UserDto();
        user.setId(77L);
        user.setUsername("social_user");
        user.setName("소셜고객");
        user.setEmail("social@example.com");
        user.setPhone("010-1234-5678");
        user.setRole("CUSTOMER");
        user.setLoginProvider("KAKAO");
        return user;
    }
}
