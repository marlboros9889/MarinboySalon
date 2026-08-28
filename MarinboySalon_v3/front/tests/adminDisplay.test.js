import {
  canCancelReservation,
  getReservationStatusLabel,
  normalizeReservationStatus,
  RESERVATION_STATUS_OPTIONS,
} from '../utils/reservationStatus';
import { getDayName } from '../utils/schedule';

describe('관리자 화면 표시 도구', () => {
  test('예약 상태 선택값은 한글로 표시한다', () => {
    expect(RESERVATION_STATUS_OPTIONS.map((option) => option.label))
      .toEqual(['접수', '확정', '완료', '취소']);
    expect(getReservationStatusLabel('CONFIRMED')).toBe('확정');
  });

  test('과거 취소 철자도 현재 표준으로 바꾸고 취소 버튼을 숨긴다', () => {
    expect(normalizeReservationStatus('CANCELED')).toBe('CANCELLED');
    expect(getReservationStatusLabel('CANCELED')).toBe('취소');
    expect(canCancelReservation('CANCELED')).toBe(false);
  });

  test('요일 번호를 월요일부터 한글로 표시한다', () => {
    expect([1, 2, 3, 4, 5, 6, 7].map(getDayName))
      .toEqual(['월요일', '화요일', '수요일', '목요일', '금요일', '토요일', '일요일']);
  });
});
