// V3 포트폴리오 실행 기준의 백엔드 주소를 한 곳에서만 관리합니다.
export const apiBaseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8082';

// SSR 서버는 내부 API 주소를 별도로 지정할 수 있지만, 기본값은 고객 화면과 같은 8082입니다.
export const internalApiBaseUrl = process.env.INTERNAL_API_URL || apiBaseUrl;
