/**
 * 관리자 페이지에 직접 접근했을 때의 이동 주소를 한 곳에서 결정합니다.
 * 서버 권한 검사는 유지하고, 화면에서는 빈 목록이나 오류 대신 자연스럽게 이동시킵니다.
 */
export function getAdminRedirectTarget(me, returnTo) {
  if (!me) {
    return `/auth/login?returnTo=${encodeURIComponent(returnTo)}`;
  }

  if (me.role !== 'ADMIN') {
    return '/';
  }

  return null;
}
