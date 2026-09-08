import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import AdminNavigation from '../../components/AdminNavigation';
import api from '../../api/axios';

/** 관리자는 공개 후기를 확인하고, 부적절한 후기만 삭제합니다. */
export default function AdminReviews() {
  const [reviews, setReviews] = useState([]);
  const [error, setError] = useState('');

  const loadReviews = async () => {
    try {
      const response = await api.get('/api/admin/reviews');
      setReviews(response.data);
      setError('');
    } catch (requestError) {
      setError(requestError.response?.data?.message || '리뷰를 불러오지 못했습니다.');
    }
  };

  useEffect(() => { loadReviews(); }, []);

  const removeReview = async (review) => {
    if (!window.confirm(`${review.userName} 고객의 리뷰를 삭제할까요? 삭제 후 복구할 수 없습니다.`)) return;
    try {
      await api.delete(`/api/admin/reviews/${review.id}`);
      await loadReviews();
    } catch (requestError) {
      setError(requestError.response?.data?.message || '리뷰 삭제에 실패했습니다.');
    }
  };

  return (
    <AppLayout><section className="page-section container">
      <header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="heading-text">리뷰 관리</h1></header>
      <AdminNavigation />
      {error && <p className="error-message">{error}</p>}
      <div className="table-responsive paper-table-wrap"><table className="table">
        <thead><tr><th>고객</th><th>시술</th><th>별점</th><th>내용</th><th>관리</th></tr></thead>
        <tbody>{reviews.map((review) => <tr key={review.id}>
          <td>{review.userName}</td><td>{review.serviceName}</td><td>{'★'.repeat(review.rating)}</td><td>{review.content}</td>
          <td><button className="danger-button" type="button" onClick={() => removeReview(review)}>삭제</button></td>
        </tr>)}</tbody>
      </table></div>
    </section></AppLayout>
  );
}
