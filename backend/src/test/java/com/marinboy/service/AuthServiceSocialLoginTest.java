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
        // 처음 로그인한 소셜 고객은 임시 연락처로 저장한 뒤 DB에서 다시 읽어 식별자를 확정합니다.
        when(authMapper.findBySocialAccount("KAKAO", "12345"))
                .thenReturn(null)
                .thenReturn(savedUser());
        when(authMapper.findByEmail("social@example.com")).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded-social-password");
        when(authMapper.findByUsername(any())).thenReturn(savedUser());
        when(authMapper.insertSocialAccount(77L, "KAKAO", "12345")).thenReturn(1);
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
        // 이미 연결된 제공자 계정은 고객 행이나 연결 행을 중복 생성하지 않아야 합니다.
        UserDto existing = savedUser();
        when(authMapper.findBySocialAccount("NAVER", "naver-id")).thenReturn(existing);
        AuthService service = new AuthService(authMapper, passwordEncoder);

        UserDto result = service.findOrCreateSocialUser("naver", "naver-id", "변경 이름", null, null);

        assertThat(result).isSameAs(existing);
        assertThat(result.getPassword()).isNull();
    }

    @Test
    void linksNaverProfileToExistingAccountWithTheSameEmail() {
        // 같은 이메일의 기존 고객이 있으면 새 고객 대신 소셜 연결만 추가하는지 확인합니다.
        UserDto existing = savedUser();
        existing.setLoginProvider(null);
        UserDto linked = savedUser();
        linked.setLoginProvider("NAVER");
        when(authMapper.findBySocialAccount("NAVER", "naver-new-id"))
                .thenReturn(null)
                .thenReturn(linked);
        when(authMapper.findByEmail("social@example.com")).thenReturn(existing);
        when(authMapper.insertSocialAccount(77L, "NAVER", "naver-new-id")).thenReturn(1);
        AuthService service = new AuthService(authMapper, passwordEncoder);

        UserDto result = service.findOrCreateSocialUser(
                "naver", "naver-new-id", "소셜고객", "social@example.com", "010-1234-5678");

        assertThat(result.getId()).isEqualTo(77L);
        assertThat(result.getLoginProvider()).isEqualTo("NAVER");
        verify(authMapper).insertSocialAccount(77L, "NAVER", "naver-new-id");
    }

    @Test
    void linksGoogleAndNaverProfilesToTheSameCustomer() {
        // 한 고객이 여러 소셜 제공자를 사용해도 하나의 고객 ID를 유지해야 합니다.
        UserDto existing = savedUser();
        UserDto linked = savedUser();
        linked.setLoginProvider("NAVER");
        when(authMapper.findBySocialAccount("NAVER", "naver-id"))
                .thenReturn(null)
                .thenReturn(linked);
        when(authMapper.findByEmail("social@example.com")).thenReturn(existing);
        when(authMapper.findSocialIdByUserAndProvider(77L, "NAVER")).thenReturn(null);
        when(authMapper.insertSocialAccount(77L, "NAVER", "naver-id")).thenReturn(1);
        AuthService service = new AuthService(authMapper, passwordEncoder);

        UserDto result = service.findOrCreateSocialUser(
                "naver", "naver-id", "소셜고객", "social@example.com", null);

        assertThat(result.getId()).isEqualTo(77L);
        verify(authMapper).insertSocialAccount(77L, "NAVER", "naver-id");
    }

    private UserDto savedUser() {
        // 각 테스트가 계정 연결 규칙에 집중할 수 있도록 공통 고객 데이터를 준비합니다.
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
