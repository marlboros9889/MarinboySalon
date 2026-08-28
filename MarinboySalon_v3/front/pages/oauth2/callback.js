import { useEffect } from 'react';
import { useRouter } from 'next/router';
import AppLayout from '../../components/AppLayout';
import api from '../../api/axios';

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
    if (typeof accessToken === 'string') {
      window.localStorage.setItem('accessToken', accessToken);
      // 토큰으로 사용자 권한을 읽어 관리자와 사용자의 시작 화면을 분리합니다.
      api.get('/auth/me')
        .then((response) => {
          const destination = response.data.role === 'ADMIN' ? '/admin/reservations' : '/reservations';
          router.replace(destination);
        })
        .catch(() => {
          window.localStorage.removeItem('accessToken');
          router.replace('/auth/login');
        });
      return;
    }
    router.replace('/auth/login');
  }, [router]);

  return (
    <AppLayout>
      <section className="auth-section container">
        <p className="status-message">소셜 로그인 정보를 확인하고 있습니다.</p>
      </section>
    </AppLayout>
  );
}
