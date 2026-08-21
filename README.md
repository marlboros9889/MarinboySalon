# Marinboy Salon

두 버전은 인증 방식과 실행 포트를 분리한 독립 프로젝트입니다.

| 버전 | 화면/서버 | 인증 | Redis | 실행 포트 |
|---|---|---|---|---|
| [`marinboy_v2/`](marinboy_v2/) | Thymeleaf + Spring Boot + MyBatis | `HttpSession` + 전용 쿠키 | 사용하지 않음 | 8081 |
| [`marinboy_v3/`](marinboy_v3/) | Next.js + Spring Boot + MyBatis/JPA | Stateless JWT + Redis 로그아웃 차단 | 필수 | 3000 / 8082 |

같은 PC에서 함께 실행해도 v2의 `MARINBOY_V2_SESSION` 쿠키와 v3의 Bearer 토큰이 서로 영향을 주지 않습니다. 각 버전의 설치·실행·검증 방법은 해당 폴더 README를 확인합니다.
