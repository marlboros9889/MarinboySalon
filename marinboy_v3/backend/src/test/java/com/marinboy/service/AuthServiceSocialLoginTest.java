package com.marinboy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marinboy.dto.UserDto;
import com.marinboy.mapper.AuthMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** 소셜 사용자를 일반 비밀번호 계정과 섞지 않고 생성·재조회하는지 검증합니다. */
@ExtendWith(MockitoExtension.class)
class AuthServiceSocialLoginTest {
    @Mock AuthMapper authMapper;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void createsSocialCustomerAndReturnsSavedDatabaseUser() {
        when(authMapper.findBySocialAccount("KAKAO", "12345"))
                .thenReturn(null)
                .thenReturn(savedUser());
        when(authMapper.countByEmail("social@example.com")).thenReturn(0);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded-social-password");
        AuthService service = new AuthService(authMapper, passwordEncoder);

        UserDto result = service.findOrCreateSocialUser(
                "kakao", "12345", "소셜고객", "SOCIAL@EXAMPLE.COM", null);

        ArgumentCaptor<UserDto> customer = ArgumentCaptor.forClass(UserDto.class);
        verify(authMapper).insertSocialCustomer(customer.capture());
        assertThat(customer.getValue().getLoginProvider()).isEqualTo("KAKAO");
        assertThat(customer.getValue().getSocialId()).isEqualTo("12345");
        assertThat(customer.getValue().getPhone()).isEqualTo("SOCIAL_REQUIRED");
        assertThat(result.getId()).isEqualTo(77L);
    }

    @Test
    void returnsExistingSocialCustomerWithoutCreatingAnotherAccount() {
        UserDto existing = savedUser();
        when(authMapper.findBySocialAccount("NAVER", "naver-id")).thenReturn(existing);
        AuthService service = new AuthService(authMapper, passwordEncoder);

        UserDto result = service.findOrCreateSocialUser("naver", "naver-id", "변경 이름", null, null);

        assertThat(result).isSameAs(existing);
        assertThat(result.getPassword()).isNull();
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
