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
