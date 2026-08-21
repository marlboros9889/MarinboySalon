# Marinboy Salon

두 버전은 인증 방식과 실행 포트를 분리한 독립 프로젝트입니다.

| 버전 | 화면/서버 | 인증 | Redis | 실행 포트 |
|---|---|---|---|---|
| [`marinboy_v2/`](marinboy_v2/) | Thymeleaf + Spring Boot + MyBatis | `HttpSession` + 전용 쿠키 | 사용하지 않음 | 8081 |
| [`marinboy_v3/`](marinboy_v3/) | Next.js + Spring Boot + MyBatis/JPA | Stateless JWT + Redis 로그아웃 차단 | 필수 | 3000 / 8082 |

같은 PC에서 함께 실행해도 v2의 `MARINBOY_V2_SESSION` 쿠키와 v3의 Bearer 토큰이 서로 영향을 주지 않습니다. 각 버전의 설치·실행·검증 방법은 해당 폴더 README를 확인합니다.

모바일은 아직 수업 범위가 아니므로 저장소에서 제거했으며 실행·검증·복구 대상에도 포함하지 않습니다.

## 다른 PC에서 처음 실행

```powershell
git clone https://github.com/marlboros9889/MarinboySalon.git
cd MarinboySalon

# v2: 세션 방식 전용 설정
cd marinboy_v2
.\scripts\setup-local.ps1
# .env.local의 V2_ORACLE_* 실제 값 입력
.\scripts\run-dev.ps1 -Action Restart

# v3: JWT + Redis 방식 전용 설정
cd ..\marinboy_v3
.\scripts\setup-local.ps1 -StartRedis
# .env.local의 ORACLE_* 실제 값 입력
.\scripts\run-dev.ps1 -Action Restart -InstallDependencies -StartDependencies
```

Oracle 비밀번호, JWT 서명키, Google 서비스 계정 JSON은 GitHub에 포함되지 않습니다. 각 PC에서 `setup-local.ps1`로 로컬 설정을 만들고 실제 값만 입력해야 합니다.

전체 검증은 저장소 루트에서 `.\scripts\verify-all.ps1`로 실행합니다.
