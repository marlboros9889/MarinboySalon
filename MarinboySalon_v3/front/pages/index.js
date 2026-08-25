import { useEffect } from 'react';
import Link from 'next/link';
import { useDispatch, useSelector } from 'react-redux';
import AppLayout from '../components/AppLayout';
import ServiceImageCarousel from '../components/ServiceImageCarousel';
import { LOAD_SERVICE_ITEMS_REQUEST } from '../reducers/serviceItemReducer';

export default function Home() {
  const dispatch = useDispatch();
  const { serviceItems, loadServiceItemsLoading, loadServiceItemsError } = useSelector(
    (state) => state.serviceItem,
  );

  useEffect(() => {
    dispatch({ type: LOAD_SERVICE_ITEMS_REQUEST });
  }, [dispatch]);

  return (
    <AppLayout>
      <section className="hero-section container">
        <div className="hero-copy">
          <p className="eyebrow">ONE PERSON HAIR STUDIO</p>
          <h1 className="heading-text">당신의 하루에<br />어울리는 머리</h1>
          <p className="hero-description">
            충분한 상담 시간과 겹치지 않는 예약으로 한 사람에게 집중합니다.
          </p>
          <div className="hero-actions">
            <Link href="/reservations/new" className="primary-link">예약하기</Link>
            <Link href="/services" className="text-link">시술 메뉴 보기 →</Link>
          </div>
        </div>
        <div className="hero-salon-visual">
          <img src="/images/salon-background.png" alt="마린보이 살롱 내부" />
          <div className="hero-salon-caption torn-paper-edge">
            <span className="display-text">MARINBOY</span>
            <small>PRIVATE SALON</small>
          </div>
        </div>
      </section>

      <section className="home-menu-section container" aria-labelledby="home-menu-title">
        <header className="home-menu-heading">
          <p className="eyebrow">SERVICE ARCHIVE</p>
          <h2 id="home-menu-title" className="display-text">ARTISAN MENU</h2>
          <p>각 메뉴의 실제 스타일 이미지가 3초마다 바뀝니다.</p>
        </header>
        {loadServiceItemsLoading && <p className="status-message">메뉴를 불러오는 중입니다.</p>}
        {loadServiceItemsError && <p className="error-message">{loadServiceItemsError}</p>}
        <div className="home-service-grid">
          {serviceItems.map((serviceItem) => (
            <article className="home-service-card" key={serviceItem.id}>
              <ServiceImageCarousel
                serviceItem={serviceItem}
                className="home-service-image"
              />
              <div className="home-service-copy">
                <p>{serviceItem.durationMinutes} MIN</p>
                <h3 className="heading-text">{serviceItem.name}</h3>
                <span>₩ {serviceItem.price.toLocaleString()}</span>
                <Link href={`/reservations/new?serviceId=${serviceItem.id}`}>BOOK THIS STYLE →</Link>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="feature-section container">
        <article><span>01</span><h2 className="heading-text">정확한 시간</h2><p>시술 시간과 기존 예약을 함께 계산합니다.</p></article>
        <article><span>02</span><h2 className="heading-text">간단한 예약</h2><p>메뉴와 날짜를 고르면 바로 신청할 수 있습니다.</p></article>
        <article><span>03</span><h2 className="heading-text">직접 관리</h2><p>영업시간과 휴무일을 관리자가 직접 바꿉니다.</p></article>
      </section>
    </AppLayout>
  );
}
