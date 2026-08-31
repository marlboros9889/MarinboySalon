import Link from 'next/link';
import { useRouter } from 'next/router';
import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { FiInstagram, FiMapPin, FiMessageCircle, FiPhone } from 'react-icons/fi';
import { LOAD_ME_REQUEST, LOG_OUT_REQUEST } from '../reducers/authReducer';
import { getAdminRedirectTarget } from '../utils/adminAccess';

/**
 * 모든 페이지에서 같은 헤더와 폭을 사용하도록 공통 레이아웃으로 묶습니다.
 */
export default function AppLayout({ children }) {
  const dispatch = useDispatch();
  const router = useRouter();
  const { me, loadMeLoading, loadMeDone } = useSelector((state) => state.auth);
  const isAdminPage = router.pathname.startsWith('/admin');

  useEffect(() => {
    // 새로고침하면 메모리 토큰이 비므로 HttpOnly Refresh 쿠키로 사용자 정보를 복구합니다.
    if (!me && !loadMeLoading && !loadMeDone) {
      dispatch({ type: LOAD_ME_REQUEST });
    }
  }, [dispatch, me, loadMeDone, loadMeLoading]);

  useEffect(() => {
    if (!isAdminPage || !loadMeDone) {
      return;
    }

    const redirectTarget = getAdminRedirectTarget(me, router.asPath);
    if (redirectTarget) {
      router.replace(redirectTarget);
    }
  }, [isAdminPage, loadMeDone, me, router]);

  const onLogOut = () => {
    dispatch({ type: LOG_OUT_REQUEST });
  };

  if (isAdminPage && (!loadMeDone || me?.role !== 'ADMIN')) {
    return <main className="page-section container">관리자 권한을 확인하는 중입니다.</main>;
  }

  return (
    <div className="site-shell">
      <header className="site-header">
        <div className="container header-inner">
          <Link href="/" className="brand display-text"><span>Marinboy</span><small>HAIR SALON</small></Link>
          <nav className="main-nav" aria-label="주요 메뉴">
            <Link href="/#about">ABOUT</Link>
            <Link href="/#menu">MENU</Link>
            <Link href="/#style">STYLE</Link>
            <Link href="/#review">REVIEW</Link>
            <Link href="/reservations/new">RESERVATION</Link>
            <Link href="/#contact">LOCATION</Link>
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
                <Link href="/reservations/new" className="header-booking-link">예약하기</Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main>{children}</main>
      <footer className="site-footer" id="contact">
        <div className="container site-footer-inner">
          <Link href="/" className="footer-brand display-text"><span>Marinboy</span><small>HAIR SALON</small></Link>
          <div className="footer-contact-grid">
            <div><FiPhone /><span><strong>전화 예약</strong><small>010-1234-5678</small></span></div>
            <div><FiMessageCircle /><span><strong>Kakao 상담</strong><small>@marinboy_hair</small></span></div>
            <div><FiInstagram /><span><strong>Instagram</strong><small>@marinboy_hair</small></span></div>
            <div><FiMapPin /><span><strong>오시는 길</strong><small>서울시 강남구 도산대로 123</small></span></div>
          </div>
        </div>
      </footer>
      <nav className="mobile-booking-nav" aria-label="빠른 예약 메뉴">
        <a href="tel:01012345678"><FiPhone /> 전화하기</a>
        <a href="#contact"><FiMessageCircle /> Kakao 상담</a>
        <Link href="/reservations/new">예약하기</Link>
      </nav>
    </div>
  );
}
