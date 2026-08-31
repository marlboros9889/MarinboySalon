import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { useDispatch, useSelector } from 'react-redux';
import AppLayout from '../components/AppLayout';
import ServiceImageCarousel from '../components/ServiceImageCarousel';
import {
  LOAD_SERVICE_ITEMS_REQUEST,
  LOAD_SERVICE_ITEMS_SUCCESS,
} from '../reducers/serviceItemReducer';
import { getArchiveLook } from '../utils/serviceItem';
import { loadServiceItemsForServer } from '../server/serviceItemServer';

export default function Services({ initialServiceItems = [], initialLoadError = null }) {
  const dispatch = useDispatch();
  const [selectedServiceId, setSelectedServiceId] = useState(null);
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

  const displayedServiceItems = serviceItems.length > 0 ? serviceItems : initialServiceItems;
  const serviceItemsError = loadServiceItemsError || initialLoadError;

  useEffect(() => {
    if (displayedServiceItems.length > 0 && selectedServiceId === null) {
      setSelectedServiceId(displayedServiceItems[0].id);
    }
  }, [displayedServiceItems, selectedServiceId]);

  const archiveServiceItems = useMemo(
    () => displayedServiceItems.map((item) => ({ item, look: getArchiveLook(item.name) })),
    [displayedServiceItems],
  );
  const archiveCategories = useMemo(
    () => [...new Set(archiveServiceItems.map(({ look }) => look.category))],
    [archiveServiceItems],
  );
  const selectedArchive = archiveServiceItems.find(({ item }) => item.id === selectedServiceId)
    || archiveServiceItems[0];

  return (
    <AppLayout>
      <section className="archive-menu-wrapper">
        <header className="archive-header-section">
          <p className="archive-eyebrow">SERVICE MENU & BEAUTY ARCHIVE</p>
          <h1 className="archive-header-title">ARTISAN SCRAPS</h1>
          <p className="archive-header-subtitle">시술 메뉴를 고르면 스타일 기록과 예약 정보를 함께 확인할 수 있습니다.</p>
        </header>
        {loadServiceItemsLoading && <p className="status-message">메뉴를 불러오는 중입니다.</p>}
        {serviceItemsError && <p className="error-message">{serviceItemsError}</p>}
        {!loadServiceItemsLoading && !serviceItemsError && displayedServiceItems.length === 0 && (
          <p className="status-message">현재 예약 가능한 메뉴가 없습니다.</p>
        )}
        {selectedArchive && (
          <div className="archive-content-grid">
            <div className="archive-menu-container">
              {archiveCategories.map((category) => (
                <section className="archive-menu-group" key={category}>
                  <h2 className="archive-category-title">{category}</h2>
                  {archiveServiceItems
                    .filter(({ look }) => look.category === category)
                    .map(({ item }) => (
                      <button
                        type="button"
                        key={item.id}
                        className={`archive-menu-item ${selectedArchive.item.id === item.id ? 'active' : ''}`}
                        onClick={() => setSelectedServiceId(item.id)}
                        onMouseEnter={() => setSelectedServiceId(item.id)}
                        aria-pressed={selectedArchive.item.id === item.id}
                      >
                        <span className="archive-item-info">
                          <strong className="archive-item-name">{item.name}</strong>
                          <span className="archive-item-description">{item.description}</span>
                          <span className="archive-item-duration">{item.durationMinutes} MIN</span>
                        </span>
                        <span className="archive-item-price">₩ {item.price.toLocaleString()}</span>
                      </button>
                    ))}
                </section>
              ))}
            </div>

            <aside className="archive-preview-container" aria-live="polite">
              <article className="archive-collage-card">
                <span className="archive-tape-effect" aria-hidden="true" />
                <ServiceImageCarousel
                  key={selectedArchive.item.id}
                  serviceItem={selectedArchive.item}
                  className="archive-image-wrapper"
                >
                  <span className="archive-image-tag">{selectedArchive.look.tag}</span>
                </ServiceImageCarousel>
                <div className="archive-details">
                  <p className="archive-look-category">{selectedArchive.look.category}</p>
                  <h2>{selectedArchive.item.name}</h2>
                  <p>{selectedArchive.item.description}</p>
                  <div className="archive-service-meta">
                    <span>{selectedArchive.item.durationMinutes}분</span>
                    <strong>₩ {selectedArchive.item.price.toLocaleString()}</strong>
                  </div>
                  <Link
                    href={`/reservations/new?serviceId=${selectedArchive.item.id}`}
                    className="archive-booking-button"
                  >
                    BOOK THIS STYLE
                  </Link>
                </div>
              </article>
            </aside>
          </div>
        )}
      </section>
    </AppLayout>
  );
}

/** 메뉴 목록 화면도 최초 응답 HTML에 실제 메뉴를 포함합니다. */
export async function getServerSideProps() {
  const result = await loadServiceItemsForServer();
  return {
    props: {
      initialServiceItems: result.serviceItems,
      initialLoadError: result.error,
    },
  };
}
