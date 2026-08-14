import { useEffect, useMemo, useRef, useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8082';
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

/** 대표·상세·카탈로그 이미지를 조합해 메뉴마다 최소 3장의 갤러리를 구성합니다. */
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

  return [...new Set([...uploadedImages, ...catalogImages])].slice(0, 3);
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

function App() {
  const [services, setServices] = useState([]);
  const [activeCategory, setActiveCategory] = useState('ALL');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [signupMode, setSignupMode] = useState(false);
  const [signup, setSignup] = useState({ username: '', password: '', name: '', email: '', phone: '' });
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState('');
  const [policyMessage, setPolicyMessage] = useState('');
  const [selectedService, setSelectedService] = useState(null);
  const policyCheckboxRef = useRef(null);

  useEffect(() => {
    // 공개 시술 메뉴는 로그인 여부와 관계없이 첫 화면에서 바로 불러옵니다.
    fetch(`${API_BASE_URL}/api/services`, { credentials: 'include' })
      .then((response) => response.ok ? response.json() : [])
      .then((data) => setServices(Array.isArray(data) ? data : []))
      .catch(() => setMessage('시술 메뉴를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'));

    // 소셜 로그인 콜백의 세션 정보를 고객 화면에 연결합니다.
    fetch(`${API_BASE_URL}/api/auth/me`, { credentials: 'include' })
      .then((response) => response.ok ? response.json() : null)
      .then((data) => data && setUser(data))
      .catch(() => null);
  }, []);

  const categories = useMemo(() => ([
    { id: 'ALL', label: '전체 시술' },
    { id: 'HAIR', label: '헤어' },
    { id: 'NAIL', label: '네일' },
  ]), []);
  const visibleServices = activeCategory === 'ALL'
    ? services : groupServices(services, activeCategory);
  const hairTopFive = monthlyTopFive(services, 'HAIR');
  const nailTopFive = monthlyTopFive(services, 'NAIL');
  const heroImage = serviceImage(services[0]);

  const signIn = async (event) => {
    event.preventDefault();
    setMessage('');
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' },
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
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/signup`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(signup),
      });
      if (!response.ok) throw new Error();
      setUsername(signup.username);
      setPassword('');
      setSignupMode(false);
      setMessage('회원가입이 완료되었습니다. 가입한 아이디로 로그인해 주세요.');
    } catch {
      setMessage('회원가입에 실패했습니다. 아이디 중복과 입력 정보를 확인해 주세요.');
    }
  };

  const signOut = async () => {
    await fetch(`${API_BASE_URL}/api/auth/logout`, { method: 'POST', credentials: 'include' }).catch(() => null);
    setUser(null);
    setMessage('로그아웃되었습니다.');
  };

  const moveToReservation = () => {
    // 예약 화면으로 이동하기 전에 노쇼·당일 취소 제한 확인 여부를 명확히 받습니다.
    if (!policyCheckboxRef.current?.checked) {
      setPolicyMessage('예약 전 노쇼 및 당일 취소 제한 안내를 확인해 주세요.');
      document.getElementById('policy-agreement')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }
    setPolicyMessage('');
    window.location.href = `${API_BASE_URL}/reservation`;
  };
  const openGallery = (service) => setSelectedService(service);
  const socialLogin = (provider) => { window.location.href = `${API_BASE_URL}/oauth2/authorization/${provider}`; };

  return (
    <main className="v3-app">
      <header className="v3-header container">
        <a className="v3-brand" href="#top" aria-label="Marinboy Salon 홈">MARINBOY<span> SALON</span></a>
        <nav className="v3-nav" aria-label="주요 메뉴">
          <a href="#monthly">이달의 추천</a><a href="#services">시술 메뉴</a><a href="#visit">매장 안내</a>
        </nav>
        <div className="v3-header-action">
          {user ? <button className="v3-text-button" onClick={signOut}>로그아웃</button> : <a className="v3-text-button" href="#login">로그인</a>}
          <button className="v3-primary-button v3-small-button" onClick={moveToReservation}>예약하기</button>
        </div>
      </header>

      <section id="top" className="v3-hero container">
        <div className="v3-hero-copy">
          <p className="v3-eyebrow">SUMMER EDITION · 2026</p>
          <h1>나에게 가장 잘 어울리는<br /><em>여름의 변화</em></h1>
          <p className="v3-lead">섬세한 상담부터 완성도 높은 디자인까지,<br />마린보이살롱에서 편안하게 경험하세요.</p>
          <div className="v3-hero-buttons">
            <button className="v3-primary-button" onClick={moveToReservation}>시술 예약하기</button>
            <a className="v3-secondary-button" href="#services">메뉴 둘러보기</a>
          </div>
          <div id="policy-agreement" className="v3-policy-agreement">
            <label>
              <input
                ref={policyCheckboxRef}
                type="checkbox"
                onChange={(event) => {
                  if (event.target.checked) setPolicyMessage('');
                }}
              />
              노쇼 및 당일 취소 제한 안내를 확인하고 동의합니다.
            </label>
            {policyMessage && <p role="alert">{policyMessage}</p>}
          </div>
        </div>
        <div className="v3-hero-visual">
          <img src={heroImage} alt="마린보이살롱 대표 시술" />
          <div className="v3-hero-caption"><strong>08</strong><span>Cool &amp; clear<br />beauty moment</span></div>
        </div>
      </section>

      <section id="login" className="v3-login-section">
        <div className="container v3-login-grid">
          <div><p className="v3-eyebrow">MEMBERSHIP</p><h2>더 편리한 예약,<br />회원으로 시작하세요.</h2></div>
          {user ? <div className="v3-welcome"><strong>{user.name || '고객'}님</strong><span>예약 내역과 맞춤 서비스를 확인할 수 있어요.</span>{user.role === 'ADMIN' ? <div className="v3-welcome-actions"><a className="v3-secondary-button" href={`${API_BASE_URL}/admin#reservation-status`}>예약 현황보기</a><a className="v3-primary-button" href={`${API_BASE_URL}/admin#service-management`}>시술 메뉴 수정</a></div> : <button className="v3-secondary-button" onClick={moveToReservation}>내 예약 보기</button>}</div>
            : signupMode ? <form className="v3-login-form" onSubmit={signUp}><div className="v3-signup-grid"><input value={signup.username} onChange={(e) => setSignup({ ...signup, username: e.target.value })} placeholder="아이디" required /><input value={signup.password} onChange={(e) => setSignup({ ...signup, password: e.target.value })} type="password" placeholder="비밀번호" required /><input value={signup.name} onChange={(e) => setSignup({ ...signup, name: e.target.value })} placeholder="이름" required /><input value={signup.email} onChange={(e) => setSignup({ ...signup, email: e.target.value })} type="email" placeholder="이메일" required /><input value={signup.phone} onChange={(e) => setSignup({ ...signup, phone: e.target.value })} placeholder="연락처" required /><button className="v3-primary-button" type="submit">가입하기</button></div><button className="v3-inline-button" type="button" onClick={() => setSignupMode(false)}>로그인으로 돌아가기</button></form>
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
          <TopList title="HAIR TOP 5" services={hairTopFive} fallback="헤어 인기 시술을 준비하고 있어요." onOpen={openGallery} />
          <TopList title="NAIL TOP 5" services={nailTopFive} fallback="네일 인기 시술을 준비하고 있어요." onOpen={openGallery} />
        </div>
      </section>

      <section id="services" className="v3-services-section">
        <div className="container v3-section">
          <div className="v3-section-title"><div><p className="v3-eyebrow">SIGNATURE MENU</p><h2>당신의 취향을 담은<br />시술을 골라보세요.</h2></div><button className="v3-outline-button" onClick={moveToReservation}>상담 후 예약하기</button></div>
          <div className="v3-filter" role="tablist">{categories.map((category) => <button key={category.id} className={activeCategory === category.id ? 'active' : ''} onClick={() => setActiveCategory(category.id)}>{category.label}</button>)}</div>
          <div className="v3-menu-grid">{visibleServices.map((item) => <article className="v3-menu-card" key={item.id}><button className="v3-gallery-open" type="button" onClick={() => openGallery(item)} aria-label={`${item.name} 사진 3장 보기`}><img src={serviceImage(item)} alt={item.name} onError={(event) => { event.currentTarget.src = `${API_BASE_URL}/images/catalog/catalog-hair-2-1.jpg`; }} /><span>사진 3장 보기</span></button><div><span>{/NAIL|네일/i.test(item.category || '') ? 'NAIL' : 'HAIR'}</span><h3>{item.name}</h3><p>{item.description || '맞춤 상담 후 가장 잘 어울리는 스타일을 제안해 드립니다.'}</p><strong>{Number(item.price || 0).toLocaleString()}원</strong><button onClick={moveToReservation} aria-label={`${item.name} 예약하기`}>예약하기 <b>→</b></button></div></article>)}</div>
          {!visibleServices.length && <p className="v3-empty">등록된 시술 메뉴를 준비하고 있습니다.</p>}
        </div>
      </section>

      <section id="visit" className="container v3-visit"><div><p className="v3-eyebrow">VISIT MARINBOY</p><h2>당신의 일상에<br />기분 좋은 변화를.</h2><p>전문 디자이너와 편안하게 상담하고,<br />나만의 아름다움을 발견해 보세요.</p><button className="v3-primary-button" onClick={moveToReservation}>지금 예약하기</button></div><div className="v3-visit-info"><p><b>OPEN</b> 10:00 – 20:00</p><p><b>CONTACT</b> 02.0000.0000</p><p><b>LOCATION</b> 서울특별시 마린보이살롱</p></div></section>

      <footer className="v3-footer"><div className="container"><strong>MARINBOY SALON</strong><span>© 2026 MARINBOY SALON. ALL RIGHTS RESERVED.</span>{user?.role === 'ADMIN' && <a className="v3-admin-edit-button" href={`${API_BASE_URL}/admin`}>메뉴 수정</a>}</div></footer>
      {selectedService && <ServiceGallery service={selectedService} onClose={() => setSelectedService(null)} onReserve={moveToReservation} />}
    </main>
  );
}

function TopList({ title, services, fallback, onOpen }) {
  return <div className="v3-top-list"><h3>{title}</h3>{services.length ? services.map((item, index) => <button key={item.id} onClick={() => onOpen(item)} aria-label={`${item.name} 사진 3장 보기`}><span>0{index + 1}</span><img src={serviceImage(item)} alt="" /><i><b>{item.name}</b><small>{Number(item.price || 0).toLocaleString()}원</small></i><em>사진 보기</em></button>) : <p>{fallback}</p>}</div>;
}

/** 선택한 시술의 사진 3장과 예약 이동 버튼을 제공하는 상세 갤러리입니다. */
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

export default App;
