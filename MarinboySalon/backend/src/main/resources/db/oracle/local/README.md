# 로컬 Oracle 초기화

개발 DB를 새로 만들 때만 다음 순서로 수동 실행합니다.

1. `01_drop_tables.sql`
2. 상위 폴더의 `../02_create_tables.sql`
3. `03_seed_sample_data.sql`
4. `09_align_identity_values.sql`
5. 상위 폴더의 `../04_verify_tables.sql`

터미널과 SQLcl/SQL*Plus 문자셋은 UTF-8로 맞춥니다. `03_seed_sample_data.sql`의 첫 번째 인수에는 평문이 아닌 BCrypt 관리자 비밀번호 해시를 전달합니다.
