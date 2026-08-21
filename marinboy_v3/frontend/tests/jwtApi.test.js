import test from 'node:test';
import assert from 'node:assert/strict';
import { clearAccessToken, jwtFetch, saveAccessToken } from '../features/shared/api/jwtApi.js';

function prepareBrowserStorage() {
  const values = new Map();
  global.window = {
    localStorage: {
      getItem(key) {
        return values.get(key) || null;
      },
      setItem(key, value) {
        values.set(key, value);
      },
      removeItem(key) {
        values.delete(key);
      },
    },
  };
}

test('만료 JWT가 남아 있어도 공개 인증 요청에는 Authorization을 보내지 않는다', async () => {
  prepareBrowserStorage();
  saveAccessToken('expired-token');

  const authorizationHeaders = [];
  global.fetch = async (_url, options) => {
    authorizationHeaders.push(options.headers.get('Authorization'));
    return new Response('{}', { status: 200 });
  };

  await jwtFetch('/api/auth/login', { method: 'POST' });
  await jwtFetch('/api/auth/signup', { method: 'POST' });
  await jwtFetch('/api/auth/check-username?username=qa');
  await jwtFetch('/api/auth/check-email?email=qa%40example.test');
  await jwtFetch('/api/auth/social/providers');

  assert.deepEqual(authorizationHeaders, [null, null, null, null, null]);
  clearAccessToken();
});

test('보호 API 요청에는 저장된 JWT를 Authorization으로 전달한다', async () => {
  prepareBrowserStorage();
  saveAccessToken('valid-token');

  let authorizationHeader = null;
  global.fetch = async (_url, options) => {
    authorizationHeader = options.headers.get('Authorization');
    return new Response('{}', { status: 200 });
  };

  await jwtFetch('/api/auth/me');

  assert.equal(authorizationHeader, 'Bearer valid-token');
  clearAccessToken();
});
