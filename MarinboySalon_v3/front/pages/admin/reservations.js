import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import AdminNavigation from '../../components/AdminNavigation';
import api from '../../api/axios';
import { getReservationPayment } from '../../utils/payment';

const statusInfo = {
  REQUESTED: { label: '접수', nextStatuses: ['CONFIRMED', 'CANCELLED'] },
  CONFIRMED: { label: '확정', nextStatuses: ['COMPLETED', 'CANCELLED'] },
  COMPLETED: { label: '완료', nextStatuses: [] },
  CANCELLED: { label: '취소', nextStatuses: [] },
};

/** 완료와 취소 예약은 상태를 잠그고 색상으로 바로 구분합니다. */
export default function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [newRequestCount, setNewRequestCount] = useState(0);
  const [selectedReservationId, setSelectedReservationId] = useState(null);
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
  const selectedReservation = reservations.find((item) => item.id === selectedReservationId);
  // 같은 고객의 예약을 함께 보여 주어 시술 이력과 결제 예정 금액을 한 번에 확인합니다.
  const customerReservations = selectedReservation
    ? reservations.filter((item) => item.userId === selectedReservation.userId)
    : [];
  const customerPaymentTotal = customerReservations
    .filter((item) => item.status !== 'CANCELLED')
    .reduce((total, item) => total + getReservationPayment(item).finalPrice, 0);
  return <AppLayout><section className="page-section container">
    <header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="heading-text">예약 관리</h1></header>
    <AdminNavigation />
    {newRequestCount > 0 && <p className="admin-notification" role="status">새 예약 접수 {newRequestCount}건이 있습니다.</p>}
    {error && <p className="error-message">{error}</p>}
    <div className="table-responsive paper-table-wrap"><table className="table align-middle">
      <thead><tr><th>고객</th><th>시술</th><th>예약 일시</th><th>요청사항</th><th>상태</th><th>결제</th></tr></thead><tbody>
        {reservations.map((item) => {
          const currentStatus = statusInfo[item.status] || statusInfo.REQUESTED;
          const locked = currentStatus.nextStatuses.length === 0;
          // 요청사항이 없을 때도 관리자가 빈 값으로 혼동하지 않도록 안내 문구를 표시합니다.
          const requestMemo = item.requestMemo?.trim() || '요청사항 없음';
          return <tr key={item.id}><td>{item.userName}<br /><small>{item.userPhone}</small></td><td>{item.serviceName}</td><td>{item.reservationStart.replace('T', ' ')}</td><td>{requestMemo}</td><td>
            <span className={`reservation-status status-${item.status.toLowerCase()}`}>{currentStatus.label}</span>
            {locked ? <small className="status-lock">변경 불가</small> : <select value={item.status} onChange={(event) => onStatusChange(item.id, event.target.value)}>
              <option value={item.status}>{currentStatus.label}</option>{currentStatus.nextStatuses.map((status) => <option key={status} value={status}>{statusInfo[status].label}</option>)}</select>}
          </td><td><button type="button" className="outline-button payment-detail-button" onClick={() => setSelectedReservationId(item.id)}>상세·결제 보기</button></td></tr>;
        })}
      </tbody></table></div>
    {selectedReservation && <ReservationPaymentDetail reservation={selectedReservation} customerReservations={customerReservations} customerPaymentTotal={customerPaymentTotal} />}
  </section></AppLayout>;
}

/** 관리자가 선택한 고객의 시술 이력과 예약별 결제 예정 금액을 확인하는 영역입니다. */
function ReservationPaymentDetail({ reservation, customerReservations, customerPaymentTotal }) {
  const payment = getReservationPayment(reservation);
  return <section className="admin-reservation-detail" aria-live="polite">
    <p className="eyebrow">CUSTOMER PAYMENT</p>
    <h2>{reservation.userName} 고객 시술·결제 내역</h2>
    <div className="payment-summary-card">
      <div><span>선택 시술</span><strong>{reservation.serviceName}</strong></div>
      <div><span>시술 정가</span><strong>{payment.originalPrice.toLocaleString()}원</strong></div>
      <div><span>적용 할인</span><strong>{payment.discountRate > 0 ? `${payment.discountRate}% · -${payment.discountAmount.toLocaleString()}원` : '적용 없음'}</strong></div>
      <div className="payment-final"><span>결제 예정 금액</span><strong>{payment.finalPrice.toLocaleString()}원</strong></div>
    </div>
    <h3>고객 시술 이력</h3>
    <div className="table-responsive"><table className="table payment-history-table">
      <thead><tr><th>예약 일시</th><th>시술</th><th>상태</th><th>결제 금액</th></tr></thead>
      <tbody>{customerReservations.map((item) => {
        const historyPayment = getReservationPayment(item);
        const status = statusInfo[item.status] || statusInfo.REQUESTED;
        return <tr key={item.id}><td>{item.reservationStart.replace('T', ' ')}</td><td>{item.serviceName}</td><td>{status.label}</td><td>{item.status === 'CANCELLED' ? '취소' : `${historyPayment.finalPrice.toLocaleString()}원`}</td></tr>;
      })}</tbody>
    </table></div>
    <p className="customer-payment-total">취소 예약 제외 결제 예정 합계 <strong>{customerPaymentTotal.toLocaleString()}원</strong></p>
  </section>;
}
