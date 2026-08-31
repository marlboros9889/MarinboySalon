# MarinboySalon 포트폴리오

1인 헤어샵의 필수 기능을 수업 단계에 맞춰 v1, v2, v3로 나눈 학습용 포트폴리오입니다. 제출·시연 대상은 **MarinboySalon_v3**입니다.

## 버전 구성

```
MarinboySalon_v1/  Spring Boot + JSP + MyBatis + MySQL 기본 예약
MarinboySalon_v2/  v1 구조 + 관리자 메뉴 영업시간 휴무일 예약 상태 관리
MarinboySalon_v3/  front(Next.js) + back(Spring Boot REST API) 완전 분리
```

> V1·V2는 학습 과정 보관용입니다. 포트폴리오 시연 때는 실행하지 않으며 V3만 실행합니다.

- [MarinboySalon_v1](MarinboySalon_v1/README.md)
- [MarinboySalon_v2](MarinboySalon_v2/README.md)
- [MarinboySalon_v3](MarinboySalon_v3/README.md)
- [개선 사항 2026-08-28](docs/IMPROVEMENTS_20260828.md)
- [포트폴리오 최종 점검](docs/PORTFOLIO_FINAL_GUIDE.md)

## 한 번에 실행하는 순서

1. MySQL에 `MarinboySalon_v3/database/schema.sql` 및 `sample_data.sql` 적용
2. 기존 DB에는 `MarinboySalon_v3/database/migrations/` SQL을 날짜순으로 적용
3. Redis 기동 (v3)
4. 환경 변수 설정 후 back → front 실행

### 필수 환경 변수

| 변수 | 설명 |
|------|------|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | DB 접속 (운영에서 비밀번호 필수) |
| `JWT_SECRET` | 32자 이상 (운영 필수, prod에서 로컬 기본값 금지) |
| `REDIS_HOST` / `REDIS_PORT` | Redis |
| `CORS_ALLOWED_ORIGINS` / `FRONT_URL` | 프론트 주소 |

### 포트 고정 규칙

| 구성 요소 | 고정 포트 | 시연 규칙 |
|---|---:|---|
| V3 Next.js | 3000 | `MarinboySalon_v3/front`만 실행 |
| V3 Spring Boot API | 8082 | V1·V2의 8080과 함께 실행하지 않음 |
| MySQL | 3306 | `marinboy_salon` 데이터베이스 사용 |
| Redis | 6379 | JWT Refresh Token과 로그아웃 토큰 차단에 사용 |

- Health: `http://localhost:8082/actuator/health`
- Swagger: `http://localhost:8082/swagger-ui/index.html`

## 범위 메모

- **v1**: 예약은 현재 이후 시간·활성 시술·겹침만 검사 (영업시간·휴무일 없음)
- **v2**: `AdminAuthInterceptor` + Controller 이중 권한 검사, 전체 POST 폼 CSRF 검증
- **v3**: 상태 전이 enum, 30분 슬롯 공통 규칙, 겹침 FOR UPDATE, Actuator, Swagger
- **메뉴 이미지**: 관리자가 JPG·PNG·WEBP 파일을 최대 4장 업로드하고, DB에는 저장된 이미지 URL과 표시 순서만 보관

## 인증과 동시성 경계

- v2는 `JSESSIONID` 세션, v3는 Access JWT + HttpOnly Refresh 쿠키 + Redis를 사용합니다.
- 두 버전은 `user_account` 데이터만 공유하며 로그인 상태는 공유하지 않습니다. 한 버전에서 로그인해도 다른 버전에는 다시 로그인해야 합니다.
- v2와 v3 예약 생성은 모두 해당 요일 `business_hour` 행을 `FOR UPDATE`로 잠근 뒤 겹침을 조회하고 저장합니다.
- 행 잠금은 같은 요일 요청을 순서대로 처리하고, DB 유니크 제약은 완전히 같은 단일 값의 중복만 막는 마지막 방어선입니다. 시술 시간이 구간으로 겹치는지는 행 잠금과 겹침 쿼리가 담당합니다.
