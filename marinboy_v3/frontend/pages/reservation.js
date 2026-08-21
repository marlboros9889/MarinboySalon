import { useEffect, useState } from 'react';
import { jwtFetch as api } from '../features/shared/api/jwtApi';

/** 고객이 시술·날짜·시간을 순서대로 선택해 예약하는 화면입니다. */
export default function Reservation() {
  const [services, setServices] = useState([]);
  const [user, setUser] = useState(null);
  const [slots, setSlots] = useState([]);
  const [done, setDone] = useState(false);
  const [message, setMessage] = useState('');
  const [serviceId, setServiceId] = useState('');
  const [date, setDate] = useState('');

  useEffect(() => {
    const selectedId = new URLSearchParams(window.location.search).get('serviceId');

    //1. 메뉴와 로그인 정보를 함께 불러와 예약 폼의 초기 상태를 맞춥니다.
    Promise.all([api('/api/services'), api('/api/auth/me')])
      .then(async ([menuResponse, userResponse]) => {
        if (!menuResponse.ok) throw new Error('서비스 메뉴 조회 실패');

        const serviceItems = await menuResponse.json();
        setServices(serviceItems);
        setServiceId(selectedId || String(serviceItems[0]?.id || ''));
        setUser(userResponse.status === 200 ? await userResponse.json() : null);
      })
      .catch(() => setMessage('시술 메뉴를 불러오지 못했습니다. 새로고침 후 다시 시도해 주세요.'));
  }, []);

  //2. 선택한 시술 ID와 날짜를 인자로 전달해 이전 state를 읽는 시간 선택 오류를 막습니다.
  const loadSlots = async (nextServiceId, nextDate) => {
    setSlots([]);
    if (!nextServiceId || !nextDate) return;

    try {
      const response = await api(`/api/services/${nextServiceId}/available-slots?date=${nextDate}`);
      if (!response.ok) throw new Error('예약 가능 시간 조회 실패');

      const result = await response.json();
      setSlots(result.availableSlots || []);
      if (!result.availableSlots?.length) {
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

    const formData = Object.fromEntries(new FormData(event.currentTarget));
    const response = await api('/api/reservations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        serviceId: Number(serviceId),
        reservationDateTime: formData.reservationDateTime,
        memo: formData.memo || '',
        customerName: user.name,
        customerEmail: user.email,
        customerPhone: user.phone,
        noShowPolicyAgreed: true,
      }),
    });

    if (!response.ok) {
      setMessage('예약에 실패했습니다. 선택한 시간을 다시 확인해 주세요.');
      return;
    }
    setDone(true);
  };

  const today = new Date().toLocaleDateString('en-CA');

  return (
    <main className="simple-page">
      <a href="/">← 홈으로</a>
      <h1>시술 예약</h1>
      <p>원하는 시술과 날짜, 시간을 선택해 주세요.</p>
      {message && <p role="alert">{message}</p>}

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
          예약 날짜
          {/* 날짜 입력창은 브라우저별로 input 또는 change 이벤트를 발생시키므로 둘 다 처리합니다. */}
          <input type="date" min={today} value={date} onInput={handleDateChange} onChange={handleDateChange} required />
        </label>
        <label>
          예약 시간
          <select name="reservationDateTime" required disabled={!slots.length}>
            <option value="">시간 선택</option>
            {slots.map((slot) => <option key={slot} value={slot}>{new Date(slot).toLocaleString('ko-KR')}</option>)}
          </select>
        </label>
        <label>
          요청 사항
          <textarea name="memo" placeholder="원하는 스타일 또는 참고 사항" />
        </label>
        <button disabled={!user || !slots.length}>예약 완료</button>
        {!user && <small>로그인 후 예약할 수 있습니다.</small>}
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
