import { getAdminRedirectTarget } from '../utils/adminAccess';

describe('관리자 화면 접근 가드', () => {
  test('비로그인 사용자는 로그인 후 원래 관리자 화면으로 돌아간다', () => {
    expect(getAdminRedirectTarget(null, '/admin/users'))
      .toBe('/auth/login?returnTo=%2Fadmin%2Fusers');
  });

  test('일반 고객은 홈으로 이동하고 관리자는 현재 화면에 남는다', () => {
    expect(getAdminRedirectTarget({ role: 'CUSTOMER' }, '/admin/users')).toBe('/');
    expect(getAdminRedirectTarget({ role: 'ADMIN' }, '/admin/users')).toBeNull();
  });
});
