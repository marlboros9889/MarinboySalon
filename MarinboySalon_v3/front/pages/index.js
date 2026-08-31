import { useEffect } from 'react';
import Head from 'next/head';
import Link from 'next/link';
import { useDispatch, useSelector } from 'react-redux';
import {
  FiArrowRight,
  FiClock,
  FiDroplet,
  FiHeart,
  FiInstagram,
  FiMessageCircle,
  FiScissors,
  FiShield,
  FiUserCheck,
  FiWind,
} from 'react-icons/fi';
import AppLayout from '../components/AppLayout';
import ServiceImageCarousel from '../components/ServiceImageCarousel';
import { LOAD_SERVICE_ITEMS_REQUEST } from '../reducers/serviceItemReducer';
import { LOAD_SERVICE_ITEMS_SUCCESS } from '../reducers/serviceItemReducer';
import { loadServiceItemsForServer } from '../server/serviceItemServer';

const serviceCategories = [
  { icon: FiScissors, name: 'CUT', label: '컷' },
  { icon: FiWind, name: 'PERM', label: '펌' },
  { icon: FiDroplet, name: 'COLOR', label: '컬러' },
  { icon: FiHeart, name: 'CLINIC', label: '클리닉' },
];

const concerns = [
  { icon: FiScissors, title: '손질이 어려워요', description: '매일 쉽게 손질할 수 있도록 라이프스타일에 맞춰 디자인해요.' },
  { icon: FiWind, title: '볼륨이 없어요', description: '얼굴형과 모발 상태를 고려해 자연스러운 볼륨을 찾아드려요.' },
  { icon: FiHeart, title: '손상이 심해요', description: '현재 모발 상태에 맞는 섬세한 케어를 제안해드려요.' },
  { icon: FiDroplet, title: '컬러가 고민이에요', description: '피부 톤과 분위기에 맞는 자연스러운 컬러를 제안해드려요.' },
];

const reviews = [
  { score: '5.0', text: '상담이 정말 꼼꼼하고 친절해요. 결과도 너무 만족합니다.', customer: '20대 고객' },
  { score: '5.0', text: '원하는 느낌을 정확히 이해해주셔서 편하게 맡길 수 있었어요.', customer: '30대 고객' },
  { score: '4.9', text: '조용한 공간에서 처음부터 끝까지 세심하게 관리받았어요.', customer: '20대 고객' },
  { score: '5.0', text: '홈케어 방법까지 알려주셔서 손질하기 훨씬 편해졌어요.', customer: '30대 고객' },
];

export default function Home({ initialServiceItems = [], initialLoadError = null }) {
  const dispatch = useDispatch();
  const { serviceItems, loadServiceItemsLoading, loadServiceItemsError } = useSelector(
    (state) => state.serviceItem,
  );

  useEffect(() => {
    if (initialServiceItems.length > 0) {
      dispatch({ type: LOAD_SERVICE_ITEMS_SUCCESS, data: initialServiceItems });
      return;
    }
    dispatch({ type: LOAD_SERVICE_ITEMS_REQUEST });
  }, [dispatch, initialServiceItems]);

  // 현재 메뉴 정렬의 앞 3개를 메인 화면 인기 시술로 고정합니다.
  const displayedServiceItems = serviceItems.length > 0 ? serviceItems : initialServiceItems;
  const popularServiceItems = displayedServiceItems.slice(0, 3);
  const serviceItemsError = loadServiceItemsError || initialLoadError;

  return (
    <AppLayout>
      <Head>
        <title>Marinboy Hair Salon</title>
        <meta name="description" content="1인 디자이너가 책임지는 프라이빗 헤어살롱" />
      </Head>
      <section className="lumiere-hero" id="about">
        <div className="lumiere-hero-content">
          <p className="eyebrow">PRIVATE HAIR SALON</p>
          <h1>일상에 빛을 더하는<br />섬세한 디자인</h1>
          <p className="lumiere-hero-description">
            자연스러운 아름다움을,<br />마린보이 헤어살롱이 함께합니다.
          </p>
          <div className="lumiere-hero-actions">
            <Link href="/reservations/new" className="primary-link">예약하기</Link>
            <a href="https://www.instagram.com" target="_blank" rel="noreferrer"><FiInstagram /> Instagram</a>
            <a href="#contact"><FiMessageCircle /> Kakao 상담</a>
          </div>
        </div>
      </section>

      <section className="lumiere-service-bar container" aria-label="시술 카테고리">
        {serviceCategories.map(({ icon: Icon, name, label }) => (
          <Link href="/services" className="lumiere-service-icon" key={name}>
            <Icon aria-hidden="true" />
            <strong>{name}</strong>
            <small>{label}</small>
          </Link>
        ))}
      </section>

      <section className="lumiere-section container" id="style">
        <header className="lumiere-section-heading">
          <p className="eyebrow">FOR YOUR HAIR</p>
          <h2>당신의 고민에 맞는<br />솔루션을 찾아보세요.</h2>
        </header>
        <div className="concern-grid">
          {concerns.map(({ icon: Icon, title, description }) => (
            <article className="concern-card" key={title}>
              <Icon aria-hidden="true" />
              <h3>{title}</h3>
              <p>{description}</p>
              <Link href="/services">자세히 보기 <FiArrowRight /></Link>
            </article>
          ))}
        </div>
      </section>

      <section className="lumiere-section container" id="menu" aria-labelledby="best-menu-title">
        <header className="lumiere-title-row">
          <div>
            <p className="eyebrow">POPULAR SERVICES</p>
            <h2 id="best-menu-title" className="display-text">BEST MENU</h2>
          </div>
          <Link href="/services">전체 메뉴 보기 <FiArrowRight /></Link>
        </header>
        {loadServiceItemsLoading && <p className="status-message">메뉴를 불러오는 중입니다.</p>}
        {serviceItemsError && <p className="error-message">{serviceItemsError}</p>}
        <div className="lumiere-menu-grid">
          {popularServiceItems.map((serviceItem, index) => (
            <article className="lumiere-menu-card" key={serviceItem.id}>
              <ServiceImageCarousel serviceItem={serviceItem} className="lumiere-menu-image">
                <span className="popular-service-badge">인기 시술 TOP {index + 1}</span>
              </ServiceImageCarousel>
              <div className="lumiere-menu-copy">
                <span>POPULAR SERVICE</span>
                <h3>{serviceItem.name}</h3>
                <p>{serviceItem.description || '고객의 얼굴형과 모발 상태를 고려한 맞춤 디자인'}</p>
                <div>
                  <strong>{serviceItem.price.toLocaleString()}원~</strong>
                  <small><FiClock /> 약 {serviceItem.durationMinutes}분</small>
                </div>
                <Link href={`/reservations/new?serviceId=${serviceItem.id}`} aria-label={`${serviceItem.name} 예약하기`} />
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="private-care-section container">
        <div className="private-care-image">
          <img src="/images/designer-private-care.webp" alt="고객의 모발을 상담하는 1인 헤어 디자이너" />
        </div>
        <div className="private-care-content">
          <p className="eyebrow">PRIVATE CARE</p>
          <h2>1인 디자이너가 책임지는<br />프라이빗한 시간</h2>
          <div className="private-care-features">
            <article><FiUserCheck /><div><h3>충분한 상담</h3><p>1:1 맞춤 상담으로 원하는 분위기와 고민을 충분히 이해합니다.</p></div></article>
            <article><FiScissors /><div><h3>섬세한 시술</h3><p>모발 상태와 얼굴형을 고려해 처음부터 끝까지 직접 시술합니다.</p></div></article>
            <article><FiShield /><div><h3>프라이빗 공간</h3><p>편안하고 조용한 공간에서 여유롭게 관리받을 수 있습니다.</p></div></article>
          </div>
        </div>
      </section>

      <section className="lumiere-section container" id="review">
        <header className="lumiere-title-row">
          <div><p className="eyebrow">CUSTOMER STORY</p><h2 className="display-text">REAL REVIEW</h2></div>
        </header>
        <div className="review-grid">
          {reviews.map((review) => (
            <article className="review-card" key={review.text}>
              <p className="review-score"><span>★★★★★</span> {review.score}</p>
              <p>{review.text}</p>
              <small>— {review.customer}</small>
            </article>
          ))}
        </div>
      </section>
    </AppLayout>
  );
}

/** 메뉴가 들어 있는 HTML을 서버에서 바로 내려주기 위한 SSR 진입점입니다. */
export async function getServerSideProps() {
  const result = await loadServiceItemsForServer();
  return {
    props: {
      initialServiceItems: result.serviceItems,
      initialLoadError: result.error,
    },
  };
}
