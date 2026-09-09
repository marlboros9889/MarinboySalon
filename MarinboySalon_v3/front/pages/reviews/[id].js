import { useEffect, useState } from 'react';
import { useRouter } from 'next/router';
import AppLayout from '../../components/AppLayout';
import api from '../../api/axios';

/** 메인에서 선택한 공개 리뷰 한 건을 새 탭에서 자세히 보여 줍니다. */
export default function ReviewDetail() {
  const router = useRouter();
  const [review, setReview] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!router.isReady) return;
    const reviewId = Number(router.query.id);
    const loadReview = async () => {
      try {
        const response = await api.get('/api/reviews');
        const selectedReview = response.data.find((item) => item.id === reviewId);
        if (!selectedReview || selectedReview.rating < 4) {
          setError('표시할 리뷰를 찾을 수 없습니다.');
          return;
        }
        setReview(selectedReview);
      } catch (requestError) {
        console.log('리뷰 상세를 불러오지 못했습니다.', requestError);
        setError('리뷰를 불러오지 못했습니다.');
      }
    };
    loadReview();
  }, [router.isReady, router.query.id]);

  return <AppLayout><section className="page-section container review-detail-page">
    <header className="page-heading"><p className="eyebrow">REAL REVIEW</p><h1 className="heading-text">고객 시술 후기</h1></header>
    {error && <p className="error-message">{error}</p>}
    {review && <article className="review-detail-card">
      <p className="review-score"><span>{'★'.repeat(review.rating)}</span> {review.rating.toFixed(1)}</p>
      <h2>{review.serviceName}</h2>
      <p>{review.content}</p>
      <small>— {review.userName} 고객</small>
    </article>}
  </section></AppLayout>;
}
