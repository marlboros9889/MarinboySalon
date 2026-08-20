// frontend/pages/index.js: 서버에서 시술 데이터를 준비해 검색엔진과 첫 화면에 완성된 HTML을 제공합니다.
import Head from 'next/head';
import SalonHome from '../features/home/components/SalonHome';
import { loadServicesFailure, loadServicesSuccess } from '../reducers/service';
import { wrapper } from '../store/configureStore';

export default function HomePage() {
  return (
    <>
      <Head>
        <title>마린보이 살롱 | 1:1 예약</title>
        <meta name="description" content="시술 메뉴를 확인하고 원하는 시간에 예약하는 마린보이 살롱입니다." />
      </Head>
      <SalonHome />
    </>
  );
}

export const getServerSideProps = wrapper.getServerSideProps((store) => async () => {
  //1. 서버가 공개 시술 데이터를 먼저 조회해 SSR HTML과 Redux 초기 상태를 함께 만듭니다.
  const apiBaseUrl = process.env.SSR_API_BASE_URL
    || process.env.NEXT_PUBLIC_API_BASE_URL
    || 'http://127.0.0.1:8082';

  try {
    const response = await fetch(`${apiBaseUrl}/api/services`);
    if (!response.ok) throw new Error(`시술 API 응답 오류: ${response.status}`);
    const data = await response.json();
    store.dispatch(loadServicesSuccess(Array.isArray(data) ? data : []));
  } catch {
    store.dispatch(loadServicesFailure('SSR 시술 조회에 실패해 브라우저에서 다시 요청합니다.'));
  }

  return { props: {} };
});
