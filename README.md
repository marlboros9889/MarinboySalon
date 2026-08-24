# marinboySalon

1인 프라이빗 헤어살롱의 메뉴 탐색, 고객 예약, 소셜 로그인, 매장 운영, Google Calendar 동기화를 연결한 KDT 풀스택 포트폴리오입니다.

## 핵심 기능

- 컷·펌·컬러·클리닉 메뉴와 시술 이미지 갤러리
- 시술 시간·기존 예약·요일별 영업시간·휴무일을 반영한 예약 슬롯
- 비로그인 예약 진입 → 로그인 → 원래 예약 화면 복귀 → 정책 동의 → 예약
- 일반 로그인과 Google·Kakao·Naver 로그인, JWT·Redis 로그아웃 무효화
- 예약 DB 커밋 후 Google Calendar 일정 등록
- 관리자 예약 상태·메뉴·영업시간·휴무일·Calendar 관리
- PC·태블릿·모바일 반응형 다크 골드 UX

## 실행 구조

```text
Browser :3000 (Next.js SSR)
        ↓ REST + Bearer JWT
Spring Boot :8082 → MyBatis → Oracle
        ├─ Redis :6379 (JWT 폐기, OAuth state)
        └─ Google Calendar / Social OAuth
```

메뉴 데이터는 `MyBatis` 한 경로, 화면 상태는 `React state + SSR props` 한 경로만 사용합니다. 화면의 HTTP 처리는 도메인 API 도구로, 예약 시간은 DB 없는 계산 도구로 재사용하며 같은 기능을 JPA·Redux/Saga로 중복 구현하지 않습니다.

## 빠른 시작

필수 도구: JDK 17+, Maven, Node.js, Oracle, Docker 또는 Redis

```powershell
cd C:\Users\tj-bu-703-21\OneDrive\Desktop\프로젝트\marinboySalon
.\scripts\setup-local.ps1 -StartRedis
# .env.local의 ORACLE_* 값을 실제 로컬 계정으로 수정
.\scripts\run-dev.ps1 -Action Restart -InstallDependencies -StartDependencies
```

- 고객 화면: `http://127.0.0.1:3000`
- 공개 시술 API: `http://127.0.0.1:8082/api/services`
- 상태 확인: `.\scripts\run-dev.ps1 -Action Status`
- 안전 종료: `.\scripts\run-dev.ps1 -Action Stop`
- 전체 검증: `.\scripts\verify-project.ps1`

## 로컬 DB 준비

Oracle 초기화가 필요할 때 아래 순서로 실행합니다.

1. `backend/src/main/resources/db/oracle/local/01_drop_tables.sql`
2. `backend/src/main/resources/db/oracle/02_create_tables.sql`
3. `backend/src/main/resources/db/oracle/local/03_seed_sample_data.sql`
4. `backend/src/main/resources/db/oracle/local/09_align_identity_values.sql`
5. `backend/src/main/resources/db/oracle/04_verify_tables.sql`

## 외부 연동

소셜 제공자 Client ID/Secret은 `.env.local`에 넣고 Callback URL을 다음과 같이 등록합니다.

```text
http://127.0.0.1:3000/login/oauth2/code/kakao
http://127.0.0.1:3000/login/oauth2/code/naver
http://127.0.0.1:3000/login/oauth2/code/google
```

Calendar 서비스 계정 JSON은 저장소 밖 `${user.home}/.marinboy-salon/credentials/`에 보관합니다. 대상 Calendar를 서비스 계정 이메일과 공유하고 일정 변경 권한을 부여해야 합니다.

## 문서

- [프로젝트 교본](docs/PROJECT_GUIDE.md)
- [V1 → V2 → V3 참고 발전 흐름](docs/EVOLUTION.md)
- [검증 기록](docs/QA.md)

## 보안 원칙

- 보호 API는 `HttpSession` 없이 Bearer JWT만 사용합니다.
- OAuth `state`와 로그아웃 JWT는 Redis에 만료시간과 함께 저장합니다.
- 비밀번호는 BCrypt, SQL 입력값은 MyBatis `#{}` 바인딩을 사용합니다.
- DB 비밀번호·OAuth Secret·Calendar 키·업로드 파일은 Git과 JAR에 넣지 않습니다.
