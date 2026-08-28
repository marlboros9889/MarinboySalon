import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useRouter } from 'next/router';
import Link from 'next/link';
import AppLayout from '../../components/AppLayout';
import {
  CANCEL_RESERVATION_REQUEST,
  LOAD_MY_RESERVATIONS_REQUEST,
} from '../../reducers/reservationReducer';

const statusLabel = {
  REQUESTED: '접수',
  CONFIRMED: '확정',
  COMPLETED: '완료',
  CANCELED: '취소',
};

export default function ReservationList() {
  const dispatch = useDispatch();
  const router = useRouter();
  const { me } = useSelector((state) => state.auth);
  const { reservations, loadReservationsLoading, reservationError } = useSelector(
    (state) => state.reservation,
  );

  useEffect(() => {
    const token = window.localStorage.getItem('accessToken');
    if (!token) {
      router.replace('/auth/login?returnTo=/reservations');
      return;
    }
    dispatch({ type: LOAD_MY_RESERVATIONS_REQUEST });
  }, [dispatch, router, me]);

  const onCancel = (id) => {
    if (window.confirm('이 예약을 취소하시겠습니까?')) {
      dispatch({ type: CANCEL_RESERVATION_REQUEST, data: id });
    }
  };

  return (
    <AppLayout>
      <section className="page-section container">
        <header className="page-heading">
          <p className="eyebrow">MY BOOKING</p>
          <h1 className="heading-text">내 예약</h1>
        </header>
        {loadReservationsLoading && <p className="status-message">예약을 불러오는 중입니다.</p>}
        {reservationError && <p className="error-message">{reservationError}</p>}
        <div className="reservation-list">
          {reservations.map((item) => (
            <article className="reservation-card" key={item.id}>
              <div>
                <span className={`status-badge status-${item.status.toLowerCase()}`}>
                  {statusLabel[item.status]}
                </span>
                <h2 className="heading-text">{item.serviceName}</h2>
                <p>{item.reservationStart.replace('T', ' ')}</p>
                {item.requestMemo && <small>{item.requestMemo}</small>}
              </div>
              {item.status !== 'CANCELED' && item.status !== 'COMPLETED' && (
                <button type="button" className="outline-button" onClick={() => onCancel(item.id)}>예약 취소</button>
              )}
              {item.status === 'COMPLETED' && (
                <Link className="outline-button" href={`/reviews/new?reservationId=${item.id}`}>리뷰 작성</Link>
              )}
            </article>
          ))}
          {!loadReservationsLoading && reservations.length === 0 && (
            <p className="empty-message">등록된 예약이 없습니다.</p>
          )}
        </div>
      </section>
    </AppLayout>
  );
}
