# V1 → V2 → V3 참고 발전 흐름

Spring Breeze ERP의 세 버전에서 가져온 핵심은 “기능을 복제하지 않고 같은 업무를 다음 구조로 옮긴다”는 발전 방식입니다. 저장소 전체 파일 수가 아니라 한 도메인의 연결 경로를 기준으로 적용했습니다.

## V1: 기능 기초

```text
화면 → Controller → Service → Mapper → DB
```

- 메뉴, 예약, 고객, 관리자 CRUD를 먼저 완성합니다.
- DTO·Mapper XML·URL이 예측 가능한 이름을 사용합니다.
- 화면과 DB가 실제로 연결되는지 작은 흐름부터 확인합니다.

## V2: 1차 리팩토링

```text
Thymeleaf 화면 → Spring Boot Controller → Service → MyBatis → Oracle
```

- Spring Framework 설정을 Spring Boot로 옮기고 JSP를 Thymeleaf로 전환합니다.
- `UtilPaging`·`FileUploadUtil` 같은 기능 부품과 header·footer·layout fragment를 재사용합니다.
- 외부 API와 Ajax 기능은 `/api/**` 경계로 분리하고, Service·Mapper 흐름은 유지합니다.

## V3: 2차 리팩토링

```text
Next/React Page → 도메인 API 도구 → Spring Boot REST API
Bearer JWT·OAuth state·logout token → Spring Security·Redis
예약 DB commit → Google Calendar
```

- Thymeleaf 화면 책임을 Next.js Page와 재사용 Component로 옮깁니다.
- 공통 HTTP/JWT 처리는 API Client 한 곳에 두고, 예약·인증·관리자 API를 도메인 도구로 분리합니다.
- `HttpSession` 없이 보호 API를 JWT 하나로 통일합니다.
- 일반·소셜 로그인이 같은 고객과 예약 소유권으로 연결됩니다.
- 운영 규칙을 하드코딩하지 않고 관리자 UI와 DB로 이동합니다.
- Calendar 연동은 예약 트랜잭션 뒤에 실행합니다.

## 현재 최종 구조에서 제거한 과도기 코드

- MyBatis 메뉴와 중복된 JPA Entity·Repository·Service·Controller
- JWT 사용자 ID 경로와 중복된 전화번호 기반 예약 소유권 경로
- 단일 시술 조회를 위한 Redux·Saga·Store
- 제품 기능이 아닌 DB 메타데이터·시간 점검 API
- Calendar와 역할이 겹치는 DB 알림·SSE·메일 계층
- Swagger 학습 설정, 과거 변환 SQL, 중복 교본·아키텍처 문서

## 세 버전에서 유지하는 부품 원칙

```text
V1  Controller → Service → Mapper
V2  + Paging·Upload 도구 / Thymeleaf fragment
V3  + API Client·Domain API / React Component
```

버전이 바뀌어도 예약 중복 검사·소유권·상태 전이 같은 업무 규칙은 Service의 한 경로만 사용합니다. 화면 기술이 바뀔 때는 기존 화면을 함께 활성화하지 않고 새 화면으로 교체합니다. 부품은 한 가지 책임, 분명한 입력·출력, 단독 테스트가 있을 때만 추가합니다.

V1·V2·V3는 구조 발전의 참고 근거로만 남기고, GitHub 기본 브랜치의 실행 코드는 독립된 `marinboySalon` 하나만 유지합니다.
