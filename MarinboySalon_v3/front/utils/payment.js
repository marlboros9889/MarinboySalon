/** 서버 할인 계산과 같은 내림 규칙으로 예약 전 예상 결제금액을 계산합니다. */
export function calculatePayment(servicePrice, discountEvent) {
  const originalPrice = Number(servicePrice || 0);
  const discountRate = Number(discountEvent?.discountRate || 0);
  const discountAmount = Math.floor((originalPrice * discountRate) / 100);

  return {
    originalPrice,
    discountRate,
    discountAmount,
    finalPrice: originalPrice - discountAmount,
    eventName: discountEvent?.name || '',
  };
}

/** 예약에 저장된 금액을 우선 사용해 관리자 화면도 실제 예약 금액과 같게 표시합니다. */
export function getReservationPayment(reservation) {
  const originalPrice = Number(reservation?.originalPrice ?? reservation?.servicePrice ?? 0);
  const discountRate = Number(reservation?.discountRate ?? 0);
  const discountAmount = Number(reservation?.discountAmount ?? 0);
  const finalPrice = Number(reservation?.finalPrice ?? (originalPrice - discountAmount));

  return { originalPrice, discountRate, discountAmount, finalPrice };
}
