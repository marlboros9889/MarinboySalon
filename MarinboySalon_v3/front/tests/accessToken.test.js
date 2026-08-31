import { clearAccessToken, getAccessToken, setAccessToken } from '../api/accessToken';

// 브라우저 저장소를 쓰지 않고 실행 메모리에서만 토큰을 다루는지 확인합니다.
describe('Access Token 메모리 저장소', () => {
  afterEach(() => {
    clearAccessToken();
  });

  test('현재 실행 메모리에만 토큰을 저장하고 삭제한다', () => {
    setAccessToken('access-token');
    expect(getAccessToken()).toBe('access-token');

    clearAccessToken();
    expect(getAccessToken()).toBeNull();
  });
});
