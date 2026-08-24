# Marinboy v2

Spring Boot + Thymeleaf + MyBatis + Oracle 기반의 서버 세션 버전입니다. v3의 JWT·Redis 설정을 사용하지 않습니다.

## 실행 구조

```text
브라우저 → Spring Boot :8081 → Controller → Service → Dao → MyBatis XML → Oracle
                  └─ MARINBOY_V2_SESSION(HttpSession)
```

## 환경설정과 실행

`setup-local.ps1`가 Git에서 제외되는 `.env.local`을 만들고, 실행 스크립트가 매번 같은 파일을 읽습니다. 실제 비밀번호 파일은 Git에 올리지 않습니다.

```powershell
.\scripts\setup-local.ps1
# .env.local의 V2_ORACLE_* 실제 값 입력
.\scripts\run-dev.ps1 -Action Restart
.\scripts\verify-project.ps1
```

- 고객 화면: `http://127.0.0.1:8081/`
- 기본 업로드 폴더: `${user.home}/.marinboy/uploads` (같은 Oracle 시술 이미지 URL을 v3와 공유)
- 실행 Maven 산출물: `${user.home}/.marinboy/build/v2` (OneDrive `target` 잠금 방지)
- 검증 Maven 산출물: `${user.home}/.marinboy/build/verify/v2` (실행 중인 JAR 잠금과 분리)
- 전체 정적·빌드 검증: `.\scripts\verify-project.ps1`
- 상태 확인/종료: `.\scripts\run-dev.ps1 -Action Status`, `.\scripts\run-dev.ps1 -Action Stop`

v2의 소셜 로그인도 Spring Security 세션을 사용합니다. Redis와 `JWT_SECRET`은 설정하지 않습니다.
