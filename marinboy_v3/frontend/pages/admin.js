import { useEffect, useState } from 'react';
import { isAdminUser } from '../features/admin/adminRules';
import { jwtFetch as api, subscribeToNotifications } from '../features/shared/api/jwtApi';

const labels = {
  REQUESTED: '승인 대기',
  CONFIRMED: '승인 완료',
  REJECTED: '거절',
  CANCELED: '취소',
  COMPLETED: '시술 완료',
};

/** API가 반환한 구체적인 실패 원인을 관리자에게 표시합니다. */
async function readApiError(response, fallbackMessage) {
  try {
    const result = await response.json();
    return result.message || fallbackMessage;
  } catch {
    return fallbackMessage;
  }
}

export default function Admin() {
  const [items, setItems] = useState([]);
  const [services, setServices] = useState([]);
  const [selected, setSelected] = useState(null);
  const [notice, setNotice] = useState(null);
  const [message, setMessage] = useState('');
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [user, setUser] = useState(null);
  const [accessStatus, setAccessStatus] = useState('checking');
  const [calendarUrl, setCalendarUrl] = useState('');
  const [showProfile, setShowProfile] = useState(false);

  //1. ADMIN 확인 뒤에만 예약·메뉴·캘린더 운영 데이터를 요청합니다.
  const load = async (targetPage = page) => {
    const [reservationsResponse, menuResponse, calendarResponse] = await Promise.all([
      api(`/api/admin/reservations?page=${targetPage}&size=5`),
      api('/api/admin/services'),
      api('/api/admin/calendar'),
    ]);
    if (!reservationsResponse.ok || !menuResponse.ok || !calendarResponse.ok) {
      throw new Error('관리자 데이터를 불러오지 못했습니다.');
    }

    const reservationData = await reservationsResponse.json();
    const calendarData = await calendarResponse.json();
    setItems(reservationData.items);
    setTotal(reservationData.total);
    setServices(await menuResponse.json());
    setCalendarUrl(calendarData.configured ? calendarData.embedUrl : '');
  };

  useEffect(() => {
    let active = true;
    let unsubscribe = () => {};

    const startAdminScreen = async () => {
      const meResponse = await api('/api/auth/me');
      const loginUser = meResponse.ok ? await meResponse.json() : null;
      if (!active) return;

      if (!isAdminUser(loginUser)) {
        setAccessStatus('denied');
        setMessage('관리자 계정으로 로그인한 뒤 이용해 주세요.');
        return;
      }

      setUser(loginUser);
      setAccessStatus('allowed');
      await load(0);
      if (!active) return;

      // ADMIN 확인 후에만 Bearer 토큰으로 실시간 예약 알림을 구독합니다.
      unsubscribe = subscribeToNotifications((data) => {
        setNotice(data);
        load().catch(() => setMessage('최신 예약 정보를 불러오지 못했습니다.'));
      });
    };

    startAdminScreen().catch(() => {
      if (active) {
        setAccessStatus('denied');
        setMessage('관리자 화면을 불러오지 못했습니다. 다시 로그인해 주세요.');
      }
    });
    return () => {
      active = false;
      unsubscribe();
    };
  }, []);

  const changeStatus = async (id, status) => {
    const response = await api(`/api/admin/reservations/${id}/status?status=${status}`, { method: 'PATCH' });
    if (!response.ok) {
      setMessage(await readApiError(response, '상태를 변경할 수 없습니다.'));
      return;
    }
    await load(page);
  };

  const saveMenu = async (event) => {
    event.preventDefault();
    setMessage('');
    const form = new FormData(event.currentTarget);
    const id = selected?.id;

    try {
      //2. 시술 메뉴 등록·수정  POST/PATCH: /api/admin/services
      const response = await api(id ? `/api/admin/services/${id}` : '/api/admin/services', {
        method: id ? 'PATCH' : 'POST',
        body: form,
      });
      if (!response.ok) {
        setMessage(await readApiError(response, '메뉴 저장에 실패했습니다.'));
        return;
      }

      await load(page);
      setSelected(null);
      setMessage(id ? '시술 메뉴를 수정했습니다.' : '시술 메뉴를 추가했습니다.');
    } catch {
      setMessage('서버 연결을 확인한 뒤 다시 저장해 주세요.');
    }
  };

  const saveProfile = async (event) => {
    event.preventDefault();
    const response = await api('/api/customers/me', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(Object.fromEntries(new FormData(event.currentTarget))),
    });
    if (!response.ok) {
      setMessage(await readApiError(response, '관리자 정보 수정에 실패했습니다.'));
      return;
    }
    setUser(await response.json());
    setShowProfile(false);
    setMessage('관리자 정보를 수정했습니다.');
  };

  const totalPages = Math.max(1, Math.ceil(total / 5));
  const goPage = async (nextPage) => {
    setPage(nextPage);
    await load(nextPage);
  };

  if (accessStatus !== 'allowed') {
    return (
      <main className="simple-page admin-page">
        <a href="/">← 고객 화면</a>
        <h1>예약 운영 대시보드</h1>
        <p role="alert">{message || '관리자 권한을 확인하고 있습니다.'}</p>
      </main>
    );
  }

  return (
    <main className="simple-page admin-page">
      <a href="/">← 고객 화면</a>
      <div className="section-head">
        <h1>예약 운영 대시보드</h1>
        <button onClick={() => setShowProfile(true)}>관리자 정보 수정</button>
      </div>
      <p>예약 현황은 과거 이력을 포함해 페이지당 5건씩 확인합니다.</p>
      {message && <p role="alert">{message}</p>}

      <section id="reservation-status">
        <h2>전체 예약 현황</h2>
        {items.map((item) => (
          <article className="reservation-row" key={item.id}>
            <b>{item.customerName} · {item.serviceName}</b>
            <span>{new Date(item.reservationDateTime).toLocaleString('ko-KR')}</span>
            <em className={`status ${item.status}`}>{labels[item.status] || item.status}</em>
            {item.status === 'REQUESTED' && <button onClick={() => changeStatus(item.id, 'CONFIRMED')}>승인</button>}
            {item.status === 'CONFIRMED' && <button onClick={() => changeStatus(item.id, 'COMPLETED')}>시술 완료</button>}
          </article>
        ))}
        <div className="pagination">
          <button disabled={page === 0} onClick={() => goPage(page - 1)}>이전</button>
          <span>{page + 1} / {totalPages}</span>
          <button disabled={page + 1 >= totalPages} onClick={() => goPage(page + 1)}>다음</button>
        </div>
      </section>

      <section id="google-calendar">
        <h2>Google Calendar 예약 일정</h2>
        <p>고객 예약이 완료되면 이 캘린더에 일정과 즉시 알림이 추가됩니다.</p>
        {calendarUrl
          ? <iframe className="google-calendar-frame" src={calendarUrl} title="Marinboy 예약 캘린더" />
          : <p>Google Calendar 백엔드 설정을 확인해 주세요.</p>}
      </section>

      <section id="service-management">
        <div className="section-head">
          <h2>시술 메뉴 관리</h2>
          <button onClick={() => setSelected({})}>메뉴 추가</button>
        </div>
        <div className="menu-admin-grid">
          {services.map((item) => (
            <article key={item.id}>
              <img src={item.imageUrl || '/favicon.svg'} alt="" />
              <b>{item.name}</b>
              <span>{Number(item.price).toLocaleString()}원</span>
              <button onClick={() => setSelected(item)}>수정</button>
            </article>
          ))}
        </div>
      </section>

      {showProfile && user && (
        <div className="dialog-backdrop">
          <form className="simple-form dialog" onSubmit={saveProfile}>
            <button type="button" className="close" onClick={() => setShowProfile(false)}>×</button>
            <h2>관리자 정보 수정</h2>
            <input name="name" defaultValue={user.name} placeholder="이름" required />
            <input name="email" type="email" defaultValue={user.email} placeholder="이메일" required />
            <input name="phone" defaultValue={user.phone} placeholder="연락처" required />
            <button>정보 저장</button>
          </form>
        </div>
      )}

      {selected && (
        <div className="dialog-backdrop">
          <form className="simple-form dialog" onSubmit={saveMenu}>
            <button type="button" className="close" onClick={() => setSelected(null)}>×</button>
            <h2>{selected.id ? '시술 메뉴 수정' : '시술 메뉴 추가'}</h2>
            <input name="name" defaultValue={selected.name} placeholder="시술명" required />
            <input name="category" defaultValue={selected.category} placeholder="카테고리" required />
            <input name="durationMinutes" type="number" defaultValue={selected.durationMinutes} placeholder="소요 시간(분)" required />
            <input name="price" type="number" defaultValue={selected.price} placeholder="가격" required />
            <textarea name="description" defaultValue={selected.description} placeholder="설명" />
            <label>대표 이미지 <input name="image" type="file" accept="image/*" /></label>
            <label>상세 이미지 <input name="galleryImages" type="file" accept="image/*" multiple /></label>
            <button>저장</button>
          </form>
        </div>
      )}

      {notice && (
        <div className="dialog-backdrop">
          <section className="dialog notification-modal">
            <button className="close" onClick={() => setNotice(null)}>×</button>
            <h2>새 예약 알림</h2>
            <p>{notice.message}</p>
            <button onClick={() => setNotice(null)}>확인</button>
          </section>
        </div>
      )}
    </main>
  );
}
