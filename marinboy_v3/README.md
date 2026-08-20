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

## 폴더 정리 기준

```text
marinboy_v3/
├─ backend/       Spring Boot API, MyBatis Mapper, DB 설정, 백엔드 테스트
├─ frontend/      Next.js 화면, Redux/Saga 상태, 정적 이미지, 프런트 테스트
├─ mobile/        Expo 관리자 앱 소스와 이미지
├─ docs/          구조·기능 흐름·검증 문서
├─ uploads/       서비스 화면에서 사용하는 업로드 이미지
├─ scripts/       개발 환경 정리와 점검 명령
├─ compose.yaml   로컬 컨테이너 실행 설정
└─ README.md      프로젝트 실행·폴더 안내
```

실제 기능 파일은 `backend`, `frontend`, `mobile` 안에서만 찾고, `target`, `.next`, `node_modules` 같은 재생성 폴더는 소스가 아닙니다.

| 구분 | 보존 대상 | 정리 대상 |
|---|---|---|
| 백엔드 | `backend/src`, `backend/pom.xml`, `backend/uploads` | `backend/target`, `backend/.settings` |
| 프런트엔드 | `frontend/pages`, `features`, `public`, `tests` | `.next`, `dist`, `logs`, `.playwright-cli`, 비어 있는 `src` |
| 모바일 | `mobile/src`, `assets`, 설정 파일 | `.expo`, `dist-verify` |
| 공통 | `docs`, `uploads`, `compose.yaml` | `.metadata`, 최상위 `target`, 비어 있는 `src`, `bin` |

`uploads/`에는 서비스 화면에서 사용하는 이미지가 포함되어 있으므로 정리 대상이 아닙니다.

### 재생성 파일 정리

서버를 종료한 뒤 아래 명령으로 삭제 대상을 먼저 확인합니다.

```powershell
.\scripts\clean-generated.ps1 -WhatIf
```

확인 후 실제로 삭제합니다. `node_modules`까지 지우려면 `-IncludeDependencies`를 추가하고 이후 `npm install`을 실행합니다.

```powershell
.\scripts\clean-generated.ps1
.\scripts\clean-generated.ps1 -IncludeDependencies
```
