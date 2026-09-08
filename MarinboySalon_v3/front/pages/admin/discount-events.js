import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import AdminNavigation from '../../components/AdminNavigation';
import api from '../../api/axios';

const emptyForm = { name: '', startDate: '', endDate: '', discountRate: '' };

/** 관리자가 기간과 할인율을 직접 정하는 할인 이벤트 화면입니다. */
export default function AdminDiscountEvents() {
  const [events, setEvents] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');

  const loadEvents = async () => {
    try {
      const response = await api.get('/api/admin/discount-events');
      setEvents(response.data);
      setError('');
    } catch (requestError) {
      setError(requestError.response?.data?.message || '할인 이벤트를 불러오지 못했습니다.');
    }
  };

  useEffect(() => { loadEvents(); }, []);

  const submit = async (event) => {
    event.preventDefault();
    const discountRate = Number(form.discountRate);
    if (discountRate <= 0 || discountRate > 100) {
      setError('할인율은 0보다 크고 100 이하여야 합니다.');
      return;
    }
    try {
      const requestData = { ...form, discountRate };
      if (editingId) await api.put(`/api/admin/discount-events/${editingId}`, requestData);
      else await api.post('/api/admin/discount-events', requestData);
      setForm(emptyForm);
      setEditingId(null);
      await loadEvents();
    } catch (requestError) {
      setError(requestError.response?.data?.message || '할인 이벤트 저장에 실패했습니다.');
    }
  };

  const edit = (item) => {
    setEditingId(item.id);
    setForm({ name: item.name, startDate: item.startDate, endDate: item.endDate, discountRate: String(item.discountRate) });
  };

  const remove = async (item) => {
    if (!window.confirm(`${item.name} 이벤트를 삭제할까요?`)) return;
    try {
      await api.delete(`/api/admin/discount-events/${item.id}`);
      if (editingId === item.id) { setEditingId(null); setForm(emptyForm); }
      await loadEvents();
    } catch (requestError) {
      setError(requestError.response?.data?.message || '할인 이벤트 삭제에 실패했습니다.');
    }
  };

  return (
    <AppLayout><section className="page-section container">
      <header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="heading-text">할인 이벤트 관리</h1></header>
      <AdminNavigation />
      <form className="admin-inline-form" onSubmit={submit}>
        <label className="admin-field">이벤트명<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label>
        <label className="admin-field">시작일<input required type="date" value={form.startDate} onChange={(event) => setForm({ ...form, startDate: event.target.value })} /></label>
        <label className="admin-field">종료일<input required type="date" value={form.endDate} onChange={(event) => setForm({ ...form, endDate: event.target.value })} /></label>
        <label className="admin-field">할인율(%)<input required type="number" min="0.01" max="100" step="0.01" value={form.discountRate} onChange={(event) => setForm({ ...form, discountRate: event.target.value })} /></label>
        <div className="admin-form-actions"><button className="primary-button" type="submit">{editingId ? '이벤트 수정 저장' : '이벤트 등록'}</button>{editingId && <button className="secondary-button" type="button" onClick={() => { setEditingId(null); setForm(emptyForm); }}>취소</button>}</div>
      </form>
      {error && <p className="error-message">{error}</p>}
      <div className="table-responsive paper-table-wrap"><table className="table"><thead><tr><th>이벤트</th><th>적용 기간</th><th>할인율</th><th>관리</th></tr></thead>
        <tbody>{events.map((item) => <tr key={item.id}><td>{item.name}</td><td>{item.startDate} ~ {item.endDate}</td><td>{item.discountRate}%</td><td><button className="secondary-button" type="button" onClick={() => edit(item)}>수정</button>{' '}<button className="danger-button" type="button" onClick={() => remove(item)}>삭제</button></td></tr>)}</tbody>
      </table></div>
    </section></AppLayout>
  );
}
