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

/** v3의 모든 보호 요청에 저장된 JWT를 Authorization 헤더로 전달합니다. */
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

/** EventSource가 헤더를 보낼 수 없어 fetch 스트림으로 관리자 SSE를 구독합니다. */
export function subscribeToNotifications(onMessage) {
  const controller = new AbortController();
  jwtFetch('/api/admin/notifications/subscribe', {
    headers: { Accept: 'text/event-stream' },
    signal: controller.signal,
  }).then(async (response) => {
    if (!response.ok || !response.body) throw new Error('알림 연결에 실패했습니다.');
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    while (!controller.signal.aborted) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      buffer = buffer.replace(/\r\n/g, '\n');
      const events = buffer.split('\n\n');
      buffer = events.pop() || '';
      events.forEach((event) => {
        const data = event.split('\n').find((line) => line.startsWith('data:'));
        if (data) onMessage(JSON.parse(data.slice(5).trim()));
      });
    }
  }).catch((error) => {
    if (error.name !== 'AbortError') console.log('SSE 연결 오류:', error.message);
  });
  return () => controller.abort();
}
