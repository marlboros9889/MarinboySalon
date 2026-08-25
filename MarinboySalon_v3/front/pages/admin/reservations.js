import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import api from '../../api/axios';

const statusOptions = ['REQUESTED', 'CONFIRMED', 'COMPLETED', 'CANCELED'];

export default function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [error, setError] = useState('');

  const loadReservations = async () => {
    try {
      const response = await api.get('/api/admin/reservations');
      setReservations(response.data);
    } catch (requestError) {
      setError(requestError.response?.data?.message || '관리자 예약을 불러오지 못했습니다.');
    }
  };

  useEffect(() => {
    loadReservations();
  }, []);

  const onStatusChange = async (id, status) => {
    try {
      await api.put(`/api/admin/reservations/${id}/status`, { status });
      await loadReservations();
    } catch (requestError) {
      setError(requestError.response?.data?.message || '상태 변경에 실패했습니다.');
    }
  };

  return (
    <AppLayout>
      <section className="page-section container">
        <header className="page-heading admin-heading">
          <p className="eyebrow">ADMIN</p>
          <h1 className="heading-text">예약 관리</h1>
        </header>
        {error && <p className="error-message">{error}</p>}
        <div className="table-responsive paper-table-wrap">
          <table className="table align-middle">
            <thead><tr><th>고객</th><th>시술</th><th>예약 일시</th><th>상태</th></tr></thead>
            <tbody>
              {reservations.map((item) => (
                <tr key={item.id}>
                  <td>{item.userName}<br /><small>{item.userPhone}</small></td>
                  <td>{item.serviceName}</td>
                  <td>{item.reservationStart.replace('T', ' ')}</td>
                  <td>
                    <select value={item.status} onChange={(event) => onStatusChange(item.id, event.target.value)}>
                      {statusOptions.map((status) => <option key={status} value={status}>{status}</option>)}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </AppLayout>
  );
}
