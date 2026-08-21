import test from 'node:test';
import assert from 'node:assert/strict';
import { isAdminUser } from '../features/admin/adminRules.js';

test('ADMIN 사용자만 관리자 화면을 열 수 있다', () => {
  assert.equal(isAdminUser({ role: 'ADMIN' }), true);
  assert.equal(isAdminUser({ role: 'CUSTOMER' }), false);
  assert.equal(isAdminUser(null), false);
});
