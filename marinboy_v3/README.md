# Marinboy v3 - 1차 완료본

## 실행 구조

```text
frontend (Next.js SSR + Redux/Saga + Bootstrap 5, 3000)
        ↓ /api 프록시
backend  (Spring Boot + Service + MyBatis XML + Oracle, 8082)
        ↓
Oracle XE
```

## 수업 내용 적용

| 수업 요소 | V3 적용 위치 |
|---|---|
| Spring Controller → Service → Mapper | `backend/src/main/java/com/marinboy` |
| MyBatis Mapper XML | `backend/src/main/resources/mybatis/mapper` |
| DTO·검증·트랜잭션 | `dto`, `ReservationService`, `AuthService` |
| Next.js SSR | `frontend/pages/index.js#getServerSideProps` |
| Redux Reducer·Saga·Store | `frontend/reducers`, `sagas`, `store` |
| Bootstrap 5 | `frontend/pages/_app.js` |

## 실행 및 검증

1. `backend`에서 `mvn test`, `mvn spring-boot:run`
2. `frontend`에서 `npm test`, `npm run lint`, `npm run build`, `npm run dev`
3. 고객 화면: `http://127.0.0.1:3000`, API: `http://127.0.0.1:8082/api/services`

모바일 앱은 1차 범위에서 제외하며, 기존 `mobile/` 폴더는 추가 개발하지 않습니다.
