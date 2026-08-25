package com.marinboy.global.oauth2;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.marinboy.user.entity.AppUser;

/**
 * 소셜 로그인 응답과 우리 DB 회원을 함께 보관합니다.
 */
public class CustomOAuth2User extends DefaultOAuth2User {

    private final AppUser appUser;

    public CustomOAuth2User(
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey,
            AppUser appUser) {
        super(authorities, attributes, nameAttributeKey);
        this.appUser = appUser;
    }

    public AppUser getAppUser() {
        return appUser;
    }
}
