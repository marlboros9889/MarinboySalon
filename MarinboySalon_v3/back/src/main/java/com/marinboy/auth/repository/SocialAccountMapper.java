package com.marinboy.auth.repository;

import org.apache.ibatis.annotations.Param;

import com.marinboy.auth.entity.SocialAccount;

public interface SocialAccountMapper {

    SocialAccount selectByProviderUser(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId);

    int insert(SocialAccount socialAccount);
}
