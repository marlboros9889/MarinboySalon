# marinboy_mybatis


## 목표 구조

```text
src/main/java/com/marinboy
├── api
├── config
├── controller
├── dao
├── dto
├── llmrag
├── security
├── service
└── util

src/main/resources
└── mybatis/mapper
```

## 1차 검증 목표

```text
Oracle 연결 확인
-> TestDao
-> test-mapper.xml
-> SELECT SYSDATE FROM DUAL
-> /api/db-time 응답
```
