import test from 'node:test';
import assert from 'node:assert/strict';
import {
  clearAccessToken,
  jwtFetch,
  requestJson,
  saveAccessToken,
} from '../features/shared/api/jwtApi.js';

// Node 테스트에서도 브라우저와 같은 방식으로 JWT 저장·삭제 흐름을 확인할 수 있게 가짜 저장소를 준비합니다.
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

// 로그인·회원가입처럼 공개된 인증 API에 만료 토큰이 섞여 401 원인을 흐리지 않는지 검증합니다.
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

// 보호 API에는 저장된 토큰이 Bearer 헤더로 전달되어 서버 인증 필터와 연결되는지 검증합니다.
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

// 모든 화면이 서버 메시지를 같은 방식으로 받도록 JSON 오류 도구의 계약을 검증합니다.
test('JSON 요청 실패는 서버 메시지와 상태 코드를 오류로 전달한다', async () => {
  prepareBrowserStorage();
  global.fetch = async () => new Response(JSON.stringify({ message: '예약 시간이 겹칩니다.' }), {
    status: 409,
    headers: { 'Content-Type': 'application/json' },
  });

  await assert.rejects(
    () => requestJson('/api/reservations'),
    (error) => error.message === '예약 시간이 겹칩니다.' && error.status === 409,
  );
});
