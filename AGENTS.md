# MarinboySalon 프로젝트 기준

이 문서는 이 저장소에서 코드 생성·수정·리뷰할 때 가장 먼저 적용하는 기준이다.

## 1. 적용 우선순위와 예외 중단

1. 사용자의 현재 명시적 요청
2. 해당 버전 README, 기존 API·DB 계약, 현재 코드 흐름
3. 이 문서와 `docs/REFERENCE_ENGINEERING_GUIDE.md`
4. 외부 참고 저장소

네이밍, 기술 스택, URL, DB 계약, 인증 정책에서 벗어날 가능성이 있으면 구현을 일시 중단한다. `차이점 → 영향 → 기준에 맞는 대안`을 먼저 제시하고, 사용자가 임시 예외를 명시한 경우에만 최소 범위로 진행한다. 임시 예외를 다른 기능에 복제하지 않는다.

## 2. 버전별 역할

- `MarinboySalon_v1`: Spring Boot + JSP + MyBatis + MySQL의 기본 회원·서비스·예약 흐름
- `MarinboySalon_v2`: v1 구조를 유지하며 관리자·영업시간·휴무일·예약 상태 확장
- `MarinboySalon_v3`: Next.js 화면과 Spring Boot REST API를 분리하고 JWT·Redis 인증 사용
- `MarinboySalon_v3_incorrect_backup_*`: 로컬 복구 전용. 실행·수정·검증·커밋 대상이 아니다.

버전 간 파일을 복사하기 전에 기능 책임과 데이터 계약을 비교한다. 상위 버전 기능을 하위 버전에 자동 역이식하지 않는다.

## 3. 기능 구현 순서

요구사항의 정상·실패·권한 흐름을 적고 다음 순서를 지킨다.

- v1/v2: `JSP → Controller → Service → Dao → Mapper XML → MySQL`
- v3: `Page/Component → Redux REQUEST/SUCCESS/FAILURE → Saga → Axios → RestController → Service → Mapper → MySQL/Redis`

화면만 만들거나 HTTP 200만 확인해 완료 처리하지 않는다. URL, 요청/응답 DTO, DB 변경, 목록 재조회까지 연결한다.

## 4. 네이밍과 구조

- Java 클래스: `{Domain}Controller`, `{Domain}Service`, `{Domain}ServiceImpl`, `{Domain}Dao`, `{Domain}Dto`
- v3 API는 입력·출력을 `{Domain}RequestDto`, `{Domain}ResponseDto`로 분리한다.
- Java 변수는 카멜 케이스, DB 컬럼은 스네이크 케이스, PK는 `id`, FK는 `{table}_id`를 쓴다.
- JSP는 `/WEB-INF/views/{domain}/{action}.jsp`, 기본 액션은 `list`, `insertForm`, `insert`, `detail`, `updateForm`, `update`, `delete`를 사용한다.
- Mapper XML `namespace`는 인터페이스 전체 경로, SQL `id`는 메서드명과 일치시킨다.
- 한 파일·메서드는 한 책임을 우선한다. 중복 코드는 공통 도구로 빼되 한 번만 쓰는 코드를 성급히 추상화하지 않는다.

## 5. 코드 안전성

- SQL은 필요한 컬럼만 명시하고 `${}`를 쓰지 않는다. 사용자 값은 `#{}`로 바인딩한다.
- 예약 생성은 영업시간 잠금, 휴무일, 기존 시간 중복을 하나의 트랜잭션에서 검사한다.
- 인증 없음은 401, 권한 부족은 403으로 구분한다. 비밀번호·JWT 비밀키·DB 비밀번호를 커밋하지 않는다.
- CORS 허용 주소와 실행 환경 값은 설정으로 관리한다.
- JSP/정적 파일은 컨텍스트 경로를 사용하고, Ajax는 기준 URL을 한 곳에서 관리한다.
- 인라인 스타일 대신 Bootstrap 유틸리티 또는 재사용 CSS 클래스를 쓴다.
- Controller, Service, Mapper, JSP, JS의 중요한 처리 흐름에는 이유가 드러나는 한글 주석을 단다.

## 6. 완료 조건

- URL 중복, Mapper 메서드/XML, DTO/DB 매핑, 상대경로, 입력 검증을 정적 점검한다.
- v1/v2 Maven 테스트, v3 Gradle 테스트·Jest·Next 빌드를 통과한다.
- 변경 기능의 실제 브라우저 흐름과 API 상태 코드, MySQL/Redis 변경, 재시작 후 동작을 확인한다.
- 생성물(`target`, `build`, `.gradle`, `.next`, `node_modules`)과 로컬 백업은 Git에 넣지 않는다.
- PR에는 변경 이유, 검증 명령, 남은 제한을 기록하고 검토된 경로만 명시적으로 스테이징한다.
