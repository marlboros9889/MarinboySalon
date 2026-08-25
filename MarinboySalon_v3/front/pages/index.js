import Link from 'next/link';
import AppLayout from '../components/AppLayout';

export default function Home() {
  return (
    <AppLayout>
      <section className="hero-section container">
        <div className="hero-copy">
          <p className="eyebrow">ONE PERSON HAIR STUDIO</p>
          <h1 className="serif-text">당신의 하루에<br />어울리는 머리</h1>
          <p className="hero-description">
            충분한 상담 시간과 겹치지 않는 예약으로 한 사람에게 집중합니다.
          </p>
          <div className="hero-actions">
            <Link href="/reservations/new" className="primary-link">예약하기</Link>
            <Link href="/services" className="text-link">시술 메뉴 보기 →</Link>
          </div>
        </div>
        <div className="hero-collage" aria-label="마린보이 살롱 소개 콜라주">
          <div className="collage-block collage-pink">CUT</div>
          <div className="collage-paper torn-paper-edge">
            <span className="serif-text">MARINBOY</span>
            <small>PRIVATE SALON</small>
          </div>
          <div className="collage-circle">1:1</div>
          <div className="collage-line">COLOR · PERM · CARE</div>
        </div>
      </section>

      <section className="feature-section container">
        <article><span>01</span><h2 className="serif-text">정확한 시간</h2><p>시술 시간과 기존 예약을 함께 계산합니다.</p></article>
        <article><span>02</span><h2 className="serif-text">간단한 예약</h2><p>메뉴와 날짜를 고르면 바로 신청할 수 있습니다.</p></article>
        <article><span>03</span><h2 className="serif-text">직접 관리</h2><p>영업시간과 휴무일을 관리자가 직접 바꿉니다.</p></article>
      </section>
    </AppLayout>
  );
}
