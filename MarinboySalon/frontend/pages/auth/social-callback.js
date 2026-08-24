import { useEffect, useState } from 'react';
import { saveAccessToken } from '../../features/shared/api/jwtApi';

/** 소셜 제공자에서 돌아온 JWT를 저장하고 고객 화면으로 안전하게 복귀합니다. */
export default function SocialCallback() {
  const [message, setMessage] = useState('소셜 로그인 정보를 확인하고 있습니다.');

  useEffect(() => {
    //1. 제공자 오류와 URL fragment의 JWT를 분리해서 읽어 실패 원인을 먼저 표시합니다.
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

    //2. JWT는 공통 저장소에 보관한 뒤 URL 기록에서 제거하고 로그인 영역으로 복귀합니다.
    saveAccessToken(accessToken);
    // fragment에 남은 토큰이 브라우저 기록에 오래 보이지 않도록 즉시 홈으로 교체 이동합니다.
    window.history.replaceState(null, '', '/auth/social-callback');
    window.location.replace('/#login');
  }, []);

  return <main className="simple-page"><a href="/">← 홈으로</a><h1>소셜 로그인</h1><p role="status">{message}</p></main>;
}
