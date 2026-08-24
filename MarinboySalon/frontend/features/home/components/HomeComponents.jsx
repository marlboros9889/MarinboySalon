import { useEffect, useMemo, useState } from 'react';
import { serviceGalleryImages, serviceImage } from '../homeRules';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '';

/** 아이디·이메일 입력과 중복 확인 상태를 같은 UI 부품으로 사용합니다. */
export function DuplicateField({ label, type = 'text', value, status, onChange, onCheck }) {
  const statusText = {
    available: '사용 가능', duplicate: '이미 사용 중', empty: '입력 필요', error: '확인 실패',
  }[status];
  const autoComplete = type === 'email' ? 'email' : 'username';

  return (
    <div className="salon-duplicate-field">
      <div>
        <input
          aria-label={label}
          type={type}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder={label}
          autoComplete={autoComplete}
          required
        />
        <button type="button" onClick={onCheck}>중복 확인</button>
      </div>
      {statusText && (
        <small className={status === 'available' ? 'available' : 'unavailable'}>{statusText}</small>
      )}
    </div>
  );
}

/** 회원가입 직후 예약 정책을 확인하는 공통 모달입니다. */
export function SignupPolicyModal({ agreed, onAgree, onConfirm }) {
  return (
    <div className="salon-policy-backdrop">
      <section className="salon-policy-modal" role="dialog" aria-modal="true" aria-labelledby="signup-policy-title">
        <p className="salon-eyebrow">RESERVATION POLICY</p>
        <h2 id="signup-policy-title">노쇼·예약 취소 안내</h2>
        <ul>
          <li>방문이 어려운 경우 예약 시간 전에 반드시 취소해 주세요.</li>
          <li>무단 불참, 10분 이상 지각, 당일 취소 시 이후 예약이 제한될 수 있습니다.</li>
          <li>예약은 고객과 매장이 함께 지키는 소중한 약속입니다.</li>
        </ul>
        <label className={`salon-check-field${agreed ? ' is-agreed' : ''}`}>
          <input
            className="salon-checkbox"
            type="checkbox"
            checked={agreed}
            onChange={(event) => onAgree(event.target.checked)}
          />
          <span className="salon-check-copy">
            <strong>필수 동의</strong><small>안내 내용을 읽고 확인했습니다.</small>
          </span>
          <span className="salon-check-state">{agreed ? '동의 완료' : '미동의'}</span>
        </label>
        <button className="salon-primary-button" type="button" disabled={!agreed} onClick={onConfirm}>
          확인하고 로그인으로 이동
        </button>
      </section>
    </div>
  );
}

/** TOP5 목록은 홈의 두 인기 그룹에서 같은 표시 도구를 재사용합니다. */
export function TopList({ title, services, fallback, onOpen }) {
  return (
    <div className="salon-top-list">
      <h3>{title}</h3>
      {services.length ? services.map((item, index) => (
        <button key={item.id} onClick={() => onOpen(item)} aria-label={`${item.name} 전체 사진 보기`}>
          <span>0{index + 1}</span>
          <RotatingServiceImage service={item} compact />
          <i><b>{item.name}</b><small>{Number(item.price || 0).toLocaleString()}원</small></i>
          <em>사진 보기</em>
        </button>
      )) : <p>{fallback}</p>}
    </div>
  );
}

/** 시술별 이미지를 순환하고 컴포넌트가 사라질 때 타이머를 정리합니다. */
export function RotatingServiceImage({ service, badge, compact = false }) {
  const images = useMemo(() => serviceGalleryImages(service), [service]);
  const [imageIndex, setImageIndex] = useState(0);

  useEffect(() => {
    setImageIndex(0);
    if (images.length < 2) return undefined;
    const timer = window.setInterval(() => {
      setImageIndex((current) => (current + 1) % images.length);
    }, 3500);
    return () => window.clearInterval(timer);
  }, [images.length, service.id]);

  const image = images[imageIndex % Math.max(images.length, 1)] || serviceImage(service);
  return (
    <div className={`salon-service-image-rotator${compact ? ' compact' : ''}`}>
      <img
        key={image}
        src={image}
        alt={compact ? '' : service.name}
        onError={(event) => { event.currentTarget.src = `${API_BASE_URL}/images/catalog/catalog-hair-2-1.jpg`; }}
      />
      {badge && <span className={`salon-popularity-badge ${badge.toLowerCase()}`}>{badge}</span>}
    </div>
  );
}

/** 선택한 시술의 모든 사진과 예약 이동 기능을 모달 부품으로 제공합니다. */
export function ServiceGallery({ service, onClose, onReserve }) {
  const images = serviceGalleryImages(service);
  return (
    <div
      className="salon-gallery-backdrop"
      role="presentation"
      onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}
    >
      <section className="salon-gallery-modal" role="dialog" aria-modal="true" aria-labelledby="service-gallery-title">
        <button className="salon-gallery-close" type="button" onClick={onClose} aria-label="사진 갤러리 닫기">×</button>
        <p className="salon-eyebrow">SERVICE GALLERY</p>
        <h2 id="service-gallery-title">{service.name}</h2>
        <div className="salon-gallery-images">
          {images.map((image, index) => (
            <img key={image} src={image} alt={`${service.name} 시술 사진 ${index + 1}`} />
          ))}
        </div>
        <div className="salon-gallery-actions">
          <p>{service.description || '여러 각도의 시술 이미지를 확인하고 예약해 주세요.'}</p>
          <button
            className="salon-primary-button"
            type="button"
            onClick={() => { onClose(); onReserve(service.id); }}
          >
            이 시술 예약하기
          </button>
        </div>
      </section>
    </div>
  );
}
