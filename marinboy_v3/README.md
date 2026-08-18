# marinboy_mybatis


## 목표 구조

```text
src/main/java/com/marinboy
├── api
├── config
├── controller
├── dao
├── dto
├── llmrag
├── security
├── service
└── util

src/main/resources
└── mybatis/mapper
```

## 1차 검증 목표

```text
Oracle 연결 확인
-> TestDao
-> test-mapper.xml
-> SELECT SYSDATE FROM DUAL
-> /api/db-time 응답
```

## 로컬 보안 설정

- Oracle 접속 정보는 `ORACLE_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD` 환경 변수로 주입합니다.
- 로컬 시드는 `src/main/resources/db/oracle/local`에서만 수동 실행합니다.
- 관리자 비밀번호는 `ADMIN_PASSWORD_BCRYPT` 환경 변수의 BCrypt 해시로만 주입합니다.
- OAuth 콘솔 키와 Redirect URI가 없으면 소셜 로그인은 `BLOCKED` 상태입니다.
- `/api/db-time`과 `/api/db/**`는 관리자 세션에서만 접근할 수 있습니다.
- `/api/v3/**`는 Bearer JWT 전용 무상태 체인이라 CSRF 예외이며, 나머지 세션 API는 CSRF 토큰이 필요합니다.

## Git 이력 주의

현재 추적 해제만으로 과거 커밋의 로컬 설정은 사라지지 않습니다. 공개 저장소라면 백업 후 `git filter-repo` 또는 BFG로 `application-local.properties`와 생성물 이력을 제거하고, 협업자 공지 후 force-push해야 합니다.
