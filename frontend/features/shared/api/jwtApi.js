const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '';
const ACCESS_TOKEN_KEY = 'marinboyAccessToken';
const PUBLIC_AUTH_PATHS = new Set([
  '/api/auth/login',
  '/api/auth/signup',
  '/api/auth/check-username',
  '/api/auth/check-email',
  '/api/auth/social/providers',
]);

function isPublicAuthPath(path) {
  const pathWithoutQuery = path.split('?')[0];
  return PUBLIC_AUTH_PATHS.has(pathWithoutQuery);
}

/** 모든 보호 요청에 저장된 JWT를 Authorization 헤더로 전달합니다. */
export async function jwtFetch(path, options = {}) {
  const headers = new Headers(options.headers || {});
  const accessToken = getAccessToken();
  // 로그인·회원가입 같은 공개 API는 만료 토큰 때문에 첫 요청이 401로 막히지 않게 JWT를 보내지 않습니다.
  if (accessToken && !isPublicAuthPath(path)) headers.set('Authorization', `Bearer ${accessToken}`);
  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
  // 만료되거나 폐기된 토큰은 즉시 지워 다음 화면에서도 로그인 상태로 오인하지 않게 합니다.
  if (accessToken && response.status === 401) clearAccessToken();
  return response;
}

/** JSON 요청에서 공통으로 사용하는 HTTP 메서드·헤더·본문을 만듭니다. */
export function jsonRequest(method, body) {
  return {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  };
}

/** 모든 화면이 같은 형식으로 서버의 JSON 결과와 오류 메시지를 읽게 합니다. */
export async function requestJson(path, options = {}, fallbackMessage = '요청을 처리하지 못했습니다.') {
  const response = await jwtFetch(path, options);
  const result = response.status === 204 ? null : await response.json().catch(() => null);

  if (!response.ok) {
    const error = new Error(result?.message || fallbackMessage);
    error.status = response.status;
    error.data = result;
    throw error;
  }
  return result;
}

export function getAccessToken() {
  return typeof window === 'undefined' ? null : window.localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function saveAccessToken(accessToken) {
  if (typeof window !== 'undefined' && accessToken) window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
}

export function clearAccessToken() {
  if (typeof window !== 'undefined') window.localStorage.removeItem(ACCESS_TOKEN_KEY);
}

/** 로그인 사용자 정보는 유효한 Bearer 토큰으로 조회된 경우에만 반환합니다. */
export async function getCurrentUser() {
  if (!getAccessToken()) return null;
  const response = await jwtFetch('/api/auth/me');
  if (response.status === 401) clearAccessToken();
  return response.status === 200 ? response.json() : null;
}
