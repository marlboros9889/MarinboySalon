# Backend

- 기술: Spring Boot, MyBatis/JPA, Oracle, Spring Security JWT, Redis
- 포트: `8082`
- 단독 테스트: `mvn test`
- 단독 패키징: `mvn -DskipTests package`

비밀값은 `src/main/resources`에 저장하지 않습니다. 프로젝트 루트의 `.env.local`과 `scripts/run-dev.ps1`을 사용하거나 외부 설정 파일의 절대 경로를 `MARINBOY_CONFIG_FILE`로 전달합니다.

이 백엔드는 `STATELESS` 방식입니다. 보호 API는 Bearer JWT를 요구하며 로그아웃 토큰은 Redis에서 차단합니다.

전체 실행은 프로젝트 루트에서 다음 명령을 사용합니다.

```powershell
.\scripts\run-dev.ps1 -Action Restart
```
