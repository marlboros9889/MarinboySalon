/** 관리자 화면은 서버 권한 검증에 더해 고객에게 운영 UI 자체도 노출하지 않습니다. */
export function isAdminUser(user) {
  return user?.role === 'ADMIN';
}
