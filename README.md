# Marinboy Salon

Marinboy Salon 프로젝트의 버전별 소스와 실행 자료를 한 저장소에서 관리합니다.

| 버전 | 폴더 | 설명 |
|---|---|---|
| v2 | [`marinboy_v2/`](marinboy_v2/) | Spring Boot, Thymeleaf, MyBatis 기반 기본 베이스 |
| v3 | [`marinboy_v3/`](marinboy_v3/) | React 포트폴리오 UI, JWT·Redis·JPA 및 관리자 기능 확장 버전 |

## 실행

### v2

```powershell
cd marinboy_v2
mvn spring-boot:run
```

### v3 백엔드

```powershell
cd marinboy_v3
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### v3 프론트엔드

```powershell
cd marinboy_v3/frontend
npm install
npm run dev
```

기능 흐름과 최종 점검 결과는 [`marinboy_v3/docs/`](marinboy_v3/docs/)에서 확인할 수 있습니다.
