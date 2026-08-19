# 로컬 Oracle 시드 실행

- 이 폴더의 초기화·데모 SQL은 로컬 개발에서만 수동 실행합니다.
- 터미널과 SQLcl/SQL*Plus 문자셋을 UTF-8로 맞춥니다.
- `ADMIN_PASSWORD_BCRYPT`에는 평문이 아닌 BCrypt 해시를 환경 변수로 설정합니다.
- 실행 예: `sql ... @03_seed_sample_data.sql "$env:ADMIN_PASSWORD_BCRYPT"`
- 운영 배포에서는 이 폴더의 SQL을 실행하지 않고 관리자 계정을 별도 프로비저닝합니다.
