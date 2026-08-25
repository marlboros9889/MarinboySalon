# MarinboySalon_v1

1인 헤어샵의 필수 예약 기능을 처음부터 새로 만드는 포트폴리오 프로젝트입니다.
기존 GitHub 저장소와 Google Sheets는 기능과 학습 방식의 참고 자료로만 사용합니다.

## 기술 구성

- Java 17
- Spring Boot 3.3.5
- Spring MVC
- JSP / JSTL
- MyBatis
- MySQL
- Bootstrap 5 / JavaScript

## 이동과 인코딩 원칙

- 모든 코드와 문서는 UTF-8로 저장합니다.
- Java 파일 이동은 IDE의 `Refactor → Move`로 진행합니다.
- JSP 자원은 컨텍스트 경로, Mapper XML은 classpath 와일드카드로 연결합니다.
- 개인 컴퓨터의 절대경로를 소스에 작성하지 않습니다.

## 실행 준비

1. `database/schema.sql`을 실행하여 새 `marinboy_salon` 데이터베이스를 만든다.
2. `database/sample_data.sql`을 실행하여 기본 메뉴를 등록한다.
3. `DB_USERNAME`, `DB_PASSWORD` 환경 변수를 설정한다.
4. 프로젝트 폴더에서 아래 명령을 실행한다.

```powershell
mvn spring-boot:run
```

5. 브라우저에서 `http://localhost:8080`으로 접속한다.

`marinboy_salon`은 v1, v2, v3가 순서대로 확장하면서 함께 사용하는 새 프로젝트 전용 DB입니다.

필수 기능과 구현 순서는 [docs/01-essential-features.md](docs/01-essential-features.md)에서 확인할 수 있습니다.
