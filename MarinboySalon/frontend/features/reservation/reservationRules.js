const MAX_BOOKING_DAYS = 7;

/** 예약 화면과 테스트가 같은 기준으로 버튼 활성화 조건을 판단합니다. */
export function canSubmitReservation({
  user,
  serviceId,
  date,
  reservationDateTime,
  noShowPolicyAgreed,
  submitting,
}) {
  return Boolean(
    user
    && user.profileComplete !== false
    && serviceId
    && date
    && reservationDateTime
    && noShowPolicyAgreed
    && !submitting,
  );
}

/** 백엔드의 예약 허용 범위와 같은 마지막 선택 날짜를 반환합니다. */
export function getMaximumBookingDate(today) {
  const maximumDate = new Date(`${today}T00:00:00`);
  maximumDate.setDate(maximumDate.getDate() + MAX_BOOKING_DAYS);
  return maximumDate.toLocaleDateString('en-CA');
}

/** 날짜는 별도 입력란에 표시하므로 시간 선택 목록에는 시각만 보여 줍니다. */
export function formatReservationTime(reservationDateTime) {
  return new Date(reservationDateTime).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

/** 수정 중인 기존 시간은 자기 예약과 겹쳐 조회 목록에서 빠져도 선택지에 유지합니다. */
export function includeCurrentReservationSlot(slots, currentSlot, selectedDate) {
  const result = Array.isArray(slots) ? [...slots] : [];
  if (currentSlot?.startsWith(selectedDate) && !result.includes(currentSlot)) {
    result.unshift(currentSlot);
  }
  return result;
}
