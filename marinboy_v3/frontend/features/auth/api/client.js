import axios from 'axios'

// 개발 서버와 Spring Boot API 주소를 환경 변수로 분리합니다.
// React와 같은 127.0.0.1 호스트를 사용해 OAuth2/JWT 세션 쿠키를 공유합니다.
const api = axios.create({ baseURL: `${process.env.NEXT_PUBLIC_API_BASE_URL ?? ''}/api/v3`, withCredentials: true })

api.interceptors.request.use((config) => {
  // 로그인 성공 후 저장한 토큰만 Authorization 헤더에 추가합니다.
  const accessToken = localStorage.getItem('marinboyAccessToken')
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

export default api
