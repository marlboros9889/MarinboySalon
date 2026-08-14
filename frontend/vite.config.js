import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 개발 중에도 API·OAuth 요청을 같은 출처처럼 전달해 포트별 CORS 오류를 줄입니다.
    proxy: {
      '/api': 'http://127.0.0.1:8082',
      '/oauth2': 'http://127.0.0.1:8082',
      '/login': 'http://127.0.0.1:8082',
      '/reservation': 'http://127.0.0.1:8082',
    },
  },
})
