import { useEffect, useState } from 'react';
import { useRouter } from 'next/router';
import AppLayout from '../../../components/AppLayout';
import api from '../../../api/axios';
import { getReservationPayment } from '../../../utils/payment';

/** 별도 탭에서 선택한 시술의 실제 받을 금액을 확인합니다. */
export default function ReservationPaymentPage() {
  const router = useRouter();
  const [reservation, setReservation] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!router.isReady) return;
    const reservationId = Number(router.query.id);
    if (!Number.isInteger(reservationId) || reservationId <= 0) {
      setError('확인할 예약 번호가 올바르지 않습니다.');
      return;
    }
    const loadReservation = async () => {
      try {
        const response = await api.get('/api/admin/reservations');
        const selectedReservation = response.data.find((item) => item.id === reservationId);
        if (!selectedReservation) {
          setError('예약 정보를 찾을 수 없습니다.');
          return;
        }
        setReservation(selectedReservation);
      } catch (requestError) {
        setError(requestError.response?.data?.message || '결제 정보를 불러오지 못했습니다.');
      }
    };
    loadReservation();
  }, [router.isReady, router.query.id]);

  const payment = getReservationPayment(reservation);
  return <AppLayout><section className="page-section container payment-page">
    <header className="page-heading"><p className="eyebrow">PAYMENT DETAIL</p><h1 className="heading-text">시술 결제 확인</h1></header>
    {error && <p className="error-message">{error}</p>}
    {reservation && <section className="admin-reservation-detail">
      <h2>{reservation.userName} 고객 · {reservation.reservationStart.replace('T', ' ')}</h2>
      <div className="payment-summary-card">
        <div><span>선택 시술</span><strong>{reservation.serviceName}</strong></div>
        <div><span>시술 정가</span><strong>{payment.originalPrice.toLocaleString()}원</strong></div>
        <div><span>적용 할인</span><strong>{payment.discountRate > 0 ? `${payment.discountRate}% · -${payment.discountAmount.toLocaleString()}원` : '적용 없음'}</strong></div>
        <div className="payment-final"><span>받을 금액</span><strong>{payment.finalPrice.toLocaleString()}원</strong></div>
      </div>
      <p className="payment-help">위 금액은 이 예약에 저장된 할인 적용 후 결제 예정 금액입니다.</p>
    </section>}
  </section></AppLayout>;
}
