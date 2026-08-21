import { useEffect, useState } from 'react';
import { saveAccessToken } from '../../features/shared/api/jwtApi';

/** 소셜 제공자에서 돌아온 JWT를 저장하고 고객 화면으로 안전하게 복귀합니다. */
export default function SocialCallback() {
  const [message, setMessage] = useState('소셜 로그인 정보를 확인하고 있습니다.');

  useEffect(() => {
    const queryError = new URLSearchParams(window.location.search).get('error');
    const hash = new URLSearchParams(window.location.hash.replace(/^#/, ''));
    const accessToken = hash.get('access_token');

    if (queryError) {
      setMessage(queryError);
      return;
    }
    if (!accessToken) {
      setMessage('소셜 로그인 토큰을 받지 못했습니다. 다시 시도해 주세요.');
      return;
    }

    saveAccessToken(accessToken);
    // fragment에 남은 토큰이 브라우저 기록에 오래 보이지 않도록 즉시 홈으로 교체 이동합니다.
    window.history.replaceState(null, '', '/auth/social-callback');
    window.location.replace('/#login');
  }, []);

  return <main className="simple-page"><a href="/">← 홈으로</a><h1>소셜 로그인</h1><p role="status">{message}</p></main>;
}
