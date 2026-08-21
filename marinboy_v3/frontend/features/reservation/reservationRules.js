const MAX_BOOKING_DAYS = 7;

/** 예약 화면과 테스트가 같은 기준으로 버튼 활성화 조건을 판단합니다. */
export function canSubmitReservation({ user, serviceId, date, reservationDateTime, submitting }) {
  return Boolean(
    user
    && user.profileComplete !== false
    && serviceId
    && date
    && reservationDateTime
    && !submitting,
  );
}

/** 소셜 로그인에서 임시로 저장한 연락처는 수정 폼에 노출하지 않습니다. */
export function editableContactValue(value, type) {
  if (!value) return '';
  if (type === 'email' && value.endsWith('@social.marinboy.local')) return '';
  if (type === 'phone' && value.startsWith('SOCIAL_REQUIRED')) return '';
  return value;
}

/** 백엔드의 예약 허용 범위와 같은 마지막 선택 날짜를 반환합니다. */
export function getMaximumBookingDate(today) {
  const maximumDate = new Date(`${today}T00:00:00`);
  maximumDate.setDate(maximumDate.getDate() + MAX_BOOKING_DAYS);
  return maximumDate.toLocaleDateString('en-CA');
}
