import { useEffect, useMemo, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { loadServicesRequest } from '../../../reducers/service';
import { getCurrentUser, sessionFetch } from '../../shared/api/sessionApi';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '';
const SALON_LUXURY_BANNER = '/images/salon-luxury-banner.png';
const SOCIAL_PROVIDERS = [
  { name: 'Kakao', key: 'kakao', label: '카카오로 시작하기' },
  { name: 'Naver', key: 'naver', label: '네이버로 시작하기' },
  { name: 'Google', key: 'google', label: 'Google로 시작하기' },
];

/** 서비스 이미지 경로를 안전하게 반환합니다. */
function serviceImage(item) {
  const category = item?.category || '';
  const isNail = /NAIL|네일/i.test(category);
  const fallback = `/images/catalog/catalog-${isNail ? 'nail' : 'hair'}-${(Number(item?.id || 1) % 5) + 1}-1.jpg`;
  // 외부 이미지 주소는 로컬 개발 환경에서 깨질 수 있어 제공받은 카탈로그 이미지로 대체합니다.
  if (!item?.imageUrl || item.imageUrl.startsWith('http')) return `${API_BASE_URL}${fallback}`;
  return `${API_BASE_URL}${item.imageUrl}`;
}

/** 등록된 대표·상세 이미지 전체를 반환하고, 이미지가 없을 때만 기본 카탈로그를 사용합니다. */
function serviceGalleryImages(item) {
  const category = item?.category || '';
  const type = /NAIL|네일/i.test(category) ? 'nail' : 'hair';
  const catalogNumber = (Number(item?.id || 1) % 5) + 1;
  const uploadedImages = [item?.imageUrl, ...(item?.additionalImageUrls || [])]
    .filter((url) => url && !url.startsWith('http'))
    .map((url) => `${API_BASE_URL}${url}`);
  const catalogImages = [1, 2, 3].map(
    (index) => `${API_BASE_URL}/images/catalog/catalog-${type}-${catalogNumber}-${index}.jpg`,
  );

  return [...new Set(uploadedImages.length ? uploadedImages : catalogImages)];
}

/** 대표 서비스의 카테고리별 목록을 구성합니다. */
function groupServices(items, keyword) {
  return items.filter((item) => {
    const category = item.category || '';
    return keyword === 'NAIL'
      ? /NAIL|네일/i.test(category)
      : /HAIR|헤어|컷|펌|클리닉|스타일링/i.test(category);
  });
}

/** 이번 달 예약 건수가 많은 순서로 카테고리별 TOP5를 반환합니다. */
function monthlyTopFive(items, keyword) {
  return [...groupServices(items, keyword)]
    .sort((left, right) => (Number(right.reservationCount) - Number(left.reservationCount))
      || (Number(left.topRank ?? 99) - Number(right.topRank ?? 99))
      || (Number(left.id) - Number(right.id)))
    .slice(0, 5);
}

/** 현재 월 예약 DB 건수를 기준으로 카테고리 1위는 BEST, 나머지 인기 메뉴는 HIT로 표시합니다. */
function popularityBadges(items) {
  const badges = new Map();
  ['HAIR', 'NAIL'].forEach((category) => {
    monthlyTopFive(items, category)
      .filter((item) => Number(item.reservationCount) > 0)
      .forEach((item, index) => badges.set(item.id, index === 0 ? 'BEST' : 'HIT'));
  });
  return badges;
}

function SalonHome() {
  //1. SSR에서 받은 시술 목록과 Saga 재조회 결과를 동일한 Redux 상태로 사용합니다.
  const dispatch = useDispatch();
  const { items: services, error: serviceError } = useSelector((state) => state.service);
  const [activeCategory, setActiveCategory] = useState('ALL');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [signupMode, setSignupMode] = useState(false);
  const [signup, setSignup] = useState({ username: '', password: '', name: '', email: '', phone: '' });
  const [duplicateChecks, setDuplicateChecks] = useState({ username: 'idle', email: 'idle' });
  const [showSignupPolicy, setShowSignupPolicy] = useState(false);
  const [signupPolicyAgreed, setSignupPolicyAgreed] = useState(false);
  const [heroImageIndex, setHeroImageIndex] = useState(0);
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState('');
  const [policyMessage, setPolicyMessage] = useState('');
  const [heroPolicyAgreed, setHeroPolicyAgreed] = useState(false);
  const [selectedService, setSelectedService] = useState(null);
  const policyCheckboxRef = useRef(null);

  useEffect(() => {
    //2. SSR 시점에 API 연결이 실패했거나 데이터가 없으면 Saga가 클라이언트에서 다시 요청합니다.
    if (!services.length) dispatch(loadServicesRequest());

    // 소셜 로그인 콜백의 세션 정보를 고객 화면에 연결합니다.
    getCurrentUser()
      .then((data) => data && setUser(data))
      .catch(() => null);
  }, [dispatch, services.length]);

  useEffect(() => {
    if (serviceError) setMessage(serviceError);
  }, [serviceError]);

  const categories = useMemo(() => ([
    { id: 'ALL', label: '전체 시술' },
    { id: 'HAIR', label: '헤어' },
    { id: 'NAIL', label: '네일' },
  ]), []);
  const visibleServices = activeCategory === 'ALL'
    ? services : groupServices(services, activeCategory);
  const hairTopFive = monthlyTopFive(services, 'HAIR');
  const nailTopFive = monthlyTopFive(services, 'NAIL');
  const serviceBadges = useMemo(() => popularityBadges(services), [services]);
  const heroImages = useMemo(
    () => [...new Set(services.flatMap((service) => serviceGalleryImages(service)))],
    [services],
  );
  const heroImage = heroImages[heroImageIndex % Math.max(heroImages.length, 1)] || serviceImage(null);

  useEffect(() => {
    // 메인 대표 이미지를 3.5초마다 다음 등록 시술 이미지로 변경합니다.
    if (heroImages.length < 2) return undefined;
    const timer = window.setInterval(() => {
      setHeroImageIndex((current) => (current + 1) % heroImages.length);
    }, 4500);
    return () => window.clearInterval(timer);
  }, [heroImages]);

  const checkDuplicate = async (field) => {
    const value = signup[field].trim();
    if (!value) {
      setDuplicateChecks((current) => ({ ...current, [field]: 'empty' }));
      return;
    }
    const path = field === 'username' ? 'check-username?username=' : 'check-email?email=';
    try {
      const response = await sessionFetch(`/api/auth/${path}${encodeURIComponent(value)}`);
      const result = response.ok ? await response.json() : { available: false };
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
      const response = await sessionFetch('/api/auth/login', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        // 서버 AuthService는 이메일이 아닌 username 필드를 로그인 식별자로 사용합니다.
        body: JSON.stringify({ username, password }),
      });
      if (!response.ok) throw new Error();
      const data = await response.json();
      setUser(data);
      setPassword('');
      setMessage(`${data.name || '고객'}님, 반갑습니다.`);
    } catch {
      setMessage('이메일 또는 비밀번호를 확인해 주세요.');
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
      const response = await sessionFetch('/api/auth/signup', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(signup),
      });
      if (!response.ok) throw new Error();
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
    await sessionFetch('/api/auth/logout', { method: 'POST' }).catch(() => null);
    setUser(null);
    setMessage('로그아웃되었습니다.');
  };

  const moveToReservation = (serviceId) => {
    // 시술 카드에서는 선택한 메뉴를 예약 화면으로 즉시 전달합니다.
    if (serviceId) {
      window.location.href = `/reservation?serviceId=${serviceId}`;
      return;
    }
    // 예약 화면으로 이동하기 전에 노쇼·당일 취소 제한 확인 여부를 명확히 받습니다.
    if (!policyCheckboxRef.current?.checked) {
      setPolicyMessage('예약 전 노쇼 및 당일 취소 제한 안내를 확인해 주세요.');
      document.getElementById('policy-agreement')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }
    setPolicyMessage('');
    window.location.href = '/reservation';
  };
  const moveToMyReservations = () => {
    // 로그인 고객의 예약 신청 화면이 아닌 예약 현황 화면으로 이동합니다.
    window.location.href = `${API_BASE_URL}/my-reservations`;
  };
  const openGallery = (service) => setSelectedService(service);
  const socialLogin = (provider) => { window.location.href = `${API_BASE_URL}/oauth2/authorization/${provider}`; };

  return (
    <main className="v3-app">
      <header className="v3-header container">
        <a className="v3-brand" href="#top" aria-label="Marinboy Salon 홈">MARINBOY<span> SALON</span></a>
        <nav className="v3-nav" aria-label="주요 메뉴">
          <a href="#monthly">이달의 추천</a><a href="#services">시술 메뉴</a><a href="#designer">디자이너</a><a href="#visit">매장 안내</a>
        </nav>
        <div className="v3-header-action">
          {user ? <button className="v3-text-button" onClick={signOut}>로그아웃</button> : <a className="v3-text-button" href="#login">로그인</a>}
          <button className="v3-primary-button v3-small-button" onClick={moveToReservation}>예약하기</button>
        </div>
      </header>

      <section id="top" className="v3-hero container">
        <div className="v3-hero-visual">
          <img className="v3-hero-rotating-image" src={SALON_LUXURY_BANNER} alt="마린보이살롱 프라이빗 살롱 내부" />
          <div className="v3-hero-caption"><strong>08</strong><span>PRIVATE BEAUTY<br />ARCHIVE</span></div>
        </div>
        <div className="v3-hero-copy">
          <p className="v3-eyebrow">MARINBOY PRIVATE SALON · 2026</p>
          <h1>나만의 결을 위한<br /><em><span className="v3-one-to-one">1:1</span> Private Beauty Design</em></h1>
          <p className="v3-lead">섬세한 상담부터 완성도 높은 디자인까지,<br />마린보이살롱에서 편안하게 경험하세요.</p>
          <div className="v3-hero-buttons">
            <button className="v3-primary-button" onClick={moveToReservation}>시술 예약하기</button>
            <a className="v3-secondary-button" href="#services">메뉴 둘러보기</a>
          </div>
          <div id="policy-agreement" className="v3-policy-agreement">
            <label className={`salon-check-field${heroPolicyAgreed ? ' is-agreed' : ''}`}>
              <input
                ref={policyCheckboxRef}
                className="salon-checkbox"
                type="checkbox"
                onChange={(event) => {
                  setHeroPolicyAgreed(event.target.checked);
                  if (event.target.checked) setPolicyMessage('');
                }}
              />
              <span className="salon-check-copy"><strong>필수 동의</strong><small>노쇼 및 당일 취소 제한 안내를 확인했습니다.</small></span>
              <span className="salon-check-state">{heroPolicyAgreed ? '동의 완료' : '미동의'}</span>
            </label>
            {policyMessage && <p role="alert">{policyMessage}</p>}
          </div>
        </div>
      </section>

      <section id="login" className="v3-login-section">
        <div className="container v3-login-grid">
          <div><p className="v3-eyebrow">MEMBERSHIP</p><h2>더 편리한 예약,<br />회원으로 시작하세요.</h2></div>
          {user ? <div className="v3-welcome"><strong>{user.name || '고객'}님</strong><span>예약 내역과 맞춤 서비스를 확인할 수 있어요.</span>{user.role === 'ADMIN' ? <div className="v3-welcome-actions"><a className="v3-secondary-button" href="/admin#reservation-status">예약 현황보기</a><a className="v3-primary-button" href="/admin#service-management">시술 메뉴 수정</a></div> : <div className="v3-welcome-actions"><button className="v3-secondary-button" onClick={moveToMyReservations}>나의 예약 보기</button><button className="v3-inline-button" type="button" onClick={() => setMessage('내 정보 수정은 예약 내역 화면에서 이용할 수 있습니다.')}>내 정보 수정</button></div>}</div>
            : signupMode ? <form className="v3-login-form" onSubmit={signUp}><div className="v3-signup-grid"><DuplicateField label="아이디" value={signup.username} status={duplicateChecks.username} onChange={(value) => changeSignupField('username', value)} onCheck={() => checkDuplicate('username')} /><input value={signup.password} onChange={(e) => changeSignupField('password', e.target.value)} type="password" placeholder="비밀번호" required /><input value={signup.name} onChange={(e) => changeSignupField('name', e.target.value)} placeholder="이름" required /><DuplicateField label="이메일" type="email" value={signup.email} status={duplicateChecks.email} onChange={(value) => changeSignupField('email', value)} onCheck={() => checkDuplicate('email')} /><input value={signup.phone} onChange={(e) => changeSignupField('phone', e.target.value)} placeholder="연락처" required /><button className="v3-primary-button" type="submit">가입하기</button></div><button className="v3-inline-button" type="button" onClick={() => setSignupMode(false)}>로그인으로 돌아가기</button></form>
              : <form className="v3-login-form" onSubmit={signIn}>
                <div className="v3-input-row"><input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="아이디" required /><input value={password} onChange={(e) => setPassword(e.target.value)} type="password" placeholder="비밀번호" required /><button className="v3-primary-button" type="submit">로그인</button></div>
                <div className="v3-social-row">{SOCIAL_PROVIDERS.map((provider) => <button type="button" className={`v3-social-button ${provider.key}`} onClick={() => socialLogin(provider.key)} key={provider.key}>{provider.label}</button>)}</div><button className="v3-inline-button" type="button" onClick={() => setSignupMode(true)}>처음이신가요? 회원가입</button>
              </form>}
        </div>
        {message && <p className="v3-message">{message}</p>}
      </section>

      <section id="monthly" className="container v3-section">
        <div className="v3-section-title"><div><p className="v3-eyebrow">MONTHLY TOP 5</p><h2>이번 달 가장 사랑받은<br />시술이에요.</h2></div><p>고객님들이 선택한 여름 스타일을<br />지금 만나보세요.</p></div>
        <div className="v3-top-grid">
          <TopList title="HAIR TOP 5" services={hairTopFive} badges={serviceBadges} fallback="헤어 인기 시술을 준비하고 있어요." onOpen={openGallery} />
          <TopList title="NAIL TOP 5" services={nailTopFive} badges={serviceBadges} fallback="네일 인기 시술을 준비하고 있어요." onOpen={openGallery} />
        </div>
      </section>

      <section id="services" className="v3-services-section">
        <div className="container v3-section">
          <div className="v3-section-title"><div><p className="v3-eyebrow">SIGNATURE MENU</p><h2>당신의 취향을 담은<br />시술을 골라보세요.</h2></div><button className="v3-outline-button" onClick={moveToReservation}>상담 후 예약하기</button></div>
          <div className="v3-filter" role="tablist">{categories.map((category) => <button key={category.id} className={activeCategory === category.id ? 'active' : ''} onClick={() => setActiveCategory(category.id)}>{category.label}</button>)}</div>
          <div className="v3-menu-grid">{visibleServices.map((item) => <article className="v3-menu-card" key={item.id}><button className="v3-gallery-open" type="button" onClick={() => openGallery(item)} aria-label={`${item.name} 전체 사진 보기`}><RotatingServiceImage service={item} badge={serviceBadges.get(item.id)} /><span>전체 사진 보기</span></button><div><span>{/NAIL|네일/i.test(item.category || '') ? 'NAIL' : 'HAIR'}</span><h3>{item.name}</h3><p>{item.description || '맞춤 상담 후 가장 잘 어울리는 스타일을 제안해 드립니다.'}</p><strong>{Number(item.price || 0).toLocaleString()}원</strong><button onClick={() => moveToReservation(item.id)} aria-label={`${item.name} 예약하기`}>예약하기 <b>→</b></button></div></article>)}</div>
          {!visibleServices.length && <p className="v3-empty">등록된 시술 메뉴를 준비하고 있습니다.</p>}
        </div>
      </section>

      <section id="designer" className="v3-designer" style={{ backgroundImage: `linear-gradient(90deg, rgba(5,5,5,.96), rgba(5,5,5,.46)), url(${SALON_LUXURY_BANNER})` }}>
        <div className="container v3-designer-copy"><p className="v3-eyebrow">DIRECTOR PROFILE</p><h2>원장과의 1:1 상담으로<br />완성하는 당신만의 디자인</h2><p>10년 이상의 현장 경험을 바탕으로 얼굴형, 모발 상태, 라이프스타일까지 고려해 가장 자연스러운 변화를 제안합니다.</p><dl><div><dt>CAREER</dt><dd>MARINBOY SALON DIRECTOR</dd></div><div><dt>SPECIALTY</dt><dd>PERSONAL COLOR · HAIR DESIGN · NAIL ART</dd></div></dl><button className="v3-secondary-button" onClick={moveToReservation}>상담 예약하기</button></div>
      </section>

      <section id="visit" className="container v3-visit"><div><p className="v3-eyebrow">VISIT MARINBOY</p><h2>당신의 일상에<br />기분 좋은 변화를.</h2><p>전문 디자이너와 편안하게 상담하고,<br />나만의 아름다움을 발견해 보세요.</p><button className="v3-primary-button" onClick={moveToReservation}>지금 예약하기</button></div><div className="v3-visit-info"><p><b>OPEN</b> 10:00 – 20:00</p><p><b>CONTACT</b> 02.0000.0000</p><p><b>LOCATION</b> 서울특별시 마린보이살롱</p><a className="v3-map-link" href="https://www.google.com/maps/search/?api=1&query=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C+%EB%A7%88%EB%A6%B0%EB%B3%B4%EC%9D%B4%EC%82%B4%EB%A1%B1" target="_blank" rel="noreferrer">지도에서 위치 확인</a></div></section>

      <footer className="v3-footer"><div className="container"><strong>MARINBOY SALON</strong><span>© 2026 MARINBOY SALON. ALL RIGHTS RESERVED.</span>{user?.role === 'ADMIN' && <a className="v3-admin-edit-button" href="/admin">메뉴 수정</a>}</div></footer>
      {selectedService && <ServiceGallery service={selectedService} onClose={() => setSelectedService(null)} onReserve={moveToReservation} />}
      {showSignupPolicy && <SignupPolicyModal agreed={signupPolicyAgreed} onAgree={setSignupPolicyAgreed} onConfirm={() => { setShowSignupPolicy(false); setMessage('회원가입이 완료되었습니다. 가입한 아이디로 로그인해 주세요.'); }} />}
    </main>
  );
}

function DuplicateField({ label, type = 'text', value, status, onChange, onCheck }) {
  const statusText = { available: '사용 가능', duplicate: '이미 사용 중', empty: '입력 필요', error: '확인 실패' }[status];
  return <div className="v3-duplicate-field"><div><input aria-label={label} type={type} value={value} onChange={(event) => onChange(event.target.value)} placeholder={label} required /><button type="button" onClick={onCheck}>중복 확인</button></div>{statusText && <small className={status === 'available' ? 'available' : 'unavailable'}>{statusText}</small>}</div>;
}

function SignupPolicyModal({ agreed, onAgree, onConfirm }) {
  return <div className="v3-policy-backdrop"><section className="v3-policy-modal" role="dialog" aria-modal="true" aria-labelledby="signup-policy-title"><p className="v3-eyebrow">RESERVATION POLICY</p><h2 id="signup-policy-title">노쇼·예약 취소 안내</h2><ul><li>방문이 어려운 경우 예약 시간 전에 반드시 취소해 주세요.</li><li>무단 불참, 10분 이상 지각, 당일 취소 시 이후 예약이 제한될 수 있습니다.</li><li>예약은 고객과 매장이 함께 지키는 소중한 약속입니다.</li></ul><label className={`salon-check-field${agreed ? ' is-agreed' : ''}`}><input className="salon-checkbox" type="checkbox" checked={agreed} onChange={(event) => onAgree(event.target.checked)} /><span className="salon-check-copy"><strong>필수 동의</strong><small>안내 내용을 읽고 확인했습니다.</small></span><span className="salon-check-state">{agreed ? '동의 완료' : '미동의'}</span></label><button className="v3-primary-button" type="button" disabled={!agreed} onClick={onConfirm}>확인하고 로그인으로 이동</button></section></div>;
}

function TopList({ title, services, fallback, onOpen }) {
  return <div className="v3-top-list"><h3>{title}</h3>{services.length ? services.map((item, index) => <button key={item.id} onClick={() => onOpen(item)} aria-label={`${item.name} 전체 사진 보기`}><span>0{index + 1}</span><RotatingServiceImage service={item} compact /><i><b>{item.name}</b><small>{Number(item.price || 0).toLocaleString()}원</small></i><em>사진 보기</em></button>) : <p>{fallback}</p>}</div>;
}

/** 시술별 대표·상세 이미지 전체를 4초 간격으로 순환합니다. */
function RotatingServiceImage({ service, badge, compact = false }) {
  const images = useMemo(() => serviceGalleryImages(service), [service]);
  const [imageIndex, setImageIndex] = useState(0);

  useEffect(() => {
    setImageIndex(0);
    if (images.length < 2) return undefined;
    const timer = window.setInterval(() => {
      setImageIndex((current) => (current + 1) % images.length);
    }, 3500);
    return () => window.clearInterval(timer);
  }, [images.length, service.id]);

  const image = images[imageIndex % Math.max(images.length, 1)] || serviceImage(service);
  return <div className={`v3-service-image-rotator${compact ? ' compact' : ''}`}><img key={image} src={image} alt={compact ? '' : service.name} onError={(event) => { event.currentTarget.src = `${API_BASE_URL}/images/catalog/catalog-hair-2-1.jpg`; }} />{badge && <span className={`v3-popularity-badge ${badge.toLowerCase()}`}>{badge}</span>}</div>;
}

/** 선택한 시술에 등록된 전체 사진과 예약 이동 버튼을 제공하는 상세 갤러리입니다. */
function ServiceGallery({ service, onClose, onReserve }) {
  const images = serviceGalleryImages(service);

  return <div className="v3-gallery-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
    <section className="v3-gallery-modal" role="dialog" aria-modal="true" aria-labelledby="service-gallery-title">
      <button className="v3-gallery-close" type="button" onClick={onClose} aria-label="사진 갤러리 닫기">×</button>
      <p className="v3-eyebrow">SERVICE GALLERY</p>
      <h2 id="service-gallery-title">{service.name}</h2>
      <div className="v3-gallery-images">
        {images.map((image, index) => <img key={image} src={image} alt={`${service.name} 시술 사진 ${index + 1}`} />)}
      </div>
      <div className="v3-gallery-actions">
        <p>{service.description || '여러 각도의 시술 이미지를 확인하고 예약해 주세요.'}</p>
        <button className="v3-primary-button" type="button" onClick={() => { onClose(); onReserve(); }}>이 시술 예약하기</button>
      </div>
    </section>
  </div>;
}

export default SalonHome;
