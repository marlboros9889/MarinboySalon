import { useEffect } from 'react';
import { useRouter } from 'next/router';
import AppLayout from '../../components/AppLayout';

/**
 * 소셜 로그인 성공 후 Access Token을 저장하고 내 예약 화면으로 이동합니다.
 */
export default function OAuth2Callback() {
  const router = useRouter();

  useEffect(() => {
    if (!router.isReady) {
      return;
    }
    const accessToken = router.query.accessToken;
    if (accessToken) {
      window.localStorage.setItem('accessToken', accessToken);
      router.replace('/reservations');
    } else {
      router.replace('/auth/login');
    }
  }, [router]);

  return (
    <AppLayout>
      <section className="auth-section container">
        <p className="status-message">소셜 로그인 정보를 확인하고 있습니다.</p>
      </section>
    </AppLayout>
  );
}
