# Marinboy v3

Next.js + Spring Boot + MyBatis/JPA + Oracle 기반의 JWT·Redis 버전입니다. `HttpSession`과 Spring OAuth2 세션 로그인을 사용하지 않습니다.

## 실행 구조

```text
브라우저 :3000 ── Authorization: Bearer JWT ──> Spring Boot :8082
                                                     ├─ Oracle
                                                     └─ Redis(로그아웃 토큰 차단)
```

## 최초 환경설정

JDK 17 이상, Maven, Node.js, Oracle, Redis가 필요합니다.

```powershell
Copy-Item .env.example .env.local
# ORACLE_*, JWT_SECRET, REDIS_* 실제 값 입력
.\scripts\run-dev.ps1 -Action Restart -InstallDependencies
```

`JWT_SECRET`은 32바이트 이상의 원문을 Base64로 인코딩한 값이어야 합니다. `.env.local`과 서비스 계정 키는 Git에 올리지 않습니다.

- 고객 화면: `http://127.0.0.1:3000`
- 백엔드 API: `http://127.0.0.1:8082/api/services`
- 상태 확인: `.\scripts\run-dev.ps1 -Action Status`
- 안전 종료: `.\scripts\run-dev.ps1 -Action Stop`

## 인증 원칙

- 로그인 성공 시 `/api/auth/login`이 JWT와 사용자 정보를 반환합니다.
- 프론트엔드는 보호 API와 SSE 요청에 Bearer 토큰을 보냅니다.
- 서버는 `SessionCreationPolicy.STATELESS`이며 세션 쿠키를 만들지 않습니다.
- 로그아웃 시 JWT 식별자를 Redis 블랙리스트에 저장해 재사용을 막습니다.
- 세션이 필요한 Spring OAuth2 로그인은 v2에만 둡니다.

## 업로드와 외부 연동

업로드 기본 경로는 실행 폴더와 무관한 `${user.home}/.marinboy/uploads`입니다. `UPLOAD_DIRECTORY`를 지정할 때는 절대경로만 허용합니다.

Google Calendar 연동을 켤 때는 캘린더 ID와 서비스 계정 키의 절대경로를 설정하고 서비스 계정에 일정 변경 권한을 부여합니다. 키 파일은 프로젝트 밖에 보관합니다.

## 전체 검증

```powershell
.\scripts\verify-project.ps1
```

백엔드 테스트·JAR 비밀파일 검사·프론트 테스트/린트/production 빌드·HTTP 응답·Git diff를 검사합니다. 모바일은 수업 전 범위이므로 프로젝트에서 제외했습니다. 버전별 보안 분리는 [`docs/VERSION_SECURITY_MATRIX.md`](docs/VERSION_SECURITY_MATRIX.md)를 확인합니다.
