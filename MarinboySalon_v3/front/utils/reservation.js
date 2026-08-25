/** HTML date 입력에 필요한 로컬 날짜 형식(YYYY-MM-DD)을 만듭니다. */
export function formatDateInputValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/** 서버가 돌려준 24시간 표기를 고객이 읽기 쉬운 오전/오후 표기로 바꿉니다. */
export function formatTimeLabel(time) {
  const [hourText, minute] = time.split(':');
  const hour = Number(hourText);
  const period = hour < 12 ? '오전' : '오후';
  const displayHour = hour % 12 || 12;
  return `${period} ${displayHour}:${minute}`;
}
