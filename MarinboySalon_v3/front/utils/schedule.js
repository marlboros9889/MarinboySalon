const dayNames = ['', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일', '일요일'];

/** DB의 1~7 요일 번호를 월요일부터 시작하는 한글 이름으로 표시합니다. */
export function getDayName(dayOfWeek) {
  return dayNames[Number(dayOfWeek)] || '알 수 없는 요일';
}
