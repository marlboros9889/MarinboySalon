# 검증 기록

검증일: 2026-08-24

## 자동 검증

- Spring Boot + 실제 Oracle 통합·단위 테스트 30개: 통과
- 일반 로그인·JWT·ADMIN 권한: 통과
- 다중 소셜 제공자 고객 연결: 통과
- 예약 생성·수정·취소, 중복·영업일 규칙: 통과
- Google Calendar 이벤트 변환·표시 설정: 통과
- Next.js 화면·API 도구 테스트 12개: 통과
- oxlint와 production build: 통과
- JAR 비밀 파일 포함 검사: 통과

실행 명령:

```powershell
.\scripts\verify-project.ps1
```

## 실제 사용자 흐름

다음 브라우저 흐름을 실제 3000·8082·Oracle 연결로 확인했습니다.

1. 비로그인 상태에서 예약 진입
2. 로그인 화면 복귀 주소 유지
3. 회원가입·로그인
4. 시술·날짜·시간 선택
5. 노쇼 정책 동의
6. 예약 제출과 나의 예약 확인
7. 데스크톱 1440px·모바일 390px 반응형 확인

최종 리팩토링 검증에서는 다음 항목을 추가로 확인했습니다.

- 홈 SSR에서 Oracle 시술 메뉴와 TOP5 출력
- 비로그인 예약 버튼 클릭 시 `/?returnTo=%2Freservation#login` 유지
- 잘못된 로그인 요청의 서버 오류 메시지 표시
- 비로그인 `/admin` 접근 시 관리자 로그인 안내
- `/api/services/1/available-slots` 200 응답과 실제 시간 목록 출력
- 모바일 390px에서 문서 너비와 화면 너비가 같아 가로 넘침 없음
- 후보 시간마다 DB를 조회하지 않고 날짜별 예약을 한 번만 조회
- 검증되지 않은 소셜 이메일의 기존 계정 자동 연결 차단
- 프로필 폼·예약 시간 조회·관리자 영역별 재조회의 공용 도구 연결
- 업로드 이미지 형식 검증·저장·롤백 정리 도구 검증

Google Calendar 연동을 켠 환경에서는 실제 일정 생성까지 확인했습니다. 재검증 시 새 Calendar 일정이 생성될 수 있으므로 테스트 계정과 시간을 먼저 확인합니다.

## 재시작 기준

```powershell
.\scripts\run-dev.ps1 -Action Restart -Production -StartDependencies
.\scripts\run-dev.ps1 -Action Status
```

- Frontend: `http://127.0.0.1:3000`
- Backend: `http://127.0.0.1:8082/api/services`
- Redis: `127.0.0.1:6379`

코드 존재나 HTTP 200만으로 완료하지 않고 SSR 메뉴 내용, 보호 API 권한, Oracle 결과와 브라우저 화면을 함께 확인합니다.
