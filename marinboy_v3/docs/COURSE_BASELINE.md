# 수업 베이스 개발 규칙

## 기준 경로

`C:\Program Files\Jang_adam\AI_Full_stack\Track`

## 매일 개발 시작 순서

1. `Track`의 최신 생성·수정 파일을 먼저 확인합니다.
2. 해당 수업의 예제 코드, 주석, 패키지 구조, 의존성을 읽습니다.
3. 수업에서 확인된 기술만 Marinboy_v3에 적용합니다.
4. 구현 뒤에는 변경 코드와 수업 예제의 연결점을 기록하고 테스트합니다.

## 적용 기준

- 백엔드: Spring Boot, JPA Entity·Repository, MyBatis Mapper, Service, RestController, Swagger
- 보안: 수업에서 다룬 Spring Security, JWT, Redis 범위에서만 적용
- 프론트엔드: track008의 Next.js SSR, React, Redux Reducer, Redux-Saga, Bootstrap 방식 사용
- 주석: `//1. 기능 설명` 형식의 한글 주석을 사용

## 금지 기준

- 수업 파일과 프로젝트 요구사항에서 확인하지 못한 라이브러리·패턴은 임의로 추가하지 않습니다.
- 필요한 기술이 수업 범위를 벗어나면 구현 전 사용자에게 먼저 알립니다.
