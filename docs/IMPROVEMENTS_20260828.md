# 2026-08-28 개선 사항

## 보안
- JWT: 32자 미만 또는 로컬 기본 secret 을 prod/stage 에서 사용하면 기동 실패 (`JwtSecretValidator`)
- 운영에서는 `JWT_SECRET`, `DB_PASSWORD` 환경 변수 필수
- v2 관리자 경로에 `AdminAuthInterceptor` 추가 (Controller 검사와 이중 방어)

## 예약
- `ReservationStatus` enum + 허용 상태 전이
- `ReservationSlotSupport` 로 가능 시간 조회와 검증 규칙 공유 (30분 슬롯)
- `countOverlapForUpdate` + 영업시간 행 잠금으로 동시성 완화
- 핵심 예약 이벤트 INFO 로그

## API / 운영
- Spring Actuator `health`, `info` (`/actuator/health`)
- OpenAPI/Swagger Bearer JWT (`/swagger-ui/index.html`)
- GlobalExceptionHandler: IllegalStateException(409), 예상치 못한 오류(500), 로깅

## v1
- 예약 수정/취소 시 IllegalArgumentException 을 화면 메시지로 반환
- 문서: v1은 영업시간·휴무일 검사 없음 (시간 겹침만)

## 메뉴 이미지
- DB에는 URL만 저장. 파일 업로드/S3 연동은 후속 과제.
