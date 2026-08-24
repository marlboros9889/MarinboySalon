/** 소셜 로그인에서 임시로 저장한 연락처는 수정 폼에 노출하지 않습니다. */
export function editableContactValue(value, type) {
  if (!value) return '';
  if (type === 'email' && value.endsWith('@social.marinboy.local')) return '';
  if (type === 'phone' && value.startsWith('SOCIAL_REQUIRED')) return '';
  return value;
}
