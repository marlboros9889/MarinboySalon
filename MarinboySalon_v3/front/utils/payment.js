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
