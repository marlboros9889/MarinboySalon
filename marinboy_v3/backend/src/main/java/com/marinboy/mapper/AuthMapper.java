package com.marinboy.mapper;

import com.marinboy.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 사용자 로그인 정보를 Oracle에서 조회하는 MyBatis DAO입니다. */
@Mapper
public interface AuthMapper {
    // username으로 사용자를 조회하며 비밀번호 검증은 AuthService에서 수행합니다.
    UserDto findByUsername(@Param("username") String username);

    // 소셜 제공자와 제공자 내부 ID의 조합으로 같은 고객 계정을 다시 찾습니다.
    UserDto findBySocialAccount(@Param("provider") String provider, @Param("socialId") String socialId);

    // 소셜 이메일과 동일한 기존 일반 계정을 찾아 예약 이력을 한 계정으로 유지합니다.
    UserDto findByEmail(@Param("email") String email);

    // 회원가입 화면의 아이디·이메일 중복 확인에 사용합니다.
    int countByUsername(@Param("username") String username);
    int countByEmail(@Param("email") String email);

    // 회원가입 시 CUSTOMER 권한의 일반 계정을 저장합니다.
    int insertCustomer(UserDto user);

    // 소셜 로그인 최초 성공 시 비밀번호 로그인이 불가능한 CUSTOMER 계정을 저장합니다.
    int insertSocialCustomer(UserDto user);

    // 아직 소셜 계정이 연결되지 않은 일반 회원에게 제공자 식별자를 연결합니다.
    int linkSocialAccount(@Param("id") Long id, @Param("provider") String provider,
            @Param("socialId") String socialId);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    // 고객이 직접 수정한 이름·이메일·연락처를 계정과 예약 연락처에 함께 반영합니다.
    int updateProfile(UserDto user);
    int updateReservationContact(UserDto user);
    int countByEmailExceptId(@Param("email") String email, @Param("id") Long id);
}
