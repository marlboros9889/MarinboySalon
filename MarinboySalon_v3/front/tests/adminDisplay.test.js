import {
  canCancelReservation,
  getReservationStatusLabel,
  RESERVATION_STATUS_OPTIONS,
} from '../utils/reservationStatus';
import { getDayName } from '../utils/schedule';

// 관리자가 보는 예약 상태와 요일 표기가 업무 용어와 일치하는지 검증합니다.
describe('관리자 화면 표시 도구', () => {
  test('예약 상태 선택값은 한글로 표시한다', () => {
    expect(RESERVATION_STATUS_OPTIONS.map((option) => option.label))
      .toEqual(['접수', '확정', '완료', '취소']);
    expect(getReservationStatusLabel('CONFIRMED')).toBe('확정');
  });

  test('표준 취소 상태는 취소 버튼을 숨긴다', () => {
    expect(getReservationStatusLabel('CANCELLED')).toBe('취소');
    expect(canCancelReservation('CANCELLED')).toBe(false);
  });

  test('요일 번호를 월요일부터 한글로 표시한다', () => {
    expect([1, 2, 3, 4, 5, 6, 7].map(getDayName))
      .toEqual(['월요일', '화요일', '수요일', '목요일', '금요일', '토요일', '일요일']);
  });
});
