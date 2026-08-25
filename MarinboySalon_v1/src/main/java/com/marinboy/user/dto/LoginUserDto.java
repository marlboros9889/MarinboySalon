package com.marinboy.user.dto;

/**
 * 비밀번호를 제외하고 로그인 상태에 필요한 정보만 세션에 보관합니다.
 */
public class LoginUserDto {

    private Long id;
    private String email;
    private String name;
    private String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
