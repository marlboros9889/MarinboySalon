import test from 'node:test';
import assert from 'node:assert/strict';
import {
  canSubmitReservation,
  editableContactValue,
  getMaximumBookingDate,
} from '../features/reservation/reservationRules.js';

test('소셜 고객은 연락처를 입력한 뒤 시간까지 선택해야 예약할 수 있다', () => {
  const base = {
    user: { profileComplete: false },
    serviceId: '1',
    date: '2026-08-24',
    reservationDateTime: '2026-08-24T10:00:00',
    submitting: false,
  };

  assert.equal(canSubmitReservation(base), false);
  assert.equal(canSubmitReservation({ ...base, user: { profileComplete: true } }), true);
  assert.equal(canSubmitReservation({ ...base, user: { profileComplete: true }, reservationDateTime: '' }), false);
});

test('소셜 임시 연락처는 수정 폼에서 빈 값으로 보여 준다', () => {
  assert.equal(editableContactValue('social_a@social.marinboy.local', 'email'), '');
  assert.equal(editableContactValue('SOCIAL_REQUIRED', 'phone'), '');
  assert.equal(editableContactValue('customer@example.com', 'email'), 'customer@example.com');
});

test('예약 마지막 날짜는 오늘을 포함해 7일 뒤이다', () => {
  assert.equal(getMaximumBookingDate('2026-08-21'), '2026-08-28');
});
