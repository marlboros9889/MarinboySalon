import test from 'node:test';
import assert from 'node:assert/strict';
import {
  canSubmitReservation,
  editableContactValue,
  formatReservationTime,
  getMaximumBookingDate,
} from '../features/reservation/reservationRules.js';

test('소셜 고객은 연락처를 입력한 뒤 시간까지 선택해야 예약할 수 있다', () => {
  const base = {
    user: { profileComplete: false },
    serviceId: '1',
    date: '2026-08-24',
    reservationDateTime: '2026-08-24T10:00:00',
    noShowPolicyAgreed: true,
    submitting: false,
  };

  assert.equal(canSubmitReservation(base), false);
  assert.equal(canSubmitReservation({ ...base, user: { profileComplete: true } }), true);
  assert.equal(canSubmitReservation({ ...base, user: { profileComplete: true }, reservationDateTime: '' }), false);
  assert.equal(canSubmitReservation({ ...base, user: { profileComplete: true }, noShowPolicyAgreed: false }), false);
});

test('소셜 임시 연락처는 수정 폼에서 빈 값으로 보여 준다', () => {
  assert.equal(editableContactValue('social_a@social.marinboy.local', 'email'), '');
  assert.equal(editableContactValue('SOCIAL_REQUIRED', 'phone'), '');
  assert.equal(editableContactValue('customer@example.com', 'email'), 'customer@example.com');
});

test('예약 마지막 날짜는 오늘을 포함해 7일 뒤이다', () => {
  assert.equal(getMaximumBookingDate('2026-08-21'), '2026-08-28');
});

test('예약 시간 목록에는 날짜를 제외한 시간만 표시한다', () => {
  const formattedTime = formatReservationTime('2026-08-24T10:30:00');
  assert.match(formattedTime, /10:30/);
  assert.doesNotMatch(formattedTime, /2026|8월|24/);
});
