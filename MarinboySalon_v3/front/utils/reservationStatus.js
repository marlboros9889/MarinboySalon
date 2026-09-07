import { formatDateInputValue } from './reservation';

export const RESERVATION_STATUS_OPTIONS = [
  { value: 'REQUESTED', label: '접수' },
  { value: 'CONFIRMED', label: '확정' },
  { value: 'COMPLETED', label: '완료' },
  { value: 'CANCELLED', label: '취소' },
];

const statusLabels = Object.fromEntries(
  RESERVATION_STATUS_OPTIONS.map((option) => [option.value, option.label]),
);

/** API의 영문 상태를 고객과 관리자가 읽기 쉬운 한글로 표시합니다. */
export function getReservationStatusLabel(status) {
  return statusLabels[status] || status || '상태 없음';
}

/**
 * 고객은 예약일 전날까지만 취소할 수 있습니다.
 * 화면 조건과 서버 정책을 맞춰 당일 예약 취소 버튼을 보여주지 않습니다.
 */
export function canCancelReservation(status, reservationStart) {
  if (status === 'CANCELLED' || status === 'COMPLETED') {
    return false;
  }

  if (!reservationStart) {
    return false;
  }

  const reservationDate = reservationStart.slice(0, 10);
  return reservationDate > formatDateInputValue(new Date());
}
