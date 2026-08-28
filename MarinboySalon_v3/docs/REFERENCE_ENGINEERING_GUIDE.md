# 참고 저장소 분석과 프로젝트 적용 기준

## 분석 기준점

2026-08-25에 다음 저장소의 최신 기본 브랜치를 코드 기준으로 확인했다.

| 저장소 | 확인 커밋 | 가져올 기준 |
|---|---|---|
| [thejoa703](https://github.com/sally03915/thejoa703) | `6aa4b779` | Git부터 Spring·API·React로 이어지는 학습 순서 |
| [2026-AI-FULLSTACK](https://github.com/sally03915/2026-AI-FULLSTACK) | `d9973e4` | Java·DB·JSP·Spring·React 단계 구분 |
| [thejoa703-2](https://github.com/sally03915/thejoa703-2) | `8f1aa18a` | DTO, Service 트랜잭션, JWT·Redis 연결 |
| [thejoa703-3](https://github.com/sally03915/thejoa703-3) | `e0b8943a` | Next.js, Redux-Saga, Axios의 요청 흐름 |
| [spring-breeze-erp](https://github.com/yoonguri988/spring-breeze-erp) | `02bc46f2` | MVC/JSP에서 REST/Next로 확장하는 버전 분리 |

참고 코드는 증거이지 자동 정답이 아니다. 현재 프로젝트의 버전 목적과 계약에 맞는 패턴만 채택한다.

## 채택한 공통 구조

| 구분 | 프로젝트 기준 |
|---|---|
| 기능 순서 | 요구사항 → URL/DTO → DB → Mapper → Service → Controller → 화면 → 검증 |
| 계층 분리 | Controller는 입출력, Service는 규칙·트랜잭션, Mapper는 DB 접근만 담당 |
| 프론트 연결 | REQUEST/SUCCESS/FAILURE를 구분하고 실패 화면과 디버깅 근거를 남김 |
| 확장성 | v1 기본 CRUD, v2 관리자 규칙, v3 REST/JWT/Redis로 단계별 확장 |
| 간결성 | 의미 있는 쉬운 이름, 짧은 메서드, 실제 중복만 공통화 |
| 도구화 | 한 명령 전체 검증, 안전한 생성물 정리, 환경값 외부화 |
| 완료 판정 | 코드뿐 아니라 브라우저·API·DB·Redis·재시작 경로 확인 |

## 채택하지 않는 패턴

- `SELECT *`, MyBatis `${}` 문자열 치환, 실제 비밀값 하드코딩
- 인증 경로 전체 `permitAll`, 401과 403 혼용
- 테스트를 건너뛴 빌드·배포를 정상 검증으로 표시
- 비동기 요청 성공 전에 화면 이동, 실패 처리 없는 Ajax/Saga
- 인라인 스타일 반복, 기계별 절대경로, 생성물·의존성·복구 백업 커밋
- 도메인 규칙을 Controller나 화면에 중복 구현

## 변경 전 점검표

1. 어느 버전의 어떤 사용자 흐름인지 정한다.
2. 정상, 입력 오류, 인증 없음, 권한 부족, 동시 요청 결과를 정한다.
3. 기존 URL·DTO·DB·Mapper 계약과 충돌하는지 확인한다.
4. 기존 네이밍과 계층으로 구현할 수 없으면 예외 중단 절차를 따른다.
5. 테스트와 브라우저·DB 확인 방법을 구현 전에 정한다.

## 버전별 검증 범위

- v1: 회원가입·로그인, 서비스 목록, 예약 생성과 MySQL 저장
- v2: v1 기능 + 관리자 권한, 서비스 논리 삭제, 영업시간·휴무일, 예약 상태 변경
- v3: v2 도메인 + JWT 쿠키, Redis 세션/갱신/로그아웃, REST 상태 코드, CORS, Redux 화면 연결
- 예약 동시성: 같은 시간 요청은 한 건만 저장되어야 한다.
- 인증: 비로그인 401, 로그인했지만 권한 부족 403이어야 한다.

## 파일 정리 기준

- 삭제 가능 생성물: `target`, `build`, `.gradle`, `.next`, `coverage`, 로그
- 다시 설치 가능한 의존성: `node_modules`는 `-IncludeDependencies`를 명시한 경우만 정리
- 보존 대상: 소스, 설정 예제, DB 스크립트, Wrapper, 문서, 테스트
- 복구 백업: 내용 확인 없이 삭제하지 않고 Git에서 제외한다.

저장소 루트의 `scripts/verify-project.ps1`과 `scripts/clean-generated.ps1`을 표준 도구로 사용한다.
