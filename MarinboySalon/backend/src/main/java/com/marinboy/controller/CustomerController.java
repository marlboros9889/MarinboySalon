package com.marinboy.controller;

import com.marinboy.dto.UserDto;
import com.marinboy.dto.UserProfileRequestDto;
import com.marinboy.dto.UserResponseDto;
import com.marinboy.service.AuthService;
import com.marinboy.service.AuthenticatedUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 고객 본인 정보 API를 예약 기능과 분리해 사용자 책임을 한곳에 둡니다. */
@RestController
public class CustomerController {
    private final AuthService authService;
    private final AuthenticatedUserService authenticatedUserService;

    public CustomerController(AuthService authService, AuthenticatedUserService authenticatedUserService) {
        this.authService = authService;
        this.authenticatedUserService = authenticatedUserService;
    }

    // 고객 본인이 수정 가능한 연락처 필드만 받아 다음 예약에도 같은 정보를 사용합니다.
    @PutMapping("/api/customers/me")
    public ResponseEntity<UserResponseDto> updateMe(
            @Valid @RequestBody UserProfileRequestDto request,
            Authentication authentication) {
        UserDto currentUser = authenticatedUserService.requireUser(authentication);
        UserDto updatedUser = authService.updateProfile(currentUser, request);
        return ResponseEntity.ok(UserResponseDto.from(updatedUser));
    }
}
