package com.marinboy.service;

import com.marinboy.mapper.AuthMapper;
import com.marinboy.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/** 로그인 입력값을 검증하고 사용자 계정을 조회하는 인증 서비스입니다. */
@Service
public class AuthService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    // 사용자 조회 SQL을 담당하는 DAO를 주입받습니다.
    public AuthService(AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
    }
    // 필수 입력값과 계정 일치 여부를 확인한 뒤 세션에 저장할 사용자를 반환합니다.
    @Transactional
    public UserDto login(String username, String password) {
        // null 입력은 DB 조회 전에 차단합니다.
        if (username == null || password == null) throw new IllegalArgumentException("아이디와 비밀번호를 입력하세요.");
        UserDto user = authMapper.findByUsername(username.trim());
        // 어떤 값이 틀렸는지 구분하지 않아 계정 정보 노출을 줄입니다.
        if (user == null || !passwordMatches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        user.setPassword(null);
        return user;
    }

    /** 고객 회원가입 정보를 검증하고 BCrypt 비밀번호로 일반 계정을 생성합니다. */
    @Transactional
    public void signup(UserDto request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())
                || isBlank(request.getName()) || isBlank(request.getEmail()) || isBlank(request.getPhone())) {
            throw new IllegalArgumentException("회원가입 정보를 모두 입력해 주세요.");
        }
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!isUsernameAvailable(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (!isEmailAvailable(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setRole("CUSTOMER");
        authMapper.insertCustomer(request);
        request.setPassword(null);
    }

    public boolean isUsernameAvailable(String username) {
        return !isBlank(username) && authMapper.countByUsername(username.trim()) == 0;
    }

    public boolean isEmailAvailable(String email) {
        return !isBlank(email) && authMapper.countByEmail(email.trim()) == 0;
    }

    /** 소셜 계정을 조회하거나 최초 로그인 고객 계정을 생성합니다. */
    @Transactional
    public UserDto findOrCreateSocialUser(String provider, String socialId, String name, String email, String phone) {
        if (isBlank(provider) || isBlank(socialId)) {
            throw new IllegalArgumentException("소셜 로그인 사용자 정보를 확인할 수 없습니다.");
        }
        String normalizedProvider = provider.trim().toUpperCase(Locale.ROOT);
        String normalizedSocialId = socialId.trim();
        UserDto existingUser = authMapper.findBySocialAccount(normalizedProvider, normalizedSocialId);
        if (existingUser != null) {
            existingUser.setPassword(null);
            return existingUser;
        }

        String accountHash = shortHash(normalizedProvider + ":" + normalizedSocialId);
        String normalizedEmail = isBlank(email)
                ? "social_" + accountHash + "@social.marinboy.local"
                : email.trim().toLowerCase(Locale.ROOT);
        UserDto emailUser = authMapper.findByEmail(normalizedEmail);
        if (emailUser != null) {
            // 동의받은 소셜 프로필 이메일로 기존 예약 계정을 이어 붙여 중복 고객 생성을 막습니다.
            int linkedCount = authMapper.linkSocialAccount(
                    emailUser.getId(), normalizedProvider, normalizedSocialId);
            if (linkedCount != 1) {
                throw new IllegalArgumentException("이 이메일은 다른 소셜 계정과 연결되어 있습니다.");
            }
            UserDto linkedUser = authMapper.findBySocialAccount(normalizedProvider, normalizedSocialId);
            if (linkedUser == null) {
                throw new IllegalStateException("기존 계정에 소셜 로그인을 연결하지 못했습니다.");
            }
            linkedUser.setDisplayName(linkedUser.getName());
            linkedUser.setPassword(null);
            return linkedUser;
        }

        UserDto socialUser = new UserDto();
        socialUser.setUsername(normalizedProvider.toLowerCase(Locale.ROOT) + "_" + accountHash);
        socialUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        socialUser.setName(isBlank(name) ? normalizedProvider + " 고객" : name.trim());
        socialUser.setEmail(normalizedEmail);
        socialUser.setPhone(isBlank(phone) ? "SOCIAL_REQUIRED" : phone.trim());
        socialUser.setRole("CUSTOMER");
        socialUser.setLoginProvider(normalizedProvider);
        socialUser.setSocialId(normalizedSocialId);
        authMapper.insertSocialCustomer(socialUser);
        UserDto savedUser = authMapper.findBySocialAccount(normalizedProvider, normalizedSocialId);
        if (savedUser == null || savedUser.getId() == null) {
            throw new IllegalStateException("소셜 로그인 고객 계정을 저장하지 못했습니다.");
        }
        savedUser.setDisplayName(savedUser.getName());
        savedUser.setPassword(null);
        return savedUser;
    }

    /** 로그인 고객이 연락처 정보를 바꾸면 과거·진행 예약의 안내 정보도 같은 값으로 유지합니다. */
    @Transactional
    public UserDto updateProfile(UserDto current, UserDto request) {
        if (current == null || current.getId() == null || request == null
                || isBlank(request.getName()) || isBlank(request.getEmail()) || isBlank(request.getPhone())) {
            throw new IllegalArgumentException("이름, 이메일, 연락처를 모두 입력하세요.");
        }
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (authMapper.countByEmailExceptId(email, current.getId()) > 0) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        current.setName(request.getName().trim());
        current.setEmail(email);
        current.setPhone(request.getPhone().trim());
        authMapper.updateProfile(current);
        authMapper.updateReservationContact(current);
        return current;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        // 예약 과정에서 생성된 guest 계정과 평문 레거시 값은 로그인 경로에서 항상 차단합니다.
        return storedPassword != null
                && storedPassword.startsWith("$2")
                && passwordEncoder.matches(rawPassword, storedPassword);
    }

    private String shortHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 24);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("소셜 계정 식별자를 만들 수 없습니다.", exception);
        }
    }
}
