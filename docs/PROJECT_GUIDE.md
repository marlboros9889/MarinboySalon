# marinboySalon 프로젝트 교본

## 1. 목표와 범위

이 프로젝트의 목표는 “고객이 원하는 시술을 찾아 예약하고, 원장이 운영 규칙과 일정을 관리한다”는 한 문장을 정확히 구현하는 것입니다. 포트폴리오 기능은 다음 여섯 흐름으로 제한합니다.

1. 시술 메뉴 탐색
2. 일반·소셜 로그인
3. 예약 가능 시간 조회
4. 본인 예약 생성·수정·취소
5. 관리자 예약·메뉴·영업일 관리
6. Google Calendar 동기화

메일, 별도 실시간 알림 서버, DB 구조 조회 API, 같은 도메인의 JPA 복제 계층처럼 위 흐름을 완성하지 않는 코드는 추가하지 않습니다.

## 2. 기술 구조

| 영역 | 선택 | 역할 |
| --- | --- | --- |
| Frontend | Next.js Pages Router, React | SSR 첫 화면, 예약·관리 UI |
| Backend | Spring Boot, Spring Security | REST API, JWT 권한, 업무 규칙 |
| Database | Oracle, MyBatis | 사용자·메뉴·예약·운영 규칙 저장 |
| Auth state | Redis | OAuth state, 로그아웃 JWT 폐기 |
| External | Google Calendar API | 확정된 DB 예약을 원장 Calendar에 등록 |

한 기능에는 활성 구현을 하나만 둡니다.

- 메뉴: `Controller → ServiceItemService → ServiceItemMapper → service-item.xml`
- 예약 저장: `ReservationController → ReservationService → ReservationMapper → reservation.xml`
- 예약 시간: `ReservationController → ReservationScheduleService → ReservationScheduleTool`
- 인증: `AuthController → AuthService → AuthMapper → auth.xml`
- 화면 데이터: SSR props 또는 해당 화면의 React state

## 3. 핵심 업무 흐름

### 3.1 예약

```text
메뉴 선택 → 날짜 선택 → 가능 시간 조회 → 로그인 확인
→ 노쇼 정책 동의 → DB 중복 재검사 → 예약 저장
→ 커밋 완료 → Google Calendar 비동기 등록
```

- 예약은 현재보다 30분 이후부터 7일 안에서만 받습니다.
- 관리자가 정한 요일별 영업시간과 특정 휴무일을 단일 기준으로 사용합니다.
- 한 명의 디자이너 일정이므로 서로 다른 시술도 시간이 겹치면 안 됩니다.
- 화면에서 시간을 확인했어도 저장 직전에 DB 잠금과 중복 검사를 다시 합니다.
- 예약 소유권은 변경 가능한 전화번호가 아니라 JWT의 사용자 `id`로 확인합니다.

### 3.2 로그인과 소셜 로그인

```text
일반 로그인 → BCrypt 확인 → JWT 발급
소셜 로그인 → Redis state 확인 → 제공자 사용자 조회
→ 기존 이메일 고객 연결 또는 신규 고객 생성 → JWT 발급
로그아웃 → JWT 남은 시간만큼 Redis 블랙리스트 저장
```

소셜 가입 직후 임시 이메일·연락처라면 예약 전에 고객 정보 입력을 요구합니다.

### 3.3 관리자

관리자는 예약 상태, 시술 메뉴와 이미지, 요일별 영업시간, 특정 휴무일, Calendar 표시를 관리합니다. 운영 규칙은 Java 상수가 아니라 DB와 관리자 화면에서 변경합니다.

## 4. 파일 책임

```text
backend/src/main/java/com/marinboy
├─ controller   요청 검증과 응답
├─ service      예약·인증·업로드·외부 연동 규칙
├─ mapper       MyBatis 메서드 계약
├─ dto          화면과 DB 사이 데이터
├─ security     JWT·Redis·URL 권한
└─ config       비동기 실행·업로드 경로 설정

frontend
├─ pages        URL별 화면과 SSR
├─ features
│  ├─ shared    JWT·JSON·오류 처리를 맡는 공통 API 도구
│  └─ domain    예약·인증·관리자처럼 기능별 API·계산 도구
├─ styles       공통 화면 스타일
└─ tests        권한·예약 규칙·JWT 요청 테스트
```

새 파일을 만들기 전 기존 Service·Mapper·화면에 자연스럽게 들어가는지 먼저 확인합니다. 한두 줄을 재사용하려고 `util`, `common`, `helper` 파일을 만들지 않습니다. 반대로 JWT 헤더·JSON 오류 처리·예약 시간 계산처럼 반복되고 독립적인 기능을 Page나 Service에 복사하지 않습니다.

## 5. 기능 부품과 도구화 기준

이 프로젝트에서 "기능을 가져다 썼다 빼는 구조"는 다음 연결을 뜻합니다.

```text
Page → ReservationApi / AuthApi / AdminApi → JsonApiClient
ReservationScheduleService → ReservationScheduleTool → 영업시간 + 기존 예약
ReservationService → GoogleCalendarService(선택 주입)
```

- Page는 API 주소·JWT 헤더·JSON 파싱을 직접 작성하지 않고 도메인 API 도구를 호출합니다.
- `JsonApiClient`는 HTTP 전송과 공통 오류만 처리하며 예약 규칙을 알지 않습니다.
- `ReservationApi`는 예약 URL과 요청 데이터를 알지만 화면의 `useState`를 알지 않습니다.
- `ReservationScheduleTool`은 DB를 직접 조회하지 않고 전달받은 영업시간·시술시간·예약 목록으로 가능 시간을 계산합니다.
- Google Calendar는 선택 주입하므로 연동을 꺼도 예약 DB 저장 규칙은 바뀌지 않습니다.
- 도구마다 공개 함수와 테스트를 두고, 사용하는 곳이 하나뿐이며 코드가 더 길어지면 분리하지 않습니다.

금지 기준:

- `CommonUtil` 하나에 날짜·HTTP·문자열·인증을 모두 넣지 않습니다.
- Page마다 같은 `fetch → response.ok → response.json → 오류`를 복사하지 않습니다.
- Service 메서드 안에서 후보 시간마다 DB를 다시 조회하지 않습니다.
- 단순한 한 화면 상태를 위해 Redux·Saga를 추가하지 않습니다.
- Thymeleaf와 Next.js를 같은 기능의 활성 화면으로 동시에 유지하지 않습니다.

## 6. 코드 작성 규칙

- Controller는 HTTP 입출력, Service는 업무 규칙, Mapper는 SQL 계약만 담당합니다.
- 주요 기능에는 목적과 예외 조건을 설명하는 짧은 한글 주석을 둡니다.
- Java·JavaScript·CSS·SQL·XML·설정·실행 스크립트는 파일 역할과 핵심 흐름을 한글 주석으로 설명합니다.
- `package.json`, `package-lock.json`, `.oxlintrc.json` 같은 JSON과 JPG·PNG 이미지는 주석 문법이 없으므로 형식을 변경하지 않습니다.
- 초급 학습 코드답게 한 줄에 여러 동작을 숨기지 않고 의미 있는 변수명을 사용합니다.
- MyBatis XML `id`와 Mapper 메서드명은 반드시 일치시킵니다.
- SQL 값은 `${}`가 아니라 `#{}`로 바인딩합니다.
- DB 컬럼은 스네이크 케이스, Java 필드는 카멜 케이스, PK는 `ID`를 사용합니다.
- 외부 API 실패가 DB 예약을 되돌리지 않도록 Calendar는 커밋 후 호출합니다.
- 화면에서 숨기는 것으로 권한 검사를 대신하지 않고 서버에서 ADMIN·소유권을 확인합니다.

## 7. 데이터 모델

```text
MB_USER 1 ── N MB_USER_SOCIAL_ACCOUNT
MB_USER 1 ── N MB_RESERVATION N ── 1 MB_SERVICE_ITEM
MB_SERVICE_ITEM 1 ── N MB_SERVICE_IMAGE
MB_BUSINESS_HOUR        요일별 영업시간
MB_HOLIDAY              특정 휴무일
```

예약에는 고객명·이메일·전화번호를 스냅샷으로 남기되, 조회·수정·취소 권한은 `CUSTOMER_ID`로 확인합니다.

## 8. UX 기준

- 헤더에서 메뉴·디자이너·매장 안내·로그인·예약으로 바로 이동할 수 있어야 합니다.
- 예약 화면은 시술 → 날짜 → 시간 → 동의 순서를 보여 줍니다.
- 실패 메시지는 서버의 구체적인 원인을 사용자에게 표시합니다.
- 모바일 390px부터 데스크톱 1440px까지 가로 스크롤 없이 사용합니다.
- 인라인 스타일을 추가하지 않고 공통 클래스를 재사용합니다.

## 9. 완료 기준

- 비로그인 예약 진입 후 로그인하면 원래 예약 화면으로 돌아온다.
- 일반·소셜 사용자가 프로필을 완성하고 예약할 수 있다.
- 휴무일, 영업시간 밖, 중복 시간, 미동의 예약이 서버에서 차단된다.
- 본인 예약만 조회·수정·취소할 수 있고 관리자 API는 ADMIN만 호출한다.
- DB 커밋 후 Calendar 일정이 생성되며 연동 장애가 예약 저장을 취소하지 않는다.
- 백엔드 테스트, 프론트 테스트·lint·production build가 통과한다.
- Stop → Restart 후 3000·8082·6379와 실제 HTTP 화면이 다시 정상 동작한다.

## 10. 자주 발생하는 실수

- DTO 필드명과 SQL 별칭이 달라 값이 `null`이 되는 문제
- Mapper 메서드명과 XML `id` 불일치
- JWT 인증 화면만 만들고 서버 소유권 검사를 빠뜨리는 문제
- SSR API 주소와 브라우저 API 주소를 혼동하는 문제
- OneDrive의 `.next` 파일 잠금 때문에 재빌드가 실패하는 문제
- Calendar 키를 프로젝트 안에 복사하거나 DB 커밋 전에 외부 API를 호출하는 문제

문제가 생기면 `브라우저 → Controller → Service → Mapper → Oracle/Redis/Calendar` 순서로 첫 실패 경계를 찾습니다.
