package com.marinboy.service;

import com.marinboy.dto.UserDto;
import com.marinboy.mapper.AuthMapper;
import com.marinboy.security.oauth.SocialProfile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 소셜 프로필을 고객 계정에 안전하게 연결하는 전용 서비스입니다. */
@Service
public class SocialAccountService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public SocialAccountService(AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /** 제공자 계정을 재사용하거나 검증된 이메일의 기존 고객에게만 연결합니다. */
    @Transactional
    public UserDto findOrCreate(SocialProfile profile) {
        String provider = profile == null ? null : profile.provider();
        String socialId = profile == null ? null : profile.socialId();
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

        String providedEmail = normalizeEmail(profile.email());
        UserDto emailUser = providedEmail == null ? null : authMapper.findByEmail(providedEmail);
        if (emailUser != null) {
            return linkVerifiedEmail(profile, normalizedProvider, normalizedSocialId, emailUser);
        }
        return createSocialCustomer(profile, normalizedProvider, normalizedSocialId, providedEmail);
    }

    private UserDto linkVerifiedEmail(SocialProfile profile, String provider, String socialId, UserDto emailUser) {
        if (!profile.emailVerified()) {
            throw new IllegalArgumentException("같은 이메일의 기존 계정이 있습니다. 기존 계정으로 로그인한 뒤 연결해 주세요.");
        }
        String linkedSocialId = authMapper.findSocialIdByUserAndProvider(emailUser.getId(), provider);
        if (linkedSocialId != null) {
            throw new IllegalArgumentException("이 이메일은 다른 " + provider + " 계정과 연결되어 있습니다.");
        }
        if (authMapper.insertSocialAccount(emailUser.getId(), provider, socialId) != 1) {
            throw new IllegalArgumentException("이 이메일은 다른 소셜 계정과 연결되어 있습니다.");
        }

        UserDto linkedUser = authMapper.findBySocialAccount(provider, socialId);
        if (linkedUser == null) {
            throw new IllegalStateException("기존 계정에 소셜 로그인을 연결하지 못했습니다.");
        }
        return prepareResponse(linkedUser);
    }

    private UserDto createSocialCustomer(
            SocialProfile profile, String provider, String socialId, String providedEmail) {
        String accountHash = shortHash(provider + ":" + socialId);
        String savedEmail = profile.emailVerified() && providedEmail != null
                ? providedEmail
                : "social_" + accountHash + "@social.marinboy.local";

        UserDto socialUser = new UserDto();
        socialUser.setUsername(provider.toLowerCase(Locale.ROOT) + "_" + accountHash);
        socialUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        socialUser.setName(isBlank(profile.name()) ? provider + " 고객" : profile.name().trim());
        socialUser.setEmail(savedEmail);
        socialUser.setPhone(isBlank(profile.phone()) ? "SOCIAL_REQUIRED" : profile.phone().trim());
        socialUser.setRole("CUSTOMER");
        socialUser.setLoginProvider(provider);
        socialUser.setSocialId(socialId);
        authMapper.insertSocialCustomer(socialUser);

        UserDto insertedUser = authMapper.findByUsername(socialUser.getUsername());
        if (insertedUser == null || insertedUser.getId() == null) {
            throw new IllegalStateException("소셜 로그인 고객 계정을 저장하지 못했습니다.");
        }
        authMapper.insertSocialAccount(insertedUser.getId(), provider, socialId);
        UserDto savedUser = authMapper.findBySocialAccount(provider, socialId);
        if (savedUser == null || savedUser.getId() == null) {
            throw new IllegalStateException("소셜 로그인 고객 계정을 저장하지 못했습니다.");
        }
        return prepareResponse(savedUser);
    }

    private UserDto prepareResponse(UserDto user) {
        user.setDisplayName(user.getName());
        user.setPassword(null);
        return user;
    }

    private String normalizeEmail(String email) {
        return isBlank(email) ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
