# MarinboySalon 포트폴리오

1인 헤어샵의 필수 기능을 수업 단계에 맞춰 v1, v2, v3로 나눈 학습용 포트폴리오입니다.

## 버전 구성

```
MarinboySalon_v1/  Spring Boot + JSP + MyBatis + MySQL 기본 예약
MarinboySalon_v2/  v1 구조 + 관리자 메뉴 영업시간 휴무일 예약 상태 관리
MarinboySalon_v3/  front(Next.js) + back(Spring Boot REST API) 완전 분리
```

- [MarinboySalon_v1](MarinboySalon_v1/README.md)
- [MarinboySalon_v2](MarinboySalon_v2/README.md)
- [MarinboySalon_v3](MarinboySalon_v3/README.md)
- [개선 사항 2026-08-28](docs/IMPROVEMENTS_20260828.md)

## 한 번에 실행하는 순서

1. MySQL에 `MarinboySalon_v3/database/schema.sql` 및 `sample_data.sql` 적용
2. (선택) `MarinboySalon_v3/database/migrations/` SQL 적용
3. Redis 기동 (v3)
4. 환경 변수 설정 후 back → front 실행

### 필수 환경 변수

| 변수 | 설명 |
|------|------|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | DB 접속 (운영에서 비밀번호 필수) |
| `JWT_SECRET` | 32자 이상 (운영 필수, prod에서 로컬 기본값 금지) |
| `REDIS_HOST` / `REDIS_PORT` | Redis |
| `CORS_ALLOWED_ORIGINS` / `FRONT_URL` | 프론트 주소 |

- Health: `http://localhost:8080/actuator/health`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

## 범위 메모

- **v1**: 예약은 현재 이후 시간·활성 시술·겹침만 검사 (영업시간·휴무일 없음)
- **v2**: `AdminAuthInterceptor` + Controller 이중 권한 검사
- **v3**: 상태 전이 enum, 30분 슬롯 공통 규칙, 겹침 FOR UPDATE, Actuator, Swagger
- **메뉴 이미지**: URL만 DB 저장 (파일 업로드는 후속)
