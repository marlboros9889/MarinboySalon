import { useEffect, useState } from 'react';
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
import api from '../api/axios';
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

const privateCareImages = [
  { src: '/images/private-care/private-care-1.png', alt: '디자이너가 고객의 모발을 염색하는 모습' },
  { src: '/images/private-care/private-care-2.png', alt: '프라이빗 공간에서 헤드 스파를 받는 모습' },
  { src: '/images/private-care/private-care-3.png', alt: '밝고 편안한 마린보이 살롱 내부' },
  { src: '/images/private-care/private-care-4.png', alt: '시술 도구와 헤어 제품이 놓인 거울 앞 공간' },
  { src: '/images/private-care/private-care-5.png', alt: '마린보이 살롱의 프라이빗한 리셉션 공간' },
];

/** 헤어와 네일 메뉴를 이름 기준으로 분리해 메인 인기 메뉴에 표시합니다. */
function isNailService(serviceItem) {
  return serviceItem.name.includes('네일');
}

function isHairService(serviceItem) {
  return !isNailService(serviceItem) && !serviceItem.name.includes('메이크업');
}

/** 헤어·네일 영역이 같은 카드 디자인을 사용하도록 공통 메뉴 목록을 만듭니다. */
function PopularMenuCards({ serviceItems, categoryLabel }) {
  const isNailMenu = categoryLabel === 'NAIL';
  const gradientColors = isNailMenu
    ? ['#E6D6F1', '#B78CC9', '#835E9E']
    : ['#F5D0CC', '#C88A83', '#A66861'];

  return (
    <div className="lumiere-menu-grid">
      {serviceItems.map((serviceItem, index) => (
        <article className="lumiere-menu-card" key={serviceItem.id}>
          <ServiceImageCarousel serviceItem={serviceItem} className="lumiere-menu-image">
            <div className={`popular-service-stamp ${isNailMenu ? 'nail-stamp' : 'hair-stamp'}`}>
              <span className="rank-text">TOP {index + 1}</span>
              <div className="m-badge" aria-hidden="true">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" className="m-svg">
                  <defs>
                    <linearGradient id={`popular-stamp-${serviceItem.id}`} x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stopColor={gradientColors[0]} />
                      <stop offset="50%" stopColor={gradientColors[1]} />
                      <stop offset="100%" stopColor={gradientColors[2]} />
                    </linearGradient>
                  </defs>
                  <path
                    d="M 20 60 C 15 60, 15 50, 20 45 C 25 35, 32 25, 38 23 C 41 22, 42 25, 43 28 C 47 38, 52 52, 54 58 C 58 45, 67 27, 74 23 C 77 21, 79 23, 78 27 C 75 38, 70 55, 68 62 C 67 66, 63 66, 61 62 C 55 50, 47 36, 44 30 C 39 40, 31 55, 25 61 C 23 62, 21 60, 20 60 Z"
                    fill="none"
                    stroke={`url(#popular-stamp-${serviceItem.id})`}
                    strokeWidth="5"
                    strokeLinecap="round"
                  />
                </svg>
              </div>
            </div>
          </ServiceImageCarousel>
          <div className="lumiere-menu-copy">
            <span>POPULAR SERVICE</span>
            <h3>{serviceItem.name}</h3>
            <p>{serviceItem.description || '고객의 스타일과 상태를 고려한 맞춤 디자인'}</p>
            <div>
              <strong>{serviceItem.price.toLocaleString()}원~</strong>
              <small><FiClock /> 약 {serviceItem.durationMinutes}분</small>
            </div>
            <Link href={`/reservations/new?serviceId=${serviceItem.id}`} aria-label={`${serviceItem.name} 예약하기`} />
          </div>
        </article>
      ))}
    </div>
  );
}

export default function Home({ initialServiceItems = [], initialLoadError = null }) {
  const dispatch = useDispatch();
  const { serviceItems, loadServiceItemsLoading, loadServiceItemsError } = useSelector(
    (state) => state.serviceItem,
  );
  const [privateCareImageIndex, setPrivateCareImageIndex] = useState(0);
  const [reviews, setReviews] = useState([]);
  const [reviewStartIndex, setReviewStartIndex] = useState(0);

  useEffect(() => {
    if (initialServiceItems.length > 0) {
      dispatch({ type: LOAD_SERVICE_ITEMS_SUCCESS, data: initialServiceItems });
      return;
    }
    dispatch({ type: LOAD_SERVICE_ITEMS_REQUEST });
  }, [dispatch, initialServiceItems]);

  useEffect(() => {
    // 1인 디자이너 공간의 다양한 분위기를 일정한 간격으로 보여 줍니다.
    const timerId = window.setInterval(() => {
      setPrivateCareImageIndex((currentIndex) => (currentIndex + 1) % privateCareImages.length);
    }, 4500);
    return () => window.clearInterval(timerId);
  }, []);

  useEffect(() => {
    const loadHighRatedReviews = async () => {
      try {
        const response = await api.get('/api/reviews');
        const highRatedReviews = response.data.filter((review) => review.rating >= 4);
        setReviews(highRatedReviews);
      } catch (error) {
        console.log('메인 리뷰를 불러오지 못했습니다.', error);
      }
    };
    loadHighRatedReviews();
  }, []);

  useEffect(() => {
    if (reviews.length <= 1) return undefined;
    const timerId = window.setInterval(() => {
      setReviewStartIndex((currentIndex) => (currentIndex + 1) % reviews.length);
    }, 5000);
    return () => window.clearInterval(timerId);
  }, [reviews.length]);

  // 헤어와 네일을 섞지 않고 각각 인기 메뉴 5개씩 보여줍니다.
  const displayedServiceItems = serviceItems.length > 0 ? serviceItems : initialServiceItems;
  const popularHairItems = displayedServiceItems.filter(isHairService).slice(0, 5);
  const popularNailItems = displayedServiceItems.filter(isNailService).slice(0, 5);
  const serviceItemsError = loadServiceItemsError || initialLoadError;
  const displayedReviews = reviews.length <= 4
    ? reviews
    : Array.from({ length: 4 }, (_, index) => reviews[(reviewStartIndex + index) % reviews.length]);

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
            <p className="eyebrow">MONTHLY HAIR</p>
            <h2 id="best-menu-title" className="display-text">HAIR TOP 5</h2>
          </div>
          <Link href="/services">전체 메뉴 보기 <FiArrowRight /></Link>
        </header>
        {loadServiceItemsLoading && <p className="status-message">메뉴를 불러오는 중입니다.</p>}
        {serviceItemsError && <p className="error-message">{serviceItemsError}</p>}
        <PopularMenuCards serviceItems={popularHairItems} categoryLabel="HAIR" />
      </section>

      <section className="lumiere-section container" id="nail-menu" aria-labelledby="nail-menu-title">
        <header className="lumiere-title-row">
          <div>
            <p className="eyebrow">MONTHLY NAIL</p>
            <h2 id="nail-menu-title" className="display-text">NAIL TOP 5</h2>
          </div>
          <Link href="/services">전체 메뉴 보기 <FiArrowRight /></Link>
        </header>
        <PopularMenuCards serviceItems={popularNailItems} categoryLabel="NAIL" />
      </section>

      <section className="private-care-section container">
        <div className="private-care-image">
          <img src={privateCareImages[privateCareImageIndex].src} alt={privateCareImages[privateCareImageIndex].alt} />
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
          {reviews.length === 0 && <p className="status-message">4점 이상 리뷰를 불러오는 중입니다.</p>}
          {displayedReviews.map((review) => <Link className="review-card" key={review.id} href={`/reviews/${review.id}`} target="_blank" rel="noopener noreferrer">
            <p className="review-score"><span>{'★'.repeat(review.rating)}</span> {review.rating.toFixed(1)}</p>
            <p>{review.content}</p>
            <small>— {review.userName} · {review.serviceName}</small>
          </Link>)}
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
