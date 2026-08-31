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

/** 취소 또는 완료된 예약에는 취소 버튼을 다시 보여주지 않습니다. */
export function canCancelReservation(status) {
  return status !== 'CANCELLED' && status !== 'COMPLETED';
}
