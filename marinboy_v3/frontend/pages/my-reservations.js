import { useEffect, useState } from 'react';
import { jwtFetch as api } from '../features/shared/api/jwtApi';

export default function MyReservations() {
  const [items, setItems] = useState([]); const [user, setUser] = useState(null); const [services, setServices] = useState([]);
  const [editing, setEditing] = useState(null); const [showProfile, setShowProfile] = useState(false); const [slots, setSlots] = useState([]); const [message, setMessage] = useState('');

  // 로그인 확인을 먼저 하여 비회원에게 빈 예약 목록이 정상 결과처럼 보이지 않게 합니다.
  const load = async () => {
    const meResponse = await api('/api/auth/me');
    const menuResponse = await api('/api/services');
    setServices(menuResponse.ok ? await menuResponse.json() : []);

    if (meResponse.status !== 200) {
      setUser(null);
      setItems([]);
      setMessage('로그인 후 내 예약을 확인해 주세요.');
      return;
    }

    const loginUser = await meResponse.json();
    const reservationResponse = await api('/api/customers/my-reservations');
    setUser(loginUser);
    if (loginUser.profileComplete === false || new URLSearchParams(window.location.search).get('profile') === '1') {
      setShowProfile(true);
      setMessage('예약 전에 이메일과 연락처를 확인해 주세요.');
    }
    setItems(reservationResponse.ok ? await reservationResponse.json() : []);
    setMessage(reservationResponse.ok ? '' : '예약 목록을 불러오지 못했습니다.');
  };
  useEffect(() => { load().catch(() => setMessage('로그인 후 내 예약을 확인해 주세요.')); }, []);
  const saveProfile = async (event) => { event.preventDefault(); const response = await api('/api/customers/me', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(Object.fromEntries(new FormData(event.currentTarget))) }); if (!response.ok) return setMessage('고객 정보 수정에 실패했습니다.'); setUser(await response.json()); setShowProfile(false); setMessage('고객 정보를 수정했습니다.'); };
  const loadSlots = async (serviceId, date) => { if (!serviceId || !date) return setSlots([]); const response = await api(`/api/services/${serviceId}/available-slots?date=${date}`); setSlots(response.ok ? (await response.json()).availableSlots || [] : []); };
  const openEdit = (item) => { setEditing({ ...item, date: item.reservationDateTime.slice(0, 10), time: item.reservationDateTime.slice(0, 16) }); loadSlots(item.serviceId, item.reservationDateTime.slice(0, 10)); };
  const saveReservation = async (event) => { event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget)); const response = await api(`/api/customers/my-reservations/${editing.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ serviceId: Number(data.serviceId), reservationDateTime: data.reservationDateTime, memo: data.memo || '', noShowPolicyAgreed: true }) }); if (!response.ok) return setMessage('예약 수정에 실패했습니다. 가능한 시간과 상태를 확인해 주세요.'); setEditing(null); setMessage('예약을 수정했습니다.'); load(); };
  return <main className="simple-page"><a href="/">← 홈으로</a><h1>나의 예약</h1>{message && <p role="alert">{message}</p>}<div className="section-head"><h2>진행 예약</h2>{user && <button onClick={() => setShowProfile(true)}>고객 정보 수정</button>}</div><section>{items.map((item) => <article className="reservation-row" key={item.id}><b>{item.serviceName}</b><span>{new Date(item.reservationDateTime).toLocaleString('ko-KR')}</span><em className={`status ${item.status}`}>{item.status}</em><button onClick={() => openEdit(item)}>예약 수정</button></article>)}{user && !items.length && <p>진행 중인 예약이 없습니다.</p>}</section>{showProfile && user && <div className="dialog-backdrop"><form className="simple-form dialog" onSubmit={saveProfile}><button type="button" className="close" onClick={() => setShowProfile(false)}>×</button><h2>고객 정보 수정</h2><input name="name" defaultValue={user.name} placeholder="이름" required /><input name="email" type="email" defaultValue={user.email} placeholder="이메일" required /><input name="phone" defaultValue={user.phone} placeholder="연락처" required /><button>정보 저장</button></form></div>}{editing && <div className="dialog-backdrop"><form className="simple-form dialog" onSubmit={saveReservation}><button type="button" className="close" onClick={() => setEditing(null)}>×</button><h2>예약 수정</h2><label>시술 메뉴<select name="serviceId" defaultValue={editing.serviceId} onChange={(e) => { setEditing({ ...editing, serviceId: e.target.value }); loadSlots(e.target.value, editing.date); }}>{services.map((service) => <option value={service.id} key={service.id}>{service.name}</option>)}</select></label><label>예약 날짜<input type="date" defaultValue={editing.date} min={new Date().toISOString().slice(0, 10)} onChange={(e) => { setEditing({ ...editing, date: e.target.value }); loadSlots(editing.serviceId, e.target.value); }} required /></label><label>예약 시간<select name="reservationDateTime" defaultValue={editing.time} required>{slots.map((slot) => <option key={slot} value={slot}>{new Date(slot).toLocaleString('ko-KR')}</option>)}</select></label><label>요청 사항<textarea name="memo" defaultValue={editing.memo} /></label><button>변경 저장</button></form></div>}</main>;
}
