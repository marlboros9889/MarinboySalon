# MarinboySalon | 예약 중심 미용실 운영 플랫폼

고객이 웹이나 모바일 앱에서 시술을 살펴보고 예약하면, 관리자가 하나의 운영 데이터에서 예약 현황을 처리하는 풀스택 프로젝트입니다. V4는 별도 백엔드를 복제하지 않고 V3의 Spring Boot API와 데이터베이스를 함께 사용합니다.

<p align="center">
  <a href="https://marinboysalon.duckdns.org/">웹 서비스</a> ·
  <a href="https://marinboysalon.duckdns.org/v4/">V4 웹 앱</a> ·
  <a href="MarinboySalon_v3/README.md">V3 기술 문서</a> ·
  <a href="MarinboySalon_v4/README.md">V4 실행 안내</a>
</p>

## 프로젝트 개요

| 항목 | 설명 |
| --- | --- |
| 목표 | 예약 과정을 간편하게 하고, 예약·시술 운영 정보를 웹과 앱에서 이어서 확인 |
| V3 웹 | Next.js 고객 서비스와 관리자 운영 화면 |
| V4 앱 | Flutter/Dart 고객 예약 앱 및 예약 운영 대시보드 |
| 공통 서버 | Spring Boot REST API, MyBatis, MySQL; 인증·예약·시술 정보를 공유 |
| 연동 | Google·Kakao·Naver 로그인 흐름, Google Calendar 예약 일정 연동(서버 설정과 제공자 권한 필요) |
| 배포 | AWS EC2 기반 운영, Nginx 경로로 웹·V4 웹·백엔드 연결 |

## 버전 구성

| 버전 | 역할 | 기술 |
| --- | --- | --- |
| [V1](MarinboySalon_v1/README.md) | 예약 기능의 기초 학습 | Spring MVC, JSP, MyBatis, MySQL |
| [V2](MarinboySalon_v2/README.md) | 관리자 기능과 예약 운영 확장 | Spring Boot, Spring Security, MyBatis |
| [V3](MarinboySalon_v3/README.md) | 웹 서비스와 공통 백엔드 | Next.js, Spring Boot REST API, MySQL, Redis |
| [V4](MarinboySalon_v4/README.md) | 기존 서비스를 공유하는 모바일·웹 앱 | Flutter, Dart, Riverpod, Dio |

## 기능 범위

- 고객: 시술 메뉴와 이미지 조회, 일반/OAuth 로그인, 예약 가능한 시간 조회, 예약 등록 및 내 예약 확인·취소
- 관리자 앱: 전체 예약 확인, 예약 상태 변경, 예약 상태를 바탕으로 한 예상 매출 확인
- 예약: 서버가 날짜별 가능 시간과 중복 여부를 검증하고, 예약을 V3와 같은 데이터베이스에 저장
- 캘린더: 예약 확정 후 V3 서버가 Google Calendar 일정을 연동하고 이벤트 ID를 기록
- 제외 범위: 직원 관리, 직원 배정, 근무표 기능은 V4에 포함하지 않음

V4는 V3 웹 화면 전체를 Flutter 화면으로 복제하는 프로젝트가 아닙니다. V3 백엔드와 계정·예약 데이터를 공유하며, V4에서 구현한 고객 예약 및 관리자 예약 운영 기능을 제공합니다. 후기·시술 편집·이벤트 등 V3 웹에만 있는 화면은 웹에서 계속 이용합니다.

## 시스템 구성

```text
고객 브라우저 ── V3 Next.js 웹 ─┐
                               ├── Spring Boot REST API ── MyBatis ── MySQL
Android / V4 웹 ─ Flutter 앱 ──┘               ├── Redis
                                               └── Google Calendar API
```

두 화면이 예약 API와 데이터베이스를 공유하므로 한쪽에서 생성한 예약도 다른 쪽 운영 화면에서 확인할 수 있습니다. 소셜 로그인의 경우 V3 서버가 OAuth 절차를 담당하고, 모바일 앱은 앱 전용 콜백 코드 교환 방식으로 로그인 정보를 받습니다. 실제 OAuth·캘린더 사용은 운영 환경의 키, 허용 콜백 주소와 권한 설정에 영향을 받습니다.

## 저장소 안내

| 경로 | 내용 |
| --- | --- |
| `MarinboySalon_v3/front` | V3 Next.js 웹 화면 |
| `MarinboySalon_v3/back` | 공통 Spring Boot API 및 서버 기능 |
| `MarinboySalon_v3/database` | 데이터베이스 스키마와 마이그레이션 |
| `MarinboySalon_v4` | Flutter 앱, 테스트, Android·웹 실행 설정 |
| `MarinboySalon_v3/portfolio-dashboard` | Django·Pandas·Chart.js 예약·후기 분석 포트폴리오 |
| `docs` | 프로젝트 기록 및 개발 문서 |

## 개발 및 검증

- V3 백엔드: Gradle 테스트와 실행 패키지 빌드
- V3 프런트엔드: Jest 테스트와 Next.js 프로덕션 빌드
- V4: Flutter 정적 분석·단위 테스트·웹 빌드·Android APK 빌드
- CI/CD: GitHub Actions가 테스트와 빌드 후 AWS 서비스 배포를 수행

각 실행 명령과 환경 변수 설정은 [V3 안내](MarinboySalon_v3/README.md)와 [V4 안내](MarinboySalon_v4/README.md)를 참고하세요. OAuth 비밀값, DB 접속 정보, Google 서비스 계정 키는 저장소에 넣지 않고 운영 환경 변수로 관리합니다.

## 관련 자료

- [V3 아키텍처·API·데이터베이스 문서](MarinboySalon_v3/README.md)
- [V4 Flutter 실행 방법](MarinboySalon_v4/README.md)
- [예약·후기 분석 대시보드](MarinboySalon_v3/portfolio-dashboard/README.md)
- [프로젝트 최종 점검 가이드](docs/PORTFOLIO_FINAL_GUIDE.md)
- [라이브 V3 웹](https://marinboysalon.duckdns.org/) · [라이브 V4 웹](https://marinboysalon.duckdns.org/v4/)
