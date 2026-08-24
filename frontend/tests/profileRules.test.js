import test from 'node:test';
import assert from 'node:assert/strict';
import { editableContactValue } from '../features/auth/profileRules.js';

// 소셜 임시 연락처는 고객이 실제 값으로 바꿀 수 있도록 빈 입력값으로 변환합니다.
test('소셜 임시 연락처는 수정 폼에서 빈 값으로 보여 준다', () => {
  assert.equal(editableContactValue('social_a@social.marinboy.local', 'email'), '');
  assert.equal(editableContactValue('SOCIAL_REQUIRED', 'phone'), '');
  assert.equal(editableContactValue('customer@example.com', 'email'), 'customer@example.com');
});
