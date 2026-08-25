import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import api from '../../api/axios';

const emptyForm = { name: '', price: '', durationMinutes: '', description: '' };

export default function AdminServiceItems() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');

  const loadItems = async () => {
    try {
      const response = await api.get('/api/admin/service-items');
      setItems(response.data);
    } catch (requestError) {
      setError(requestError.response?.data?.message || '시술 항목을 불러오지 못했습니다.');
    }
  };

  useEffect(() => {
    loadItems();
  }, []);

  const onChange = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const onSubmit = async (event) => {
    event.preventDefault();
    try {
      await api.post('/api/admin/service-items', {
        ...form,
        price: Number(form.price),
        durationMinutes: Number(form.durationMinutes),
      });
      setForm(emptyForm);
      await loadItems();
    } catch (requestError) {
      setError(requestError.response?.data?.message || '시술 등록에 실패했습니다.');
    }
  };

  const onDelete = async (id) => {
    await api.delete(`/api/admin/service-items/${id}`);
    await loadItems();
  };

  return (
    <AppLayout>
      <section className="page-section container">
        <header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="serif-text">시술 메뉴 관리</h1></header>
        <form className="admin-inline-form" onSubmit={onSubmit}>
          <input name="name" value={form.name} onChange={onChange} placeholder="시술명" required />
          <input name="price" type="number" value={form.price} onChange={onChange} placeholder="가격" required />
          <input name="durationMinutes" type="number" value={form.durationMinutes} onChange={onChange} placeholder="소요 분" required />
          <input name="description" value={form.description} onChange={onChange} placeholder="설명" />
          <button className="primary-button" type="submit">등록</button>
        </form>
        {error && <p className="error-message">{error}</p>}
        <div className="service-grid admin-grid">
          {items.map((item) => (
            <article className="paper-card" key={item.id}>
              <h2 className="serif-text">{item.name}</h2>
              <p>{item.price.toLocaleString()}원 · {item.durationMinutes}분</p>
              <p>{item.active ? '사용 중' : '비활성'}</p>
              {item.active && <button className="outline-button" type="button" onClick={() => onDelete(item.id)}>비활성화</button>}
            </article>
          ))}
        </div>
      </section>
    </AppLayout>
  );
}
