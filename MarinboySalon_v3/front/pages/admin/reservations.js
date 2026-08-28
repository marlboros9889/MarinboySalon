import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import AdminNavigation from '../../components/AdminNavigation';
import api from '../../api/axios';

const statusInfo = {
  REQUESTED: { label: '접수', nextStatuses: ['CONFIRMED', 'CANCELED'] },
  CONFIRMED: { label: '확정', nextStatuses: ['COMPLETED', 'CANCELED'] },
  COMPLETED: { label: '완료', nextStatuses: [] },
  CANCELED: { label: '취소', nextStatuses: [] },
};

/** 완료와 취소 예약은 상태를 잠그고 색상으로 바로 구분합니다. */
export default function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [newRequestCount, setNewRequestCount] = useState(0);
  const [error, setError] = useState('');
  const loadReservations = async () => {
    try {
      const response = await api.get('/api/admin/reservations');
      const requestedReservations = response.data.filter((item) => item.status === 'REQUESTED');
      setReservations(response.data);
      setNewRequestCount(requestedReservations.length);
    }
    catch (requestError) { setError(requestError.response?.data?.message || '관리자 예약을 불러오지 못했습니다.'); }
  };
  useEffect(() => {
    loadReservations();
    // 새 예약을 놓치지 않도록 관리자 화면에서 주기적으로 다시 확인합니다.
    const timerId = window.setInterval(loadReservations, 30000);
    return () => window.clearInterval(timerId);
  }, []);
  const onStatusChange = async (id, status) => {
    try { await api.put(`/api/admin/reservations/${id}/status`, { status }); await loadReservations(); }
    catch (requestError) { setError(requestError.response?.data?.message || '상태 변경에 실패했습니다.'); }
  };
  return <AppLayout><section className="page-section container">
    <header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="heading-text">예약 관리</h1></header>
    <AdminNavigation />
    {newRequestCount > 0 && <p className="admin-notification" role="status">새 예약 접수 {newRequestCount}건이 있습니다.</p>}
    {error && <p className="error-message">{error}</p>}
    <div className="table-responsive paper-table-wrap"><table className="table align-middle">
      <thead><tr><th>고객</th><th>시술</th><th>예약 일시</th><th>상태</th></tr></thead><tbody>
        {reservations.map((item) => {
          const currentStatus = statusInfo[item.status] || statusInfo.REQUESTED;
          const locked = currentStatus.nextStatuses.length === 0;
          return <tr key={item.id}><td>{item.userName}<br /><small>{item.userPhone}</small></td><td>{item.serviceName}</td><td>{item.reservationStart.replace('T', ' ')}</td><td>
            <span className={`reservation-status status-${item.status.toLowerCase()}`}>{currentStatus.label}</span>
            {locked ? <small className="status-lock">변경 불가</small> : <select value={item.status} onChange={(event) => onStatusChange(item.id, event.target.value)}>
              <option value={item.status}>{currentStatus.label}</option>{currentStatus.nextStatuses.map((status) => <option key={status} value={status}>{statusInfo[status].label}</option>)}</select>}
          </td></tr>;
        })}
      </tbody></table></div>
  </section></AppLayout>;
}
