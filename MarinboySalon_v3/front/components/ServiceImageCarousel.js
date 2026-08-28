import { useEffect, useMemo, useState } from 'react';
import { getServiceImageUrls } from '../utils/serviceItem';

const IMAGE_CHANGE_INTERVAL = 5000;

/** 메뉴에 연결된 이미지를 5초마다 순서대로 보여줍니다. */
export default function ServiceImageCarousel({ serviceItem, className, children }) {
  const imageUrls = useMemo(() => getServiceImageUrls(serviceItem), [serviceItem]);
  const [imageIndex, setImageIndex] = useState(0);

  useEffect(() => {
    setImageIndex(0);
    if (imageUrls.length < 2) {
      return undefined;
    }

    const intervalId = window.setInterval(() => {
      setImageIndex((currentIndex) => (currentIndex + 1) % imageUrls.length);
    }, IMAGE_CHANGE_INTERVAL);

    return () => window.clearInterval(intervalId);
  }, [imageUrls]);

  return (
    <div
      className={className}
      data-image-count={imageUrls.length}
      data-image-index={imageIndex}
    >
      <img
        key={imageUrls[imageIndex]}
        src={imageUrls[imageIndex]}
        alt={`${serviceItem.name} 스타일 ${imageIndex + 1}`}
        onError={(event) => {
          event.currentTarget.src = '/images/salon-background.png';
        }}
      />
      {children}
    </div>
  );
}
