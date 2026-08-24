import test from 'node:test';
import assert from 'node:assert/strict';
import {
  canSubmitReservation,
  editableContactValue,
  formatReservationTime,
  getMaximumBookingDate,
  includeCurrentReservationSlot,
} from '../features/reservation/reservationRules.js';

// 예약 제출 조건을 한 곳에서 검증해 화면 버튼과 실제 요청의 판단이 달라지지 않게 합니다.
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

// 소셜 로그인용 임시 연락처는 고객이 실제 값으로 바꿀 수 있도록 빈 입력값으로 변환합니다.
test('소셜 임시 연락처는 수정 폼에서 빈 값으로 보여 준다', () => {
  assert.equal(editableContactValue('social_a@social.marinboy.local', 'email'), '');
  assert.equal(editableContactValue('SOCIAL_REQUIRED', 'phone'), '');
  assert.equal(editableContactValue('customer@example.com', 'email'), 'customer@example.com');
});

// 오늘 기준 예약 범위가 정책상 허용한 7일을 넘지 않는지 날짜 경계를 검증합니다.
test('예약 마지막 날짜는 오늘을 포함해 7일 뒤이다', () => {
  assert.equal(getMaximumBookingDate('2026-08-21'), '2026-08-28');
});

// 시간 선택 목록에는 고객에게 불필요한 날짜 부분을 숨기는 표시 규칙을 검증합니다.
test('예약 시간 목록에는 날짜를 제외한 시간만 표시한다', () => {
  const formattedTime = formatReservationTime('2026-08-24T10:30:00');
  assert.match(formattedTime, /10:30/);
  assert.doesNotMatch(formattedTime, /2026|8월|24/);
});

// 수정 전 시간은 서버의 중복 조회에서 빠지더라도 고객이 그대로 유지할 수 있어야 합니다.
test('예약 수정 목록에는 현재 선택 시간을 한 번만 유지한다', () => {
  const current = '2026-08-24T10:30:00';
  assert.deepEqual(includeCurrentReservationSlot([], current, '2026-08-24'), [current]);
  assert.deepEqual(includeCurrentReservationSlot([current], current, '2026-08-24'), [current]);
  assert.deepEqual(includeCurrentReservationSlot([], current, '2026-08-25'), []);
});
