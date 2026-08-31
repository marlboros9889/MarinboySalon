// Access Token은 브라우저 저장소가 아니라 현재 탭의 메모리에만 보관합니다.
let accessToken = null;

export function getAccessToken() {
  return accessToken;
}

export function setAccessToken(token) {
  accessToken = token || null;
}

export function clearAccessToken() {
  accessToken = null;
}
