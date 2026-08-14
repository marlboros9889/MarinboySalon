# Marinboy v3 개발 방향

## 수업 기준

기준 소스: `C:\Program Files\Jang_adam\AI_Full_stack\Track\track008_2\boot1`

매일 개발 전 최신 수업 파일을 확인하는 규칙은 `docs/COURSE_BASELINE.md`를 따릅니다.

수업에서 다룬 Spring Boot, JPA Entity·Repository, MyBatis Mapper, Validation, JWT, Swagger만 사용합니다.

## 확정 기술 구조

`Entity·Repository(JPA) + DTO·Mapper(MyBatis) → Service → RestController → Swagger`

- Entity·Repository는 수업의 기본 CRUD 구조로 사용합니다.
- DTO는 요청·응답을 분리하고, MyBatis Mapper와 XML은 복잡한 조회 SQL을 담당합니다.
- Service는 예약 규칙, 권한, 트랜잭션을 담당합니다.
- RestController는 DTO만 입출력하며 Swagger로 API 계약을 공개합니다.

## 전환 원칙

- 기존 MyBatis Mapper와 화면 API는 유지합니다.
- 신규 API는 `/api/v3/**` 경로에 구현합니다.
- 동일 테이블은 JPA CRUD와 MyBatis 조회 역할이 겹치지 않게 구분합니다.
- 인증은 수업에서 다룬 Spring Security + JWT + Redis 방식으로 전환합니다.

## 도메인 전환 순서

1. 시술 메뉴
2. 예약
3. 회원/인증
4. 관리자
