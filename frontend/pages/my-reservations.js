import { useEffect, useState } from 'react';
import {
  formatReservationTime,
  includeCurrentReservationSlot,
} from '../features/reservation/reservationRules';
import {
  authApi,
  reservationApi,
  serviceApi,
} from '../features/shared/api/salonApi';

/** 로그인 고객의 예약 목록·프로필·예약 변경 기능을 필요한 도구로 조립합니다. */
export default function MyReservations() {
  const [items, setItems] = useState([]);
  const [user, setUser] = useState(null);
  const [services, setServices] = useState([]);
  const [editing, setEditing] = useState(null);
  const [showProfile, setShowProfile] = useState(false);
  const [slots, setSlots] = useState([]);
  const [message, setMessage] = useState('');

  // 로그인 확인 뒤 고객 소유권이 적용된 예약 API만 호출합니다.
  const load = async () => {
    const [loginUser, serviceItems] = await Promise.all([
      authApi.currentUser(),
      serviceApi.list().catch(() => []),
    ]);
    setServices(serviceItems);

    if (!loginUser) {
      setUser(null);
      setItems([]);
      setMessage('로그인 후 내 예약을 확인해 주세요.');
      return;
    }

    setUser(loginUser);
    if (loginUser.profileComplete === false
        || new URLSearchParams(window.location.search).get('profile') === '1') {
      setShowProfile(true);
      setMessage('예약 전에 이메일과 연락처를 확인해 주세요.');
    }

    try {
      setItems(await reservationApi.listMine());
      if (loginUser.profileComplete !== false) setMessage('');
    } catch (error) {
      setItems([]);
      setMessage(error.message);
    }
  };

  useEffect(() => {
    load().catch(() => setMessage('로그인 후 내 예약을 확인해 주세요.'));
  }, []);

  const saveProfile = async (event) => {
    event.preventDefault();
    const profile = Object.fromEntries(new FormData(event.currentTarget));
    try {
      setUser(await authApi.updateProfile(profile));
      setShowProfile(false);
      setMessage('고객 정보를 수정했습니다.');
    } catch (error) {
      setMessage(error.message || '고객 정보 수정에 실패했습니다.');
    }
  };

  const loadSlots = async (serviceId, date, currentSlot = '') => {
    if (!serviceId || !date) {
      setSlots([]);
      return;
    }
    try {
      const result = await reservationApi.availableSlots(serviceId, date);
      setSlots(includeCurrentReservationSlot(result.availableSlots, currentSlot, date));
    } catch {
      setSlots(includeCurrentReservationSlot([], currentSlot, date));
    }
  };

  const openEdit = (item) => {
    const date = item.reservationDateTime.slice(0, 10);
    const time = item.reservationDateTime.slice(0, 16);
    setEditing({ ...item, originalServiceId: item.serviceId, date, time });
    loadSlots(item.serviceId, date, time);
  };

  const changeEditService = (serviceId) => {
    const currentSlot = String(serviceId) === String(editing.originalServiceId) ? editing.time : '';
    setEditing({ ...editing, serviceId });
    loadSlots(serviceId, editing.date, currentSlot);
  };

  const changeEditDate = (date) => {
    const currentSlot = date === editing.time.slice(0, 10) ? editing.time : '';
    setEditing({ ...editing, date });
    loadSlots(editing.serviceId, date, currentSlot);
  };

  const saveReservation = async (event) => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.currentTarget));
    try {
      await reservationApi.update(editing.id, {
        serviceId: Number(data.serviceId),
        reservationDateTime: data.reservationDateTime,
        memo: data.memo || '',
        noShowPolicyAgreed: true,
      });
      setEditing(null);
      setMessage('예약을 수정했습니다.');
      await load();
    } catch (error) {
      setMessage(error.message);
    }
  };

  const today = new Date().toLocaleDateString('en-CA');
  return (
    <main className="simple-page">
      <a href="/">← 홈으로</a>
      <h1>나의 예약</h1>
      {message && <p role="alert">{message}</p>}

      <div className="section-head">
        <h2>진행 예약</h2>
        {user && <button onClick={() => setShowProfile(true)}>고객 정보 수정</button>}
      </div>
      <section>
        {items.map((item) => (
          <article className="reservation-row" key={item.id}>
            <b>{item.serviceName}</b>
            <span>{new Date(item.reservationDateTime).toLocaleString('ko-KR')}</span>
            <em className={`status ${item.status}`}>{item.status}</em>
            <button onClick={() => openEdit(item)}>예약 수정</button>
          </article>
        ))}
        {user && !items.length && <p>진행 중인 예약이 없습니다.</p>}
      </section>

      {showProfile && user && (
        <div className="dialog-backdrop">
          <form className="simple-form dialog" onSubmit={saveProfile}>
            <button type="button" className="close" onClick={() => setShowProfile(false)}>×</button>
            <h2>고객 정보 수정</h2>
            <input name="name" defaultValue={user.name} placeholder="이름" required />
            <input name="email" type="email" defaultValue={user.email} placeholder="이메일" required />
            <input name="phone" defaultValue={user.phone} placeholder="연락처" required />
            <button>정보 저장</button>
          </form>
        </div>
      )}

      {editing && (
        <div className="dialog-backdrop">
          <form className="simple-form dialog" onSubmit={saveReservation}>
            <button type="button" className="close" onClick={() => setEditing(null)}>×</button>
            <h2>예약 수정</h2>
            <label>
              시술 메뉴
              <select
                name="serviceId"
                value={editing.serviceId}
                onChange={(event) => changeEditService(event.target.value)}
              >
                {services.map((service) => (
                  <option value={service.id} key={service.id}>{service.name}</option>
                ))}
              </select>
            </label>
            <label>
              예약 일자
              <input
                type="date"
                value={editing.date}
                min={today}
                onClick={(event) => event.currentTarget.showPicker?.()}
                onChange={(event) => changeEditDate(event.target.value)}
                required
              />
            </label>
            <label>
              예약 시간
              <select name="reservationDateTime" defaultValue={editing.time} required>
                {slots.map((slot) => (
                  <option key={slot} value={slot}>{formatReservationTime(slot)}</option>
                ))}
              </select>
            </label>
            <label>요청 사항<textarea name="memo" defaultValue={editing.memo} /></label>
            <button>변경 저장</button>
          </form>
        </div>
      )}
    </main>
  );
}
