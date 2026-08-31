# MarinboySalon V3 포트폴리오 최종 점검

## 1. 제출·시연 범위

제출본은 `MarinboySalon_v3`만 실행합니다. `MarinboySalon_v1`, `MarinboySalon_v2`는 기능 발전 과정을 보여 주는 학습 보관 폴더이므로 8080 서버를 실행하지 않습니다.

| 구성 요소 | 기준 | 확인 주소 |
|---|---|---|
| Next.js 화면 | 3000 | `http://localhost:3000` |
| Spring Boot API | 8082 고정 | `http://localhost:8082/actuator/health` |
| MySQL | 3306 | `marinboy_salon` |
| Redis | 6379 | 로그인·로그아웃 후 API 호출 |

## 2. 실행과 검증 스크립트

저장소 루트 `scripts` 폴더는 반복되는 제출 점검을 같은 방법으로 실행하기 위해 둡니다.

```powershell
# 포트 설정과 실행 중인 V3 서버를 점검합니다.
.\scripts\portfolio-preflight.ps1 -CheckRunningServices

# V3 백엔드 테스트·JAR, 프런트 테스트·빌드를 검증합니다.
.\scripts\verify-v3.ps1 -SkipFrontendInstall

# 생성된 build, target, .next만 안전하게 미리 확인합니다.
.\scripts\clean-generated.ps1 -WhatIf
```

`verify-project.ps1`은 V1·V2·V3 전체 학습 과정을 검사하는 용도이고, 포트폴리오 제출 전에는 `verify-v3.ps1`을 사용합니다.

## 3. 데이터 정규화와 무결성

현재 구조는 회원, 소셜계정, 시술, 시술이미지, 예약, 영업시간, 휴무일을 각각 분리합니다. 이미지 URL은 `service_item_image`에 분리해 한 메뉴에 최대 4장을 순서대로 보관합니다.

`database/migrations/20260831_portfolio_data_integrity.sql`은 다음 규칙을 DB에도 적용합니다.

- 시술 가격은 0 이상이고 소요 시간은 30분 단위
- 예약 상태는 `REQUESTED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`만 허용
- 영업 요일은 1~7이며, 영업일의 시작 시간은 종료 시간보다 빠름
- 예약 상태·시작 시간 인덱스로 예약 겹침 조회를 보조

마이그레이션의 첫 SELECT 결과가 0건인지 확인한 뒤 MySQL Workbench에서 실행합니다. 이 저장소에는 MySQL CLI가 설치되어 있지 않아 실제 DB 적용 여부는 제출 전 Workbench에서 확인해야 합니다.

## 4. 제출 전 실제 시연 순서

1. Redis와 MySQL을 기동하고 V3 백엔드(8082), 프런트(3000)를 실행합니다.
2. 첫 화면 원본 HTML에 메뉴가 포함되는지 `view-source:http://localhost:3000`으로 확인합니다.
3. 고객 예약을 생성하고 관리자 예약 목록에서 상태를 변경합니다.
4. 로그아웃한 뒤, 이전 Access Token으로 `/auth/me` 호출 시 401인지 확인합니다.
5. `test-v3-concurrent-reservation.ps1`으로 동일 시간 요청 두 건 중 한 건만 성공하는지 확인합니다.
6. Google Calendar는 서비스 계정·공유 캘린더가 준비된 경우에만 시연합니다. 준비되지 않았다면 선택 기능으로 명확히 설명합니다.

## 5. 포트폴리오에서 강조할 내용

- Next.js SSR로 최초 HTML에 시술 메뉴를 포함한 이유
- JWT Access Token과 HttpOnly Refresh Cookie, Redis 로그아웃 차단의 역할
- 30분 슬롯·휴무일·영업시간·`FOR UPDATE`로 중복 예약을 막는 흐름
- 관리자 메뉴 이미지, 휴무일, 예약 상태 관리
- 단위 테스트와 실제 동시 요청 검증으로 기능을 확인한 근거

## 6. 남은 위험과 다음 개선

| 위험 | 제출 전 대응 | 다음 개선 |
|---|---|---|
| 8080/8082 혼동 | V3만 실행하고 사전 점검 스크립트 실행 | V1·V2를 별도 학습 저장소로 분리 |
| 로컬 이미지 경로 | `SERVICE_IMAGE_DIRECTORY`를 외부 절대 경로로 설정 | S3 등 객체 저장소로 이전 |
| OAuth·Calendar 설정 | 실제 키가 있을 때만 시연 | 운영 환경별 비밀 관리 자동화 |
| 동시성 검증 | PowerShell 실제 동시 요청 검증 | Testcontainers 기반 MySQL 통합 테스트 |
| 데이터 제약 | 마이그레이션 사전 조회 후 적용 | Flyway 도입으로 적용 이력 자동화 |
