import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import api from '../../api/axios';

export default function AdminSchedule() {
  const [businessHours, setBusinessHours] = useState([]);
  const [holidays, setHolidays] = useState([]);
  const [holidayDate, setHolidayDate] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  const loadSchedule = async () => {
    try {
      const [hoursResponse, holidaysResponse] = await Promise.all([
        api.get('/api/admin/business-hours'),
        api.get('/api/admin/holidays'),
      ]);
      setBusinessHours(hoursResponse.data);
      setHolidays(holidaysResponse.data);
    } catch (requestError) {
      setError(requestError.response?.data?.message || '영업일 정보를 불러오지 못했습니다.');
    }
  };

  useEffect(() => {
    loadSchedule();
  }, []);

  const onHolidaySubmit = async (event) => {
    event.preventDefault();
    await api.post('/api/admin/holidays', { holidayDate, reason });
    setHolidayDate('');
    setReason('');
    await loadSchedule();
  };

  const onHolidayDelete = async (id) => {
    await api.delete(`/api/admin/holidays/${id}`);
    await loadSchedule();
  };

  return (
    <AppLayout>
      <section className="page-section container">
        <header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="serif-text">영업일 관리</h1></header>
        {error && <p className="error-message">{error}</p>}
        <div className="booking-layout">
          <div className="paper-panel">
            <h2 className="serif-text">주간 영업시간</h2>
            <ul className="schedule-list">
              {businessHours.map((item) => (
                <li key={item.id}><span>{item.dayOfWeek}요일</span><strong>{item.closed ? '휴무' : `${item.openTime} - ${item.closeTime}`}</strong></li>
              ))}
            </ul>
          </div>
          <div className="paper-panel accent-panel">
            <h2 className="serif-text">임시 휴무</h2>
            <form className="holiday-form" onSubmit={onHolidaySubmit}>
              <input type="date" value={holidayDate} onChange={(event) => setHolidayDate(event.target.value)} required />
              <input value={reason} onChange={(event) => setReason(event.target.value)} placeholder="휴무 사유" />
              <button type="submit" className="primary-button">등록</button>
            </form>
            <ul className="schedule-list">
              {holidays.map((item) => (
                <li key={item.id}><span>{item.holidayDate} · {item.reason}</span><button className="link-button" type="button" onClick={() => onHolidayDelete(item.id)}>삭제</button></li>
              ))}
            </ul>
          </div>
        </div>
      </section>
    </AppLayout>
  );
}
