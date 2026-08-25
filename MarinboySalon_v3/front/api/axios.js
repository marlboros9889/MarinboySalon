import axios from 'axios';

// 프론트와 백이 분리되어 있으므로 모든 API 주소를 한 곳에서 관리합니다.
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  withCredentials: true,
});

// 로그인 후 받은 Access Token을 선생님 예제처럼 Authorization 헤더에 붙입니다.
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const accessToken = window.localStorage.getItem('accessToken');
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
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
        const response = await api.post('/auth/refresh');
        const accessToken = response.data.accessToken;
        window.localStorage.setItem('accessToken', accessToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        window.localStorage.removeItem('accessToken');
      }
    }
    return Promise.reject(error);
  },
);

export default api;
