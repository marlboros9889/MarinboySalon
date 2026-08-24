import test from 'node:test';
import assert from 'node:assert/strict';
import { adminApi } from '../features/admin/adminApi.js';

function prepareBrowserStorage() {
  global.window = {
    localStorage: {
      getItem() { return 'admin-token'; },
      removeItem() {},
    },
  };
}

// 관리자 변경 뒤 전체 대시보드가 아니라 영향받은 도메인 API 하나만 재조회할 수 있어야 합니다.
test('예약 목록 도구는 예약 API 한 곳만 호출한다', async () => {
  prepareBrowserStorage();
  const requestedUrls = [];
  global.fetch = async (url) => {
    requestedUrls.push(url);
    return new Response(JSON.stringify({ items: [], total: 0 }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    });
  };

  await adminApi.reservations(2);

  assert.deepEqual(requestedUrls, ['/api/admin/reservations?page=2&size=5']);
});

// 시술 메뉴 삭제는 ADMIN JWT와 DELETE 메서드를 함께 보내야 서버의 논리 삭제 API에 도달합니다.
test('시술 메뉴 삭제 도구는 관리자 DELETE API를 호출한다', async () => {
  prepareBrowserStorage();
  let requestedUrl = '';
  let requestedOptions = null;
  global.fetch = async (url, options) => {
    requestedUrl = url;
    requestedOptions = options;
    return new Response(null, { status: 204 });
  };

  await adminApi.deleteService(7);

  assert.equal(requestedUrl, '/api/admin/services/7');
  assert.equal(requestedOptions.method, 'DELETE');
  assert.equal(requestedOptions.headers.get('Authorization'), 'Bearer admin-token');
});
