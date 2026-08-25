# MarinboySalon 포트폴리오

1인 헤어샵의 필수 기능을 수업 단계에 맞춰 v1, v2, v3로 나눈 학습용 포트폴리오입니다.
현재 개발 정본은 이 저장소 루트이며 모든 소스는 UTF-8로 관리합니다.

## 버전 구성

```text
MarinboySalon_v1/  Spring Boot + JSP + MyBatis + MySQL 기본 예약
MarinboySalon_v2/  v1 구조 + 관리자 메뉴·영업시간·휴무일·예약 상태 관리
MarinboySalon_v3/  front(Next.js) + back(Spring Boot REST API) 완전 분리
```

- [MarinboySalon_v1](MarinboySalon_v1/README.md)
- [MarinboySalon_v2](MarinboySalon_v2/README.md)
- [MarinboySalon_v3](MarinboySalon_v3/README.md)
- [프로젝트 코드 기준](AGENTS.md)
- [참고 저장소 분석과 검증 기준](docs/REFERENCE_ENGINEERING_GUIDE.md)

세 버전은 새 MySQL 데이터베이스 `marinboy_salon`을 함께 사용합니다.
v3는 선생님 `thejoa703`과 Spring Breeze v3처럼 `front/back`을 분리하고 JWT·Redis 인증을 사용합니다.

## 기본 빌드

```powershell
# v1
Set-Location .\MarinboySalon_v1
mvn clean test
Set-Location ..

# v2
Set-Location .\MarinboySalon_v2
mvn clean test
Set-Location ..

# v3 백엔드
Set-Location .\MarinboySalon_v3\back
.\gradlew.bat clean test bootJar
Set-Location ..\..

# v3 프론트엔드
Set-Location .\MarinboySalon_v3\front
npm ci
npm test -- --runInBand --passWithNoTests
npm run build
```

`MarinboySalon_v3_incorrect_backup_20260825`는 구조 수정 전 v3를 복구할 수 있도록 보존한 백업이며 실행 기준이 아닙니다.
전체 검증은 저장소 루트에서 `.\scripts\verify-project.ps1`로 실행합니다.
