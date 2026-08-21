// frontend/next.config.js: 브라우저 요청을 Spring Boot로 전달해 SSR과 API를 한 주소에서 사용합니다.
const springBootUrl = process.env.SSR_API_BASE_URL || 'http://127.0.0.1:8082';

/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      { source: '/api/:path*', destination: `${springBootUrl}/api/:path*` },
      { source: '/images/:path*', destination: `${springBootUrl}/images/:path*` },
      // 관리자 메뉴에서 저장한 대표·상세 이미지를 Spring의 업로드 폴더에서 제공합니다.
      { source: '/uploads/:path*', destination: `${springBootUrl}/uploads/:path*` },
      { source: '/login', destination: `${springBootUrl}/login` },
    ];
  },
};

export default nextConfig;
