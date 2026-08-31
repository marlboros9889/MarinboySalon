import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useRouter } from 'next/router';
import Link from 'next/link';
import AppLayout from '../../components/AppLayout';
import { LOG_IN_REQUEST } from '../../reducers/authReducer';
import { apiBaseUrl } from '../../api/apiConfig';

// 일반·소셜 로그인 진입점과 로그인 후 이동할 화면을 관리합니다.
export default function Login() {
  const dispatch = useDispatch();
  const router = useRouter();
  const { me, logInLoading, logInError } = useSelector((state) => state.auth);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const oauthError = router.query.oauthError === 'failed';

  useEffect(() => {
    // 관리자와 고객은 사용하는 업무 화면이 달라 역할별 기본 화면으로 이동합니다.
    if (me) {
      const destination = me.role === 'ADMIN' ? '/admin/reservations' : (router.query.returnTo || '/reservations');
      router.replace(destination);
    }
  }, [me, router]);

  const onSubmit = (event) => {
    event.preventDefault();
    dispatch({ type: LOG_IN_REQUEST, data: { email, password } });
  };

  return (
    <AppLayout>
      <section className="auth-section container">
        <form className="paper-form torn-paper-edge" onSubmit={onSubmit}>
          <p className="eyebrow">WELCOME BACK</p>
          <h1 className="heading-text">로그인</h1>
          <label htmlFor="email">이메일</label>
          <input id="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          <label htmlFor="password">비밀번호</label>
          <input id="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
          {logInError && <p className="error-message">{logInError}</p>}
          {oauthError && <p className="error-message">소셜 로그인에 실패했습니다. 제공자 계정 권한과 콜백 주소를 확인해 주세요.</p>}
          <button type="submit" className="primary-button" disabled={logInLoading}>
            {logInLoading ? '확인 중...' : '로그인'}
          </button>
          <div className="social-login-list" aria-label="소셜 로그인">
            <a href={`${apiBaseUrl}/oauth2/authorization/google`}>Google</a>
            <a href={`${apiBaseUrl}/oauth2/authorization/kakao`}>Kakao</a>
            <a href={`${apiBaseUrl}/oauth2/authorization/naver`}>Naver</a>
          </div>
          <p className="form-guide">처음 방문하셨나요? <Link href="/auth/signup">회원가입</Link></p>
        </form>
      </section>
    </AppLayout>
  );
}
