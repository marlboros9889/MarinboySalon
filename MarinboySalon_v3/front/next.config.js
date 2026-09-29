/** @type {import('next').NextConfig} */
// 개발 중 의도하지 않은 부수 효과를 빨리 찾도록 React 엄격 모드를 사용합니다.
const nextConfig = {
  reactStrictMode: true,
  // public/v4의 Flutter 시작 파일을 V3 라우터보다 먼저 연결합니다.
  async redirects() {
    return [
      {
        source: '/v4',
        destination: '/v4/index.html',
        permanent: false,
      },
    ];
  },
  // 이전 개발 화면이 뒤로가기로 남지 않도록 인증 관련 페이지는 브라우저 캐시를 사용하지 않습니다.
  async headers() {
    return [
      {
        source: '/auth/:path*',
        headers: [{ key: 'Cache-Control', value: 'no-store, max-age=0' }],
      },
      {
        source: '/oauth2/:path*',
        headers: [{ key: 'Cache-Control', value: 'no-store, max-age=0' }],
      },
    ];
  },
};

module.exports = nextConfig;
