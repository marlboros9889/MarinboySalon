import { clearAccessToken, getAccessToken, setAccessToken } from '../api/accessToken';

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
