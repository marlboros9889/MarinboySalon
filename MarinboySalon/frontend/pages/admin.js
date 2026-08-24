import { useEffect, useState } from 'react';
import { isAdminUser } from '../features/admin/adminRules';
import { adminApi } from '../features/admin/adminApi';
import { authApi } from '../features/auth/authApi';
import { ProfileForm } from '../features/auth/components/ProfileForm';

const labels = {
  REQUESTED: '승인 대기',
  CONFIRMED: '승인 완료',
  REJECTED: '거절',
  CANCELED: '취소',
  COMPLETED: '시술 완료',
};

const dayLabels = {
  1: '월요일',
  2: '화요일',
  3: '수요일',
  4: '목요일',
  5: '금요일',
  6: '토요일',
  7: '일요일',
};

export default function Admin() {
  const [items, setItems] = useState([]);
  const [services, setServices] = useState([]);
  const [selected, setSelected] = useState(null);
  const [message, setMessage] = useState('');
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [user, setUser] = useState(null);
  const [accessStatus, setAccessStatus] = useState('checking');
  const [calendarUrl, setCalendarUrl] = useState('');
  const [showProfile, setShowProfile] = useState(false);
  const [businessHours, setBusinessHours] = useState([]);
  const [holidays, setHolidays] = useState([]);

  //1. 첫 진입에는 대시보드 전체를 읽고 이후 변경은 영향받은 영역만 갱신합니다.
  const loadDashboard = async (targetPage = page) => {
    const { reservationData, services: menuItems, calendar, businessHours: hours, holidays: holidayItems } =
      await adminApi.dashboard(targetPage);
    setItems(reservationData.items);
    setTotal(reservationData.total);
    setServices(menuItems);
    setCalendarUrl(calendar.configured ? calendar.embedUrl : '');
    setBusinessHours(hours);
    setHolidays(holidayItems);
  };

  const loadReservations = async (targetPage = page) => {
    const reservationData = await adminApi.reservations(targetPage);
    setItems(reservationData.items);
    setTotal(reservationData.total);
  };

  const loadServices = async () => {
    setServices(await adminApi.services());
  };

  const loadBusinessHours = async () => {
    setBusinessHours(await adminApi.businessHours());
  };

  const loadHolidays = async () => {
    setHolidays(await adminApi.holidays());
  };

  useEffect(() => {
    let active = true;
    const startAdminScreen = async () => {
      const loginUser = await authApi.currentUser();
      if (!active) return;

      if (!isAdminUser(loginUser)) {
        setAccessStatus('denied');
        setMessage('관리자 계정으로 로그인한 뒤 이용해 주세요.');
        return;
      }

      setUser(loginUser);
      setAccessStatus('allowed');
      await loadDashboard(0);
    };

    startAdminScreen().catch(() => {
      if (active) {
        setAccessStatus('denied');
        setMessage('관리자 화면을 불러오지 못했습니다. 다시 로그인해 주세요.');
      }
    });
    return () => {
      active = false;
    };
  }, []);

  const changeStatus = async (id, status) => {
    try {
      await adminApi.changeReservationStatus(id, status);
      await loadReservations(page);
    } catch (error) {
      setMessage(error.message);
    }
  };

  const saveMenu = async (event) => {
    event.preventDefault();
    setMessage('');
    const form = new FormData(event.currentTarget);
    const id = selected?.id;

    try {
      //2. 시술 메뉴 등록·수정  POST/PATCH: /api/admin/services
      await adminApi.saveService(id, form);
      await loadServices();
      setSelected(null);
      setMessage(id ? '시술 메뉴를 수정했습니다.' : '시술 메뉴를 추가했습니다.');
    } catch (error) {
      setMessage(error.message || '서버 연결을 확인한 뒤 다시 저장해 주세요.');
    }
  };

  const deleteMenu = async (item) => {
    if (!window.confirm(`${item.name} 메뉴를 삭제하시겠습니까?`)) return;
    try {
      await adminApi.deleteService(item.id);
      await loadServices();
      setMessage('시술 메뉴를 삭제했습니다.');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const saveBusinessHour = async (event, dayOfWeek) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await adminApi.saveBusinessHour(dayOfWeek, {
        open: form.get('open') === 'on',
        openTime: form.get('openTime'),
        closeTime: form.get('closeTime'),
      });
      await loadBusinessHours();
      setMessage(`${dayLabels[dayOfWeek]} 영업 규칙을 저장했습니다.`);
    } catch (error) {
      setMessage(error.message);
    }
  };

  const saveHoliday = async (event) => {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    try {
      await adminApi.saveHoliday({
        holidayDate: form.get('holidayDate'),
        reason: form.get('reason'),
      });
      formElement.reset();
      await loadHolidays();
      setMessage('특정 휴무일을 등록했습니다.');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const deleteHoliday = async (holidayDate) => {
    try {
      await adminApi.deleteHoliday(holidayDate);
      await loadHolidays();
      setMessage('특정 휴무일을 해제했습니다.');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const totalPages = Math.max(1, Math.ceil(total / 5));
  const goPage = async (nextPage) => {
    setPage(nextPage);
    await loadReservations(nextPage);
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

      <section id="business-hours">
        <h2>요일별 영업 규칙</h2>
        <p>요일마다 영업 여부와 예약 가능한 시작·종료 시간을 따로 설정합니다.</p>
        <div className="business-hour-grid">
          {businessHours.map((item) => (
            <form className="business-hour-row" key={item.dayOfWeek} onSubmit={(event) => saveBusinessHour(event, item.dayOfWeek)}>
              <strong>{dayLabels[item.dayOfWeek]}</strong>
              <label className="business-open-check">
                <input name="open" type="checkbox" defaultChecked={item.open} /> 영업일
              </label>
              <label>시작 <input name="openTime" type="time" step="1800" defaultValue={item.openTime} required /></label>
              <label>종료 <input name="closeTime" type="time" step="1800" defaultValue={item.closeTime} required /></label>
              <button>저장</button>
            </form>
          ))}
        </div>
      </section>

      <section id="holiday-management">
        <h2>특정 휴무일 관리</h2>
        <p>정상 영업일이어도 매장 사정으로 쉬는 날짜를 별도로 지정할 수 있습니다.</p>
        <form className="simple-form holiday-form" onSubmit={saveHoliday}>
          <label>휴무 일자 <input name="holidayDate" type="date" required /></label>
          <label>휴무 사유 <input name="reason" placeholder="예: 매장 정비" required /></label>
          <button>휴무일 등록</button>
        </form>
        <div className="holiday-list">
          {holidays.map((holiday) => (
            <article key={holiday.holidayDate}>
              <span><b>{holiday.holidayDate}</b> · {holiday.reason || '사유 없음'}</span>
              <button onClick={() => deleteHoliday(holiday.holidayDate)}>휴무 해제</button>
            </article>
          ))}
          {!holidays.length && <p>등록된 특정 휴무일이 없습니다.</p>}
        </div>
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
              <button onClick={() => deleteMenu(item)}>삭제</button>
            </article>
          ))}
        </div>
      </section>

      {showProfile && user && (
        <ProfileForm
          user={user}
          title="관리자 정보 수정"
          successMessage="관리자 정보를 수정했습니다."
          failureMessage="관리자 정보 수정에 실패했습니다."
          dialog
          onSaved={(updatedUser) => { setUser(updatedUser); setShowProfile(false); }}
          onCancel={() => setShowProfile(false)}
          onMessage={setMessage}
        />
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

    </main>
  );
}
