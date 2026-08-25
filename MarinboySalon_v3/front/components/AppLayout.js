import Link from 'next/link';
import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { FiInstagram, FiMapPin, FiMessageCircle, FiPhone } from 'react-icons/fi';
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
