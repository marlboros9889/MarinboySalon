import { useEffect } from 'react';
import { useRouter } from 'next/router';
import AppLayout from '../../components/AppLayout';
import api from '../../api/axios';

/**
 * 소셜 로그인 성공 후 HttpOnly Refresh 쿠키로 사용자 정보를 확인합니다.
 */
export default function OAuth2Callback() {
  const router = useRouter();

  useEffect(() => {
    if (!router.isReady) {
      return;
    }
    // URL에 토큰을 노출하지 않고 /auth/me 호출의 401 재발급 흐름만 사용합니다.
    api.get('/auth/me')
      .then((response) => {
        const destination = response.data.role === 'ADMIN' ? '/admin/reservations' : '/reservations';
        router.replace(destination);
      })
      .catch(() => {
        router.replace('/auth/login');
      });
  }, [router]);

  return (
    <AppLayout>
      <section className="auth-section container">
        <p className="status-message">소셜 로그인 정보를 확인하고 있습니다.</p>
      </section>
    </AppLayout>
  );
}
