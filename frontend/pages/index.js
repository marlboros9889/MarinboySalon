// frontend/pages/index.js: 서버에서 시술 데이터를 준비해 검색엔진과 첫 화면에 완성된 HTML을 제공합니다.
import Head from 'next/head';
import SalonHome from '../features/home/components/SalonHome';

export default function HomePage({ initialServices }) {
  return (
    <>
      <Head>
        <title>marinboySalon | 프라이빗 헤어 아틀리에</title>
        <meta name="description" content="1:1 헤어 상담, 실시간 예약, 소셜 로그인과 Calendar 동기화를 제공하는 marinboySalon입니다." />
      </Head>
      <SalonHome initialServices={initialServices} />
    </>
  );
}

export async function getServerSideProps() {
  //1. 서버가 공개 시술 데이터를 먼저 조회해 완성된 첫 화면을 만듭니다.
  const apiBaseUrl = process.env.SSR_API_BASE_URL
    || process.env.NEXT_PUBLIC_API_BASE_URL
    || 'http://127.0.0.1:8082';

  try {
    const response = await fetch(`${apiBaseUrl}/api/services`);
    if (!response.ok) throw new Error(`시술 API 응답 오류: ${response.status}`);
    const data = await response.json();
    return { props: { initialServices: Array.isArray(data) ? data : [] } };
  } catch {
    // 백엔드가 잠시 중단되어도 홈 자체는 열고, SalonHome이 브라우저에서 한 번 더 조회하게 합니다.
    return { props: { initialServices: [] } };
  }
}
