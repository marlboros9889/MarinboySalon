import test from 'node:test';
import assert from 'node:assert/strict';
import { isAdminUser } from '../features/admin/adminRules.js';

// 관리자 화면의 1차 진입 판단이 역할 값과 비로그인 상태를 정확히 구분하는지 검증합니다.
test('ADMIN 사용자만 관리자 화면을 열 수 있다', () => {
  assert.equal(isAdminUser({ role: 'ADMIN' }), true);
  assert.equal(isAdminUser({ role: 'CUSTOMER' }), false);
  assert.equal(isAdminUser(null), false);
});
