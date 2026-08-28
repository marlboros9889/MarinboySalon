import { useState } from 'react';
import { useRouter } from 'next/router';
import AppLayout from '../../components/AppLayout';
import api from '../../api/axios';

/** 완료 예약의 고객이 별점과 후기를 남기는 화면입니다. */
export default function NewReview() {
  const router = useRouter();
  const [rating, setRating] = useState('5');
  const [content, setContent] = useState('');
  const [error, setError] = useState('');
  const onSubmit = async (event) => {
    event.preventDefault();
    try {
      await api.post('/api/reviews', { reservationId: Number(router.query.reservationId), rating: Number(rating), content });
      router.replace('/reservations');
    } catch (requestError) { setError(requestError.response?.data?.message || '후기 등록에 실패했습니다.'); }
  };
  return <AppLayout><section className="auth-section container"><form className="paper-form" onSubmit={onSubmit}>
    <p className="eyebrow">REVIEW</p><h1 className="heading-text">시술 후기 작성</h1>
    <label htmlFor="rating">별점</label><select id="rating" value={rating} onChange={(event) => setRating(event.target.value)}><option value="5">★★★★★ 매우 만족</option><option value="4">★★★★ 만족</option><option value="3">★★★ 보통</option><option value="2">★★ 아쉬움</option><option value="1">★ 불만족</option></select>
    <label htmlFor="content">후기 내용</label><textarea id="content" value={content} onChange={(event) => setContent(event.target.value)} maxLength="500" rows="6" required />
    {error && <p className="error-message">{error}</p>}<button className="primary-button" type="submit">후기 등록</button>
  </form></section></AppLayout>;
}
