import { useEffect, useState } from 'react';
import { ProfileForm } from '../features/auth/components/ProfileForm';
import { reservationApi } from '../features/reservation/reservationApi';
import { useReservationSlots } from '../features/reservation/useReservationSlots';
import {
  canSubmitReservation,
  formatReservationTime,
  getMaximumBookingDate,
} from '../features/reservation/reservationRules';

/** 고객이 시술·날짜·시간을 순서대로 선택해 예약하는 화면입니다. */
export default function Reservation() {
  const [services, setServices] = useState([]);
  const [user, setUser] = useState(null);
  const { slots, clearSlots, loadSlots: loadAvailableSlots } = useReservationSlots();
  const [done, setDone] = useState(false);
  const [message, setMessage] = useState('');
  const [serviceId, setServiceId] = useState('');
  const [date, setDate] = useState('');
  const [reservationDateTime, setReservationDateTime] = useState('');
  const [showProfileForm, setShowProfileForm] = useState(false);
  const [noShowPolicyAgreed, setNoShowPolicyAgreed] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const selectedId = new URLSearchParams(window.location.search).get('serviceId');

    //1. 메뉴와 로그인 정보를 함께 불러와 예약 폼의 초기 상태를 맞춥니다.
    reservationApi.bookingPage()
      .then(({ services: serviceItems, user: loginUser }) => {
        setServices(serviceItems);
        setServiceId(selectedId || String(serviceItems[0]?.id || ''));
        setUser(loginUser);
        if (loginUser?.profileComplete === false) {
          setShowProfileForm(true);
          setMessage('예약을 계속하려면 이메일과 연락처를 입력해 주세요.');
        }
      })
      .catch(() => setMessage('시술 메뉴를 불러오지 못했습니다. 새로고침 후 다시 시도해 주세요.'));
  }, []);

  //2. 선택한 시술 ID와 날짜를 인자로 전달해 이전 state를 읽는 시간 선택 오류를 막습니다.
  const loadSlots = async (nextServiceId, nextDate) => {
    clearSlots();
    setReservationDateTime('');
    if (!nextServiceId || !nextDate) return;

    try {
      const availableSlots = await loadAvailableSlots(nextServiceId, nextDate);
      if (!availableSlots.length) {
        setMessage('선택한 날짜에는 예약 가능한 시간이 없습니다. 다른 날짜를 선택해 주세요.');
      } else {
        setMessage('');
      }
    } catch {
      setMessage('예약 가능 시간을 불러오지 못했습니다. 서버 연결을 확인해 주세요.');
    }
  };

  const handleServiceChange = (event) => {
    const nextServiceId = event.target.value;
    setServiceId(nextServiceId);
    loadSlots(nextServiceId, date);
  };

  const handleDateChange = (event) => {
    const nextDate = event.target.value;
    setDate(nextDate);
    loadSlots(serviceId, nextDate);
  };

  //3. 로그인 고객의 연락처 정보를 사용해 중복 검증을 통과한 예약만 서버에 저장합니다.
  const submit = async (event) => {
    event.preventDefault();
    if (!user) {
      setMessage('예약하려면 먼저 로그인해 주세요.');
      return;
    }

    if (user.profileComplete === false) {
      setShowProfileForm(true);
      setMessage('예약을 계속하려면 이메일과 연락처를 입력해 주세요.');
      return;
    }
    setSubmitting(true);
    setMessage('');
    try {
      await reservationApi.create({
        serviceId: Number(serviceId),
        reservationDateTime,
        memo: Object.fromEntries(new FormData(event.currentTarget)).memo || '',
        noShowPolicyAgreed,
      });
      setDone(true);
    } catch (error) {
      console.log('예약 저장 오류:', error.message);
      const fallback = error.status === 401
        ? '로그인이 만료되었습니다. 홈에서 다시 로그인해 주세요.'
        : '예약 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.';
      setMessage(error.message || fallback);
    } finally {
      setSubmitting(false);
    }
  };

  const today = new Date().toLocaleDateString('en-CA');
  const maximumDate = getMaximumBookingDate(today);
  const reservationEnabled = canSubmitReservation({
    user,
    serviceId,
    date,
    reservationDateTime,
    noShowPolicyAgreed,
    submitting,
  });

  return (
    <main className="simple-page reservation-page">
      <a className="reservation-back-link" href="/">← SALON HOME</a>
      <header className="reservation-intro">
        <p className="reservation-eyebrow">PRIVATE APPOINTMENT</p>
        <h1>시술 예약</h1>
        <p>원하는 시술과 날짜, 시간을 순서대로 선택해 주세요.</p>
      </header>
      <ol className="reservation-steps" aria-label="예약 진행 순서">
        <li className={serviceId ? 'is-ready' : ''}><b>01</b><span>시술</span></li>
        <li className={date ? 'is-ready' : ''}><b>02</b><span>날짜</span></li>
        <li className={reservationDateTime ? 'is-ready' : ''}><b>03</b><span>시간</span></li>
        <li className={noShowPolicyAgreed ? 'is-ready' : ''}><b>04</b><span>확인</span></li>
      </ol>
      {message && <p role="alert">{message}</p>}

      {showProfileForm && user && (
        <ProfileForm
          user={user}
          title="예약 연락처 입력"
          description="예약 안내에 사용할 정보를 한 번만 입력하면 다음 예약에도 사용됩니다."
          submitLabel="연락처 저장 후 예약 계속"
          successMessage="고객 정보를 저장했습니다. 예약을 계속해 주세요."
          failureMessage="고객 정보를 저장하지 못했습니다. 입력값을 확인해 주세요."
          onSaved={(updatedUser) => { setUser(updatedUser); setShowProfileForm(false); }}
          onMessage={setMessage}
        />
      )}

      <form className="simple-form" onSubmit={submit}>
        <label>
          시술 메뉴
          <select value={serviceId} onChange={handleServiceChange} required>
            {services.map((service) => (
              <option key={service.id} value={service.id}>
                {service.name} · {Number(service.price).toLocaleString()}원
              </option>
            ))}
          </select>
        </label>
        <label>
          예약 일자
          {/* 입력 영역을 누르면 브라우저 달력을 직접 열어 일자를 별도로 선택합니다. */}
          <input
            type="date"
            min={today}
            max={maximumDate}
            value={date}
            onClick={(event) => event.currentTarget.showPicker?.()}
            onChange={handleDateChange}
            required
          />
        </label>
        <label>
          예약 시간
          <select
            name="reservationDateTime"
            value={reservationDateTime}
            onChange={(event) => setReservationDateTime(event.target.value)}
            required
            disabled={!slots.length}
          >
            <option value="">시간 선택</option>
            {slots.map((slot) => <option key={slot} value={slot}>{formatReservationTime(slot)}</option>)}
          </select>
        </label>
        <label>
          요청 사항
          <textarea name="memo" placeholder="원하는 스타일 또는 참고 사항" />
        </label>
        <label className={`salon-check-field${noShowPolicyAgreed ? ' is-agreed' : ''}`}>
          <input
            className="salon-checkbox"
            type="checkbox"
            checked={noShowPolicyAgreed}
            onChange={(event) => setNoShowPolicyAgreed(event.target.checked)}
          />
          <span className="salon-check-copy">
            <strong>필수 동의</strong>
            <small>노쇼 및 당일 취소 제한 안내를 확인했습니다.</small>
          </span>
          <span className="salon-check-state">{noShowPolicyAgreed ? '동의 완료' : '미동의'}</span>
        </label>
        <button disabled={!reservationEnabled}>{submitting ? '예약 처리 중...' : '예약 완료'}</button>
        {!user && (
          <small>
            예약하려면 먼저 로그인해 주세요.{' '}
            <a href={`/?returnTo=${encodeURIComponent(`/reservation${serviceId ? `?serviceId=${serviceId}` : ''}`)}#login`}>
              로그인 화면으로 이동
            </a>
          </small>
        )}
        {user?.profileComplete === false && <small>위 연락처를 저장하면 예약 버튼이 활성화됩니다.</small>}
      </form>

      {done && (
        <div className="dialog-backdrop">
          <section className="dialog notification-modal">
            <h2>예약이 완료되었습니다</h2>
            <p>결제는 시술 당일 매장에서 진행해 주세요.</p>
            <button onClick={() => { setDone(false); window.location.href = '/my-reservations'; }}>나의 예약 확인</button>
          </section>
        </div>
      )}
    </main>
  );
}
