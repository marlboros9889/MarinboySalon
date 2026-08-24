import { useEffect, useMemo, useState } from 'react';
import { authApi } from '../../auth/authApi';
import { serviceApi } from '../../service/serviceApi';
import { groupServices, monthlyTopFive, popularityBadges } from '../homeRules';
import {
  DuplicateField,
  RotatingServiceImage,
  ServiceGallery,
  SignupPolicyModal,
  TopList,
} from './HomeComponents';

// public 폴더의 배너는 실행 폴더와 무관한 브라우저 절대경로로 사용합니다.
const SALON_LUXURY_BANNER = '/images/salon-luxury-banner.png';

function SalonHome({ initialServices = [] }) {
  //1. SSR 결과를 바로 표시하고, 실패했을 때만 브라우저에서 한 번 다시 조회합니다.
  const [services, setServices] = useState(initialServices);
  const [activeCategory, setActiveCategory] = useState('ALL');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [signupMode, setSignupMode] = useState(false);
  const [signup, setSignup] = useState({ username: '', password: '', name: '', email: '', phone: '' });
  const [duplicateChecks, setDuplicateChecks] = useState({ username: 'idle', email: 'idle' });
  const [showSignupPolicy, setShowSignupPolicy] = useState(false);
  const [signupPolicyAgreed, setSignupPolicyAgreed] = useState(false);
  const [user, setUser] = useState(null);
  const [socialProviders, setSocialProviders] = useState({ kakao: false, naver: false, google: false });
  const [message, setMessage] = useState('');
  const [returnTo, setReturnTo] = useState('');
  const [selectedService, setSelectedService] = useState(null);

  useEffect(() => {
    if (!initialServices.length) {
      serviceApi.list()
        .then((items) => setServices(Array.isArray(items) ? items : []))
        .catch(() => setMessage('시술 메뉴를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'));
    }

    // 저장된 JWT로 현재 사용자를 조회해 고객 화면에 로그인 상태를 연결합니다.
    authApi.currentUser()
      .then((data) => data && setUser(data))
      .catch(() => null);

    // 서버 설정 상태를 확인해 실제 API 키가 준비된 소셜 버튼만 활성화합니다.
    authApi.socialProviders()
      .then(setSocialProviders)
      .catch(() => setSocialProviders({ kakao: false, naver: false, google: false }));

    // 예약 화면에서 로그인하러 돌아온 경우에만 로그인 성공 뒤 원래 예약 주소로 복귀합니다.
    const requestedPath = new URLSearchParams(window.location.search).get('returnTo');
    if (requestedPath?.startsWith('/reservation') && !requestedPath.startsWith('//')) {
      setReturnTo(requestedPath);
    }
  }, [initialServices.length]);

  const categories = useMemo(() => ([
    { id: 'ALL', label: '전체 시술' },
    { id: 'CUT', label: '컷' },
    { id: 'PERM', label: '펌' },
    { id: 'COLOR', label: '컬러' },
    { id: 'CARE', label: '클리닉' },
  ]), []);
  const visibleServices = activeCategory === 'ALL'
    ? services : groupServices(services, activeCategory);
  const styleTopFive = monthlyTopFive(services, 'STYLE');
  const careTopFive = monthlyTopFive(services, 'CARE');
  const serviceBadges = useMemo(() => popularityBadges(services), [services]);
  const checkDuplicate = async (field) => {
    const value = signup[field].trim();
    if (!value) {
      setDuplicateChecks((current) => ({ ...current, [field]: 'empty' }));
      return;
    }
    try {
      const result = await authApi.checkDuplicate(field, value);
      setDuplicateChecks((current) => ({ ...current, [field]: result.available ? 'available' : 'duplicate' }));
    } catch {
      setDuplicateChecks((current) => ({ ...current, [field]: 'error' }));
    }
  };

  const changeSignupField = (field, value) => {
    setSignup((current) => ({ ...current, [field]: value }));
    if (field === 'username' || field === 'email') {
      setDuplicateChecks((current) => ({ ...current, [field]: 'idle' }));
    }
  };

  const signIn = async (event) => {
    event.preventDefault();
    setMessage('');
    try {
      // AuthApi가 로그인과 JWT 저장을 함께 처리하고 화면에는 사용자 정보만 돌려줍니다.
      const loginUser = await authApi.login(username, password);
      setUser(loginUser);
      setPassword('');
      if (returnTo) {
        window.location.href = returnTo;
        return;
      }
      setMessage(`${loginUser?.name || '고객'}님, 반갑습니다.`);
    } catch {
      setMessage('아이디 또는 비밀번호를 확인해 주세요.');
    }
  };

  const signUp = async (event) => {
    event.preventDefault();
    setMessage('');
    if (duplicateChecks.username !== 'available' || duplicateChecks.email !== 'available') {
      setMessage('아이디와 이메일 중복 확인을 모두 완료해 주세요.');
      return;
    }
    try {
      await authApi.signup(signup);
      setUsername(signup.username);
      setPassword('');
      setSignupMode(false);
      setSignupPolicyAgreed(false);
      setShowSignupPolicy(true);
    } catch {
      setMessage('회원가입에 실패했습니다. 아이디·이메일 중복과 입력 정보를 확인해 주세요.');
    }
  };

  const signOut = async () => {
    await authApi.logout().catch(() => null);
    setUser(null);
    setMessage('로그아웃되었습니다.');
  };

  const moveToReservation = (serviceId) => {
    const hasServiceId = Number.isInteger(Number(serviceId)) && Number(serviceId) > 0;
    const reservationPath = hasServiceId
      ? `/reservation?serviceId=${encodeURIComponent(serviceId)}`
      : '/reservation';

    // 비로그인 고객은 막힌 예약 화면 대신 로그인으로 안내하고, 성공 뒤 선택 메뉴로 복귀합니다.
    if (!user) {
      window.location.href = `/?returnTo=${encodeURIComponent(reservationPath)}#login`;
      return;
    }
    window.location.href = reservationPath;
  };
  const moveToMyReservations = () => {
    // 로그인 고객의 예약 신청 화면이 아닌 예약 현황 화면으로 이동합니다.
    window.location.href = '/my-reservations';
  };
  const startSocialLogin = (provider) => {
    if (!socialProviders[provider]) {
      const providerName = { kakao: '카카오', naver: '네이버', google: 'Google' }[provider] || provider;
      setMessage(`${providerName} 로그인 API 키 설정이 필요합니다.`);
      return;
    }
    window.location.href = `/oauth2/authorization/${provider}`;
  };
  const openGallery = (service) => setSelectedService(service);

  return (
    <main className="salon-app">
      <header className="salon-header container">
        <a className="salon-brand" href="#top" aria-label="Marinboy Salon 홈">MARINBOY<span> SALON</span></a>
        <nav className="salon-nav" aria-label="주요 메뉴">
          <a href="#monthly">이달의 추천</a><a href="#services">시술 메뉴</a><a href="#designer">디자이너</a><a href="#visit">매장 안내</a>
        </nav>
        <div className="salon-header-action">
          {user ? <button className="salon-text-button" onClick={signOut}>로그아웃</button> : <a className="salon-text-button" href="#login">로그인</a>}
          <button className="salon-primary-button salon-small-button" onClick={moveToReservation}>예약하기</button>
        </div>
      </header>

      <section id="top" className="salon-hero container">
        <div className="salon-hero-visual">
          <img className="salon-hero-rotating-image" src={SALON_LUXURY_BANNER} alt="마린보이살롱 프라이빗 살롱 내부" />
          <div className="salon-hero-caption"><strong>08</strong><span>PRIVATE HAIR<br />ARCHIVE</span></div>
        </div>
        <div className="salon-hero-copy">
          <p className="salon-eyebrow">MARINBOY PRIVATE HAIR ATELIER · 2026</p>
          <h1>당신의 결을 읽는<br /><em><span className="salon-one-to-one">1:1</span> Private Hair Design</em></h1>
          <p className="salon-lead">얼굴형·모발·일상의 리듬까지 읽는 섬세한 상담,<br />오직 한 고객에게 집중하는 프라이빗 헤어 경험입니다.</p>
          <div className="salon-hero-buttons">
            <button className="salon-primary-button" onClick={moveToReservation}>시술 예약하기</button>
            <a className="salon-secondary-button" href="#services">메뉴 둘러보기</a>
          </div>
        </div>
      </section>

      <section className="salon-proof-strip" aria-label="살롱 서비스 특징">
        <div className="container">
          <article><strong>1:1</strong><span>PRIVATE CONSULTING</span></article>
          <article><strong>SMART</strong><span>LIVE BOOKING SLOTS</span></article>
          <article><strong>3-WAY</strong><span>SOCIAL LOGIN</span></article>
          <article><strong>SYNC</strong><span>GOOGLE CALENDAR</span></article>
        </div>
      </section>

      <section id="login" className="salon-login-section">
        <div className="container salon-login-grid">
          <div><p className="salon-eyebrow">MEMBERSHIP</p><h2>더 편리한 예약,<br />회원으로 시작하세요.</h2></div>
          {user ? <div className="salon-welcome"><strong>{user.name || '고객'}님</strong><span>예약 내역과 맞춤 서비스를 확인할 수 있어요.</span>{user.role === 'ADMIN' ? <div className="salon-welcome-actions"><a className="salon-secondary-button" href="/admin#reservation-status">예약 현황보기</a><a className="salon-primary-button" href="/admin#service-management">시술 메뉴 수정</a></div> : <div className="salon-welcome-actions"><button className="salon-secondary-button" onClick={moveToMyReservations}>나의 예약 보기</button><button className="salon-inline-button" type="button" onClick={() => setMessage('내 정보 수정은 예약 내역 화면에서 이용할 수 있습니다.')}>내 정보 수정</button></div>}</div>
            : signupMode ? <form className="salon-login-form" onSubmit={signUp}><div className="salon-signup-grid"><DuplicateField label="아이디" value={signup.username} status={duplicateChecks.username} onChange={(value) => changeSignupField('username', value)} onCheck={() => checkDuplicate('username')} /><input value={signup.password} onChange={(e) => changeSignupField('password', e.target.value)} type="password" placeholder="비밀번호" autoComplete="new-password" required /><input value={signup.name} onChange={(e) => changeSignupField('name', e.target.value)} placeholder="이름" autoComplete="name" required /><DuplicateField label="이메일" type="email" value={signup.email} status={duplicateChecks.email} onChange={(value) => changeSignupField('email', value)} onCheck={() => checkDuplicate('email')} /><input value={signup.phone} onChange={(e) => changeSignupField('phone', e.target.value)} placeholder="연락처" autoComplete="tel" required /><button className="salon-primary-button" type="submit">가입하기</button></div><button className="salon-inline-button" type="button" onClick={() => setSignupMode(false)}>로그인으로 돌아가기</button></form>
              : <div className="salon-login-form">
                <form onSubmit={signIn}>
                  <div className="salon-input-row"><input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="아이디" autoComplete="username" required /><input value={password} onChange={(e) => setPassword(e.target.value)} type="password" placeholder="비밀번호" autoComplete="current-password" required /><button className="salon-primary-button" type="submit">로그인</button></div>
                  <button className="salon-inline-button" type="button" onClick={() => setSignupMode(true)}>처음이신가요? 회원가입</button>
                </form>
                <div className="salon-social-row" aria-label="소셜 로그인">
                  <button className="salon-social-button kakao" type="button" onClick={() => startSocialLogin('kakao')} aria-disabled={!socialProviders.kakao}>카카오 로그인</button>
                  <button className="salon-social-button naver" type="button" onClick={() => startSocialLogin('naver')} aria-disabled={!socialProviders.naver}>네이버 로그인</button>
                  <button className="salon-social-button google" type="button" onClick={() => startSocialLogin('google')} aria-disabled={!socialProviders.google}>Google 로그인</button>
                </div>
              </div>}
        </div>
        {message && <p className="salon-message">{message}</p>}
      </section>

      <section id="monthly" className="container salon-section">
        <div className="salon-section-title"><div><p className="salon-eyebrow">MONTHLY TOP 5</p><h2>이번 달 가장 사랑받은<br />시술이에요.</h2></div><p>고객님들이 선택한 여름 스타일을<br />지금 만나보세요.</p></div>
        <div className="salon-top-grid">
          <TopList title="HAIR STYLE TOP 5" services={styleTopFive} fallback="인기 헤어 스타일을 준비하고 있어요." onOpen={openGallery} />
          <TopList title="SCALP & HAIR CARE" services={careTopFive} fallback="두피·모발 케어를 준비하고 있어요." onOpen={openGallery} />
        </div>
      </section>

      <section id="services" className="salon-services-section">
        <div className="container salon-section">
          <div className="salon-section-title"><div><p className="salon-eyebrow">SIGNATURE MENU</p><h2>당신의 취향을 담은<br />시술을 골라보세요.</h2></div><button className="salon-outline-button" onClick={moveToReservation}>상담 후 예약하기</button></div>
          <div className="salon-filter" role="tablist">{categories.map((category) => <button key={category.id} className={activeCategory === category.id ? 'active' : ''} onClick={() => setActiveCategory(category.id)}>{category.label}</button>)}</div>
          <div className="salon-menu-grid">{visibleServices.map((item) => <article className="salon-menu-card" key={item.id}><button className="salon-gallery-open" type="button" onClick={() => openGallery(item)} aria-label={`${item.name} 전체 사진 보기`}><RotatingServiceImage service={item} badge={serviceBadges.get(item.id)} /><span>전체 사진 보기</span></button><div><span>{String(item.category || 'HAIR').toUpperCase()}</span><h3>{item.name}</h3><p>{item.description || '맞춤 상담 후 가장 잘 어울리는 스타일을 제안해 드립니다.'}</p><strong>{Number(item.price || 0).toLocaleString()}원</strong><button onClick={() => moveToReservation(item.id)} aria-label={`${item.name} 예약하기`}>예약하기 <b>→</b></button></div></article>)}</div>
          {!visibleServices.length && <p className="salon-empty">등록된 시술 메뉴를 준비하고 있습니다.</p>}
        </div>
      </section>

      <section id="designer" className="salon-designer">
        <div className="container salon-designer-copy"><p className="salon-eyebrow">DIRECTOR PROFILE</p><h2>원장과의 1:1 상담으로<br />완성하는 당신만의 디자인</h2><p>10년 이상의 현장 경험을 바탕으로 얼굴형, 모발 상태, 라이프스타일까지 고려해 가장 자연스러운 변화를 제안합니다.</p><dl><div><dt>CAREER</dt><dd>MARINBOY SALON DIRECTOR</dd></div><div><dt>SPECIALTY</dt><dd>PERSONAL COLOR · HAIR DESIGN · SCALP CARE</dd></div></dl><button className="salon-secondary-button" onClick={moveToReservation}>상담 예약하기</button></div>
      </section>

      <section id="visit" className="container salon-visit"><div><p className="salon-eyebrow">VISIT MARINBOY</p><h2>당신의 일상에<br />기분 좋은 변화를.</h2><p>전문 디자이너와 편안하게 상담하고,<br />나만의 아름다움을 발견해 보세요.</p><button className="salon-primary-button" onClick={moveToReservation}>지금 예약하기</button></div><div className="salon-visit-info"><p><b>OPEN</b> 10:00 – 19:00</p><p><b>CONTACT</b> 02.0000.0000</p><p><b>LOCATION</b> 서울특별시 마린보이살롱</p><a className="salon-map-link" href="https://www.google.com/maps/search/?api=1&query=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C+%EB%A7%88%EB%A6%B0%EB%B3%B4%EC%9D%B4%EC%82%B4%EB%A1%B1" target="_blank" rel="noreferrer">지도에서 위치 확인</a></div></section>

      <footer className="salon-footer"><div className="container"><strong>MARINBOY SALON</strong><span>© 2026 MARINBOY SALON. ALL RIGHTS RESERVED.</span>{user?.role === 'ADMIN' && <a className="salon-admin-edit-button" href="/admin">메뉴 수정</a>}</div></footer>
      {selectedService && <ServiceGallery service={selectedService} onClose={() => setSelectedService(null)} onReserve={moveToReservation} />}
      {showSignupPolicy && <SignupPolicyModal agreed={signupPolicyAgreed} onAgree={setSignupPolicyAgreed} onConfirm={() => { setShowSignupPolicy(false); setMessage('회원가입이 완료되었습니다. 가입한 아이디로 로그인해 주세요.'); }} />}
    </main>
  );
}

export default SalonHome;
