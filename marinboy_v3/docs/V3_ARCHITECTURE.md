# Marinboy v3 개발 방향

## 수업 기준

기준 소스: `track007_boot_api/boot1`, `track008_nodeReact/front`, `track008_nodeReact/※2_react.md`

매일 개발 전 최신 수업 파일을 확인하는 규칙은 `docs/COURSE_BASELINE.md`를 따릅니다.

수업에서 다룬 Spring Boot·MyBatis와 Next.js·Redux·Saga·SSR 구조를 프로젝트 기능에 맞게 사용합니다.

## 저장소 구조

```text
marinboy_v3/
├── backend/   # Spring Boot, MyBatis, Oracle, 서버 렌더링 화면
├── frontend/  # Next.js SSR, Redux Reducer, Saga, Bootstrap 5
└── docs/      # 요구사항, 기능 흐름, 검증 기록
```

`mobile/`은 기존 보관 폴더이며 1차 완료 범위와 이후 현재 작업 범위에서 제외합니다.

## 확정 기술 구조

`Entity·Repository(JPA) + DTO·Mapper(MyBatis) → Service → RestController → Swagger`

- Entity·Repository는 수업의 기본 CRUD 구조로 사용합니다.
- DTO는 요청·응답을 분리하고, MyBatis Mapper와 XML은 복잡한 조회 SQL을 담당합니다.
- Service는 예약 규칙, 권한, 트랜잭션을 담당합니다.
- RestController는 DTO만 입출력하며 Swagger로 API 계약을 공개합니다.

프런트엔드는 `pages(SSR) → View → Saga(API) → Reducer → Store → View` 흐름을 사용합니다.

## 기능별 클래스 분류

| 기능 | Frontend | Backend |
|---|---|---|
| 인증 | `features/auth` | `AuthController`, `AuthService`, `AuthMapper` |
| 시술 | `features/home`, `reducers/service`, `sagas/service` | `V3ServiceItemController`, `MenuService`, `MenuMapper` |
| 예약 | `pages/reservation.js`, `pages/my-reservations.js` | `ReservationController`, `ReservationService`, `ReservationMapper` |
| 알림 | `pages/admin.js` SSE 모달 | `NotificationController`, `NotificationService`, `NotificationMapper` |
| 운영 | `pages/admin.js` | `AdminController`, `MenuService`, `ReservationService` |

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
