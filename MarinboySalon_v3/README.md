# MarinboySalon v3

선생님 `thejoa703`과 Spring Breeze v3의 구조를 기준으로 프론트엔드와 백엔드를 분리한 버전입니다.

## 버전 구분

- v1: Spring MVC + JSP + MyBatis + MySQL
- v2: Spring Boot 기반 SSR 구조와 관리자 기능 확장
- v3: `front` Next.js와 `back` Spring Boot REST API 완전 분리

## 폴더 구조

```text
MarinboySalon_v3
├─ back   # Spring Boot REST API, MyBatis, JWT, Redis
└─ front  # Next.js, React, Redux, Redux-Saga, Axios
```

세 버전은 새 MySQL 데이터베이스 `marinboy_salon`을 함께 사용합니다.

## 실행 환경

- Java 17
- Node.js 20.9 이상
- MySQL 8
- Redis 7
- 모든 소스 파일 UTF-8

## 포트폴리오 실행 기준

| 구성 요소 | 주소 | 주의 사항 |
|---|---|---|
| 프런트 | `http://localhost:3000` | Next.js 고객·관리자 화면 |
| 백엔드 | `http://localhost:8082` | V3 API는 8082로 고정 |
| Redis | `localhost:6379` | 기동하지 않으면 인증·Health 확인이 실패할 수 있음 |

V1·V2의 8080은 학습 단계용입니다. V3 포트폴리오를 시연할 때는 V1·V2 서버를 실행하지 않습니다.

제출 전에는 저장소 루트에서 다음 순서로 확인합니다.

```powershell
.\scripts\portfolio-preflight.ps1 -CheckRunningServices
.\scripts\verify-v3.ps1 -SkipFrontendInstall
```

## Google Calendar 로컬 설정

예약 저장이 완료되면 Google Calendar 일정을 비동기로 생성하고, 생성된 이벤트 ID를 `reservation.calendar_event_id`에 기록합니다. 비밀값과 서비스 계정 키는 Git에 넣지 않고 실행 환경 변수로 전달합니다.

```powershell
$env:GOOGLE_CALENDAR_ENABLED = 'true'
$env:GOOGLE_CALENDAR_ID = '공유받은 캘린더 ID'
$env:GOOGLE_CALENDAR_CREDENTIALS_PATH = 'C:\절대경로\service-account.json'
```

서비스 계정 이메일에는 대상 캘린더의 일정 변경 권한이 필요합니다.
