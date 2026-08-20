# V3 1차 완료 검증 기록

## 범위

- 고객 시술 조회·예약·예약 수정·고객 정보 수정
- 관리자 예약 상태·메뉴·정보 수정·SSE 알림
- Spring Boot, MyBatis XML, Oracle, Next.js SSR, Redux/Saga, Bootstrap 5
- 모바일 신규 개발과 실제 결제 연동은 제외

## 검증 결과 (2026-08-19)

| 구분 | 명령 또는 확인 | 결과 |
|---|---|---|
| Backend | `mvn test` | 17개 통과 |
| Frontend | `npm test` | 2개 통과 |
| Frontend | `npm run lint` | 경고 0건 |
| Frontend | `npm run build` | 성공 |
| Runtime | `/api/services`, `/` | 각 HTTP 200 |

## 확인한 수업 흐름

`Controller → Service → Mapper 인터페이스 → MyBatis XML → Oracle` 흐름으로 예약과 메뉴 기능을 처리하고, 화면은 `Next SSR → Redux Store → React View`와 `View → Saga → API → Reducer` 흐름을 사용한다.
