import axios from 'axios';
import { clearAccessToken, getAccessToken, setAccessToken } from './accessToken';
import { apiBaseUrl } from './apiConfig';

// 프론트와 백이 분리되어 있으므로 모든 API 주소를 한 곳에서 관리합니다.
const api = axios.create({
  baseURL: apiBaseUrl,
  withCredentials: true,
});

let refreshPromise = null;

// 메모리에 있는 Access Token만 Authorization 헤더에 붙입니다.
api.interceptors.request.use((config) => {
  const accessToken = getAccessToken();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

// Access Token이 만료되면 HttpOnly Refresh Token 쿠키로 한 번만 재발급합니다.
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const isUnauthorized = error.response && error.response.status === 401;
    const isRefreshRequest = originalRequest && originalRequest.url === '/auth/refresh';

    if (isUnauthorized && !isRefreshRequest && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        // 여러 요청이 동시에 만료되어도 Refresh 요청은 한 번만 실행합니다.
        if (!refreshPromise) {
          refreshPromise = api.post('/auth/refresh')
            .then((response) => {
              setAccessToken(response.data.accessToken);
              return response.data.accessToken;
            })
            .finally(() => {
              refreshPromise = null;
            });
        }
        const accessToken = await refreshPromise;
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        clearAccessToken();
      }
    }
    return Promise.reject(error);
  },
);

export default api;
