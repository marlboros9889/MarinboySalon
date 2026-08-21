# Marinboy v2

Spring Boot + Thymeleaf + MyBatis + Oracle 기반의 서버 세션 버전입니다. v3의 JWT·Redis 설정을 사용하지 않습니다.

## 실행 구조

```text
브라우저 → Spring Boot :8081 → Controller → Service → Dao → MyBatis XML → Oracle
                  └─ MARINBOY_V2_SESSION(HttpSession)
```

## 환경설정과 실행

`.env.example`의 `V2_ORACLE_*` 값을 실행 환경변수로 지정합니다. 실제 비밀번호 파일은 Git에 올리지 않습니다.

```powershell
$env:V2_ORACLE_URL='jdbc:oracle:thin:@localhost:1521/XEPDB1'
$env:V2_ORACLE_USERNAME='marinboy'
$env:V2_ORACLE_PASSWORD='실제 비밀번호'
mvn clean test
mvn -DskipTests package
java -jar target\marinboy-v2-0.0.1-SNAPSHOT.jar
```

- 고객 화면: `http://127.0.0.1:8081/`
- 기본 업로드 폴더: `${user.home}/.marinboy/uploads` (같은 Oracle 시술 이미지 URL을 v3와 공유)
- 전체 정적·빌드 검증: `.\scripts\verify-project.ps1`

v2의 소셜 로그인도 Spring Security 세션을 사용합니다. Redis와 `JWT_SECRET`은 설정하지 않습니다.
