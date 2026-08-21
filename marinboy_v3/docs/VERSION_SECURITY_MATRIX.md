# v2·v3 인증 분리 기준

수업 참고 저장소 [`spring-breeze-erp`](https://github.com/yoonguri988/spring-breeze-erp)의 버전 구성을 확인해 v2는 세션, v3는 Stateless JWT + Redis로 분리했습니다.

| 점검 항목 | v2 | v3 |
|---|---|---|
| SecurityContext 저장 | `HttpSession` | 요청마다 JWT 필터가 복원 |
| 클라이언트 인증값 | `MARINBOY_V2_SESSION` 쿠키 | `Authorization: Bearer ...` |
| 세션 정책 | 기본 세션 | `SessionCreationPolicy.STATELESS` |
| Redis | 사용하지 않음 | 로그아웃 토큰 블랙리스트 필수 |
| CSRF | 세션 폼 요청 보호 | 비활성화, 쿠키 인증 미사용 |
| 소셜 로그인 | Spring OAuth2 세션 | 제외(세션 충돌 방지) |
| 실행 포트 | 8081 | 백엔드 8082, 프론트 3000 |
| 업로드 | `${user.home}/.marinboy/uploads` | `${user.home}/.marinboy/uploads` |

v3 소스에는 `HttpSession`, `getSession()` 또는 세션 사용자 상수가 없어야 합니다. 인증 사용자 정보는 `Authentication`의 `UserDto` principal에서만 읽습니다.

두 버전이 현재 같은 Oracle 시술 데이터를 사용하므로 이미지 파일도 같은 고정 폴더를 사용합니다. 인증 상태는 공유하지 않습니다.
