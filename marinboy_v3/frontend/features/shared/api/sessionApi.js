const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '';
const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS', 'TRACE'];

/** 세션 기반 요청에 credentials와 상태 변경용 CSRF 토큰을 일관되게 적용합니다. */
export async function sessionFetch(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = new Headers(options.headers || {});

  if (!SAFE_METHODS.includes(method)) {
    const tokenResponse = await fetch(`${API_BASE_URL}/api/csrf`, { credentials: 'include' });
    if (!tokenResponse.ok) throw new Error('CSRF 토큰을 불러오지 못했습니다.');
    headers.set('X-XSRF-TOKEN', (await tokenResponse.json()).token);
  }

  return fetch(`${API_BASE_URL}${path}`, { ...options, headers, credentials: 'include' });
}

/** 로그인 사용자 정보는 성공 응답(200)일 때만 반환합니다. */
export async function getCurrentUser() {
  const response = await sessionFetch('/api/auth/me');
  return response.status === 200 ? response.json() : null;
}
