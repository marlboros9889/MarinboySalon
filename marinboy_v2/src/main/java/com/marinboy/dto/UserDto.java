package com.marinboy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 로그인 요청과 세션 사용자 정보를 전달하는 DTO입니다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Long id;
    // 로그인에 사용하는 계정 아이디입니다.
    private String username;
    // 로그인 요청에서만 전달되는 비밀번호입니다.
    private String password;
    // DB에 저장된 사용자 실명입니다.
    private String name;
    // 화면에 표시할 이름이며 값이 없으면 실명을 사용합니다.
    private String displayName;
    // 예약 연락에 사용하는 이메일과 전화번호입니다.
    private String email;
    private String phone;
    // 고객과 관리자를 구분하는 권한 값입니다.
    private String role;
    private String loginProvider;

    // MyBatis 매핑과 JSON 변환에 사용하는 getter/setter입니다.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    // 응답 JSON에는 비밀번호가 노출되지 않도록 쓰기 전용으로 제한합니다.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    // 별도 표시 이름이 없으면 사용자 실명을 반환합니다.
    public String getDisplayName() {
        if (isUsableName(displayName)) return displayName;
        if (isUsableName(name)) return name;
        return username == null || username.isBlank() ? "사용자" : username;
    }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getLoginProvider() { return loginProvider; }
    public void setLoginProvider(String loginProvider) { this.loginProvider = loginProvider; }

    private boolean isUsableName(String value) {
        return value != null && !value.isBlank() && !value.matches("\\?+");
    }
}
