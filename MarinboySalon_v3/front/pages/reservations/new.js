import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useRouter } from 'next/router';
import AppLayout from '../../components/AppLayout';
import { LOAD_SERVICE_ITEMS_REQUEST } from '../../reducers/serviceItemReducer';
import { CREATE_RESERVATION_REQUEST } from '../../reducers/reservationReducer';
import { getValidServiceId } from '../../utils/serviceItem';

export default function NewReservation() {
  const dispatch = useDispatch();
  const router = useRouter();
  const { me } = useSelector((state) => state.auth);
  const { serviceItems } = useSelector((state) => state.serviceItem);
  const { createReservationLoading, createReservationDone, reservationError } = useSelector(
    (state) => state.reservation,
  );
  const [serviceId, setServiceId] = useState('');
  const [reservationStart, setReservationStart] = useState('');
  const [requestMemo, setRequestMemo] = useState('');

  useEffect(() => {
    dispatch({ type: LOAD_SERVICE_ITEMS_REQUEST });
  }, [dispatch]);

  useEffect(() => {
    if (!router.isReady || serviceItems.length === 0 || !router.query.serviceId) {
      return;
    }

    // 메뉴 화면에서 선택한 서비스가 실제 목록에 있을 때만 예약 폼에 반영합니다.
    const validServiceId = getValidServiceId(serviceItems, router.query.serviceId);
    if (validServiceId) {
      setServiceId(validServiceId);
    }
  }, [router.isReady, router.query.serviceId, serviceItems]);

  useEffect(() => {
    if (createReservationDone) {
      router.push('/reservations');
    }
  }, [createReservationDone, router]);

  const onSubmit = (event) => {
    event.preventDefault();
    if (!me) {
      router.push('/auth/login?returnTo=/reservations/new');
      return;
    }
    dispatch({
      type: CREATE_RESERVATION_REQUEST,
      data: { serviceId: Number(serviceId), reservationStart, requestMemo },
    });
  };

  return (
    <AppLayout>
      <section className="page-section container">
        <header className="page-heading">
          <p className="eyebrow">RESERVATION</p>
          <h1 className="serif-text">예약 신청</h1>
          <p>영업시간, 휴무일, 기존 예약은 서버에서 다시 확인합니다.</p>
        </header>
        <form className="booking-layout" onSubmit={onSubmit}>
          <div className="paper-panel torn-paper-edge">
            <span className="step-number serif-text">01</span>
            <h2 className="serif-text">DATE & SERVICE</h2>
            <label htmlFor="serviceId">시술 메뉴</label>
            <select id="serviceId" value={serviceId} onChange={(event) => setServiceId(event.target.value)} required>
              <option value="">시술을 선택해 주세요</option>
              {serviceItems.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name} · {item.durationMinutes}분 · {item.price.toLocaleString()}원
                </option>
              ))}
            </select>
            <label htmlFor="reservationStart">예약 일시</label>
            <input
              id="reservationStart"
              type="datetime-local"
              value={reservationStart}
              onChange={(event) => setReservationStart(event.target.value)}
              required
            />
          </div>
          <div className="paper-panel accent-panel torn-paper-edge">
            <span className="step-number serif-text">02</span>
            <h2 className="serif-text">REQUEST</h2>
            <label htmlFor="requestMemo">요청사항</label>
            <textarea
              id="requestMemo"
              rows="7"
              maxLength="500"
              value={requestMemo}
              onChange={(event) => setRequestMemo(event.target.value)}
              placeholder="모발 상태나 원하는 스타일을 적어 주세요."
            />
            {reservationError && <p className="error-message">{reservationError}</p>}
            <button type="submit" className="primary-button" disabled={createReservationLoading}>
              {createReservationLoading ? '예약 확인 중...' : '예약 신청하기'}
            </button>
          </div>
        </form>
      </section>
    </AppLayout>
  );
}
