# 1인 헤어샵 필수 기능

Google Sheets의 요구사항과 선생님 GitHub의 작성 흐름은 참고 기준으로만 사용한다.
기존 코드를 복사하지 않고 `MarinboySalon_v1`에서 모든 기능을 새로 작성한다.

## 구현 순서

1. 서비스 메뉴 조회
   - 고객이 시술명, 가격, 소요 시간을 확인한다.
   - 관리자가 서비스 메뉴를 등록, 수정, 삭제한다.
2. 회원 가입과 로그인
   - 일반 회원 가입과 로그인을 먼저 구현한다.
   - 로그인 사용자는 자신의 정보만 수정한다.
3. 예약 가능 시간 조회
   - 영업시간, 휴무일, 기존 예약을 함께 검사한다.
4. 예약 등록과 내 예약 관리
   - 예약 등록, 목록, 변경, 취소를 구현한다.
   - 서버에서 로그인 사용자와 예약 소유자를 반드시 확인한다.
5. 관리자 예약 관리
   - 관리자가 예약 상태를 접수, 확정, 완료, 취소로 변경한다.
6. 관리자 영업일 관리
   - 요일별 영업시간과 휴무일을 화면과 DB에서 관리한다.
7. 확장 기능
   - 기본 예약 흐름을 완성한 뒤 소셜 로그인과 Google Calendar 연동을 추가한다.

## 공통 작성 규칙

- `ServiceItemController`, `ServiceItemService`, `ServiceItemServiceImpl`, `ServiceItemDao`, `ServiceItemDto`처럼 수업 예제 네이밍을 그대로 따른다.
- 주요 Java, JSP, JavaScript, CSS, SQL, Mapper XML에는 역할을 설명하는 한글 주석을 작성한다.
- URL과 파일 이름은 독자적으로 줄이지 않고 예측 가능한 이름을 사용한다.
- MyBatis SQL 값은 `${}` 대신 `#{}`를 사용한다.
- JSP 자원 경로는 `${pageContext.request.contextPath}`로 시작한다.
- 인라인 스타일을 사용하지 않고 Bootstrap 클래스를 우선 사용한다.
- 기능 하나를 Controller부터 DB와 화면까지 완성한 뒤 다음 기능으로 이동한다.

## 파일 이동과 UTF-8 고정 규칙

- Java 클래스는 탐색기에서 옮기지 않고 IDE의 `Refactor → Move`를 사용한다.
- Dao 패키지를 이동하면 Mapper XML의 `namespace`도 같은 경로로 변경한다.
- JSP와 정적 자원은 상대경로(`../`)를 사용하지 않고 `${pageContext.request.contextPath}`로 연결한다.
- Mapper XML은 개별 실제 경로가 아니라 `classpath:mappers/**/*.xml`로 찾는다.
- 로컬 컴퓨터의 절대경로를 코드에 작성하지 않고 환경 변수 또는 설정값으로 전달한다.
- `.editorconfig`, `.gitattributes`, Maven, Spring 설정에서 UTF-8을 고정한다.
- 기존 프로젝트 DB를 재사용하지 않고 새로 만든 `marinboy_salon`을 v1, v2, v3가 함께 사용한다.

## v2 추가 범위

- 관리자가 시술 메뉴를 등록, 수정, 비활성화한다.
- 관리자가 요일별 영업시간과 정기 휴무를 수정한다.
- 관리자가 날짜별 임시 휴무일을 등록하고 삭제한다.
- 예약 등록과 수정 시 영업시간, 휴무일, 기존 예약을 서버에서 함께 검사한다.
- 관리자가 전체 예약을 확인하고 접수, 확정, 완료, 취소 상태로 변경한다.
