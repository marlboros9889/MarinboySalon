package com.marinboy.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

import com.marinboy.auth.dto.response.UserResponseDto;
import com.marinboy.auth.service.AuthUserJwtService;
import com.marinboy.auth.dto.request.UserRequestDto;
import com.marinboy.user.service.AppUserService;
import com.marinboy.user.entity.AppUser;
import com.marinboy.user.repository.AppUserMapper;

import lombok.RequiredArgsConstructor;

/** 관리자만 다른 계정의 권한과 삭제를 관리합니다. */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AppUserMapper userMapper;
    private final AuthUserJwtService authUserJwtService;
    private final AppUserService appUserService;

    /** 관리자가 새 관리자 계정을 만들고 바로 관리자 권한을 부여합니다. */
    @PostMapping
    public ResponseEntity<UserResponseDto> createAdmin(@Valid @RequestBody UserRequestDto request) {
        UserResponseDto createdUser = appUserService.signup(request);
        userMapper.updateRole(createdUser.getId(), "ADMIN");
        return ResponseEntity.ok(UserResponseDto.from(userMapper.selectById(createdUser.getId())));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> list() {
        return ResponseEntity.ok(userMapper.selectAll().stream().map(UserResponseDto::from).toList());
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        Long currentUserId = authUserJwtService.getCurrentUserId(authentication);
        String role = request.get("role");
        if (!"ADMIN".equals(role) && !"CUSTOMER".equals(role)) throw new IllegalArgumentException("올바른 권한을 선택해 주세요.");
        if (currentUserId.equals(id) && "CUSTOMER".equals(role)) throw new IllegalArgumentException("본인 관리자 권한은 해제할 수 없습니다.");
        userMapper.updateRole(id, role);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateAdminUser(@PathVariable Long id, @RequestBody Map<String, String> request) {
        userMapper.updateAdminUser(id, request.get("email"), request.get("name"), request.get("phone"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = authUserJwtService.getCurrentUserId(authentication);
        if (currentUserId.equals(id)) throw new IllegalArgumentException("본인 계정은 삭제할 수 없습니다.");
        if (userMapper.countReservations(id) > 0) throw new IllegalArgumentException("예약 이력이 있는 계정은 삭제할 수 없습니다.");
        userMapper.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
