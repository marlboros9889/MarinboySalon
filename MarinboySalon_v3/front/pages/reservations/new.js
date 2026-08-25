import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useRouter } from 'next/router';
import AppLayout from '../../components/AppLayout';
import { LOAD_SERVICE_ITEMS_REQUEST } from '../../reducers/serviceItemReducer';
import {
  CREATE_RESERVATION_REQUEST,
  LOAD_AVAILABLE_TIMES_REQUEST,
} from '../../reducers/reservationReducer';
import { getValidServiceId } from '../../utils/serviceItem';
import { formatDateInputValue, formatTimeLabel } from '../../utils/reservation';

export default function NewReservation() {
  const dispatch = useDispatch();
  const router = useRouter();
  const { me } = useSelector((state) => state.auth);
  const { serviceItems } = useSelector((state) => state.serviceItem);
  const {
    availableTimes,
    loadAvailableTimesLoading,
    availableTimesError,
    createReservationLoading,
    createReservationDone,
    reservationError,
  } = useSelector(
    (state) => state.reservation,
  );
  const [serviceId, setServiceId] = useState('');
  const [reservationDate, setReservationDate] = useState('');
  const [reservationTime, setReservationTime] = useState('');
  const [requestMemo, setRequestMemo] = useState('');
  const minimumDate = formatDateInputValue(new Date());
  const selectedService = serviceItems.find((item) => String(item.id) === serviceId);
  const progressStep = !serviceId ? 1 : !reservationDate ? 2 : !reservationTime ? 3 : 4;

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

  useEffect(() => {
    setReservationTime('');
    if (!serviceId || !reservationDate) {
      return;
    }
    dispatch({
      type: LOAD_AVAILABLE_TIMES_REQUEST,
      data: { serviceId: Number(serviceId), date: reservationDate },
    });
  }, [dispatch, reservationDate, serviceId]);

  const onSubmit = (event) => {
    event.preventDefault();
    if (!me) {
      router.push('/auth/login?returnTo=/reservations/new');
      return;
    }
    dispatch({
      type: CREATE_RESERVATION_REQUEST,
      data: {
        serviceId: Number(serviceId),
        reservationStart: `${reservationDate}T${reservationTime}:00`,
        requestMemo,
      },
    });
  };

  return (
    <AppLayout>
      <section className="page-section container">
        <header className="page-heading">
          <p className="eyebrow">RESERVATION</p>
          <h1 className="heading-text">예약하기</h1>
          <p>영업시간, 휴무일, 기존 예약은 서버에서 다시 확인합니다.</p>
        </header>
        <ol className="reservation-progress" aria-label="예약 진행 단계">
          {['시술 선택', '날짜 선택', '시간 선택', '정보 입력'].map((label, index) => (
            <li className={progressStep >= index + 1 ? 'active' : ''} key={label}>
              <span>{index + 1}</span>
              <strong>{label}</strong>
            </li>
          ))}
        </ol>
        <form className="booking-layout" onSubmit={onSubmit}>
          <div className="paper-panel torn-paper-edge">
            <span className="step-number display-text">01</span>
            <h2 className="display-text">DATE & SERVICE</h2>
            <fieldset className="service-choice-fieldset">
              <legend>시술 메뉴</legend>
              <div className="service-choice-grid">
              {serviceItems.map((item) => (
                <button
                  type="button"
                  className={serviceId === String(item.id) ? 'service-choice active' : 'service-choice'}
                  aria-pressed={serviceId === String(item.id)}
                  key={item.id}
                  onClick={() => setServiceId(String(item.id))}
                >
                  <img src={item.imageUrls?.[0] || '/images/salon-background.png'} alt="" />
                  <span>
                    <strong>{item.name}</strong>
                    <small>{item.price.toLocaleString()}원~ · 약 {item.durationMinutes}분</small>
                  </span>
                </button>
              ))}
              </div>
            </fieldset>
            <label htmlFor="reservationDate">예약 날짜</label>
            <input
              id="reservationDate"
              name="reservationDate"
              type="date"
              min={minimumDate}
              value={reservationDate}
              onChange={(event) => setReservationDate(event.target.value)}
              required
            />
            <fieldset className="time-slot-fieldset">
              <legend>예약 시간 <small>30분 단위</small></legend>
              {!serviceId || !reservationDate ? (
                <p className="time-slot-guide">메뉴와 날짜를 먼저 선택해 주세요.</p>
              ) : loadAvailableTimesLoading ? (
                <p className="time-slot-guide">가능한 시간을 확인하고 있습니다.</p>
              ) : availableTimes.length > 0 ? (
                <div className="time-slot-grid">
                  {availableTimes.map((time) => (
                    <button
                      key={time}
                      type="button"
                      className={`time-slot-button ${reservationTime === time ? 'active' : ''}`}
                      aria-pressed={reservationTime === time}
                      onClick={() => setReservationTime(time)}
                    >
                      {formatTimeLabel(time)}
                    </button>
                  ))}
                </div>
              ) : (
                <p className="empty-message">선택 가능한 시간이 없습니다.</p>
              )}
              {availableTimesError && <p className="error-message">{availableTimesError}</p>}
            </fieldset>
            {selectedService && (
              <div className="booking-selection-summary">
                <strong>{selectedService.name}</strong>
                <span>{selectedService.durationMinutes}분 · {selectedService.price.toLocaleString()}원</span>
                <span>{reservationDate || '날짜 미선택'} · {reservationTime ? formatTimeLabel(reservationTime) : '시간 미선택'}</span>
              </div>
            )}
          </div>
          <div className="paper-panel accent-panel torn-paper-edge">
            <span className="step-number display-text">02</span>
            <h2 className="display-text">REQUEST</h2>
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
            <button
              type="submit"
              className="primary-button"
              disabled={createReservationLoading || !reservationTime}
            >
              {createReservationLoading ? '예약 확인 중...' : '예약 신청하기'}
            </button>
          </div>
        </form>
      </section>
    </AppLayout>
  );
}
