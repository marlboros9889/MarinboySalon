package com.marinboy.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.auth.entity.SocialAccount;
import com.marinboy.auth.repository.SocialAccountMapper;
import com.marinboy.global.oauth2.CustomOAuth2User;
import com.marinboy.global.oauth2.OAuth2UserInfoFactory;
import com.marinboy.global.oauth2.UserInfoOAuth2;
import com.marinboy.user.entity.AppUser;
import com.marinboy.user.repository.AppUserMapper;

import lombok.RequiredArgsConstructor;

/**
 * 소셜 계정을 기존 이메일 회원과 연결하거나 새 회원으로 등록합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAccountMapper socialAccountMapper;
    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String nameKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        UserInfoOAuth2 userInfo = OAuth2UserInfoFactory.create(provider, oauthUser.getAttributes());

        AppUser appUser = findOrCreateUser(provider, userInfo);
        return new CustomOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole())),
                oauthUser.getAttributes(),
                nameKey,
                appUser);
    }

    private AppUser findOrCreateUser(String provider, UserInfoOAuth2 userInfo) {
        SocialAccount savedSocial = socialAccountMapper.selectByProviderUser(
                provider, userInfo.getProviderUserId());
        if (savedSocial != null) {
            return appUserMapper.selectById(savedSocial.getUserId());
        }

        AppUser appUser = null;
        if (userInfo.getEmail() != null) {
            appUser = appUserMapper.selectByEmail(userInfo.getEmail());
        }
        if (appUser == null) {
            appUser = new AppUser();
            String email = userInfo.getEmail() == null
                    ? provider + "_" + userInfo.getProviderUserId() + "@social.local"
                    : userInfo.getEmail();
            appUser.setEmail(email);
            appUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            appUser.setName(userInfo.getName() == null ? "소셜 회원" : userInfo.getName());
            appUser.setPhone("미등록");
            appUser.setRole("CUSTOMER");
            appUserMapper.insert(appUser);
        }

        SocialAccount socialAccount = new SocialAccount();
        socialAccount.setUserId(appUser.getId());
        socialAccount.setProvider(provider);
        socialAccount.setProviderUserId(userInfo.getProviderUserId());
        socialAccount.setProviderEmail(userInfo.getEmail());
        socialAccountMapper.insert(socialAccount);
        return appUser;
    }
}
