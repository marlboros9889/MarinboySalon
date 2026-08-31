package com.marinboy.auth.repository;

import org.apache.ibatis.annotations.Param;

import com.marinboy.auth.entity.SocialAccount;

// 소셜 제공자와 제공자 회원번호 조합으로 연결된 내부 계정을 찾거나 저장합니다.
public interface SocialAccountMapper {

    SocialAccount selectByProviderUser(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId);

    int insert(SocialAccount socialAccount);
}
