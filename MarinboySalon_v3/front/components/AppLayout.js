import Link from 'next/link';
import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { LOAD_ME_REQUEST, LOG_OUT_REQUEST } from '../reducers/authReducer';

/**
 * 모든 페이지에서 같은 헤더와 폭을 사용하도록 공통 레이아웃으로 묶습니다.
 */
export default function AppLayout({ children }) {
  const dispatch = useDispatch();
  const { me } = useSelector((state) => state.auth);

  useEffect(() => {
    const accessToken = window.localStorage.getItem('accessToken');
    if (accessToken && !me) {
      dispatch({ type: LOAD_ME_REQUEST });
    }
  }, [dispatch, me]);

  const onLogOut = () => {
    dispatch({ type: LOG_OUT_REQUEST });
  };

  return (
    <div className="site-shell">
      <header className="site-header">
        <div className="container header-inner">
          <Link href="/" className="brand serif-text">MARINBOY SALON</Link>
          <nav className="main-nav" aria-label="주요 메뉴">
            <Link href="/services">시술 메뉴</Link>
            <Link href="/reservations/new">예약하기</Link>
            {me && <Link href="/reservations">내 예약</Link>}
            {me?.role === 'ADMIN' && <Link href="/admin/reservations">관리자</Link>}
          </nav>
          <div className="auth-nav">
            {me ? (
              <>
                <span className="user-name">{me.name}님</span>
                <button type="button" className="link-button" onClick={onLogOut}>로그아웃</button>
              </>
            ) : (
              <>
                <Link href="/auth/login">로그인</Link>
                <Link href="/auth/signup" className="outline-link">회원가입</Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main>{children}</main>
      <footer className="site-footer">
        <div className="container">1인 헤어샵을 위한 예약 포트폴리오 · UTF-8</div>
      </footer>
    </div>
  );
}
