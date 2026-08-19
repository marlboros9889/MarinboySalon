# 포트폴리오 구조 반영표

| 점검 항목 | 반영 위치 | 결과 |
|---|---|---|
| Front·Backend·Docs 분리 | `frontend/`, `backend/`, `docs/` | 완료 |
| 기능별 분류 | `features/auth`, `features/home`, 백엔드 클래스 매핑 | 완료 |
| Reducer | `frontend/reducers/service.js` | 완료 |
| Saga | `frontend/sagas/service.js` | 완료 |
| Store | `frontend/store/configureStore.js` | 완료 |
| SSR | `frontend/pages/index.js#getServerSideProps` | 완료 |

시술 조회 흐름은 `Next SSR → Redux Store hydration → View`, 재조회는 `View → Saga → API → Reducer → Store → View`입니다.
