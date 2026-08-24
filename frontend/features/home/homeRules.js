const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '';

/** 서비스 이미지 경로를 안전하게 반환합니다. */
export function serviceImage(item) {
  const catalogNumber = (Number(item?.id || 1) % 5) + 1;
  const fallback = `/images/catalog/catalog-hair-${catalogNumber}-1.jpg`;
  if (!item?.imageUrl || item.imageUrl.startsWith('http')) return `${API_BASE_URL}${fallback}`;
  return `${API_BASE_URL}${item.imageUrl}`;
}

/** 등록된 시술 이미지가 없을 때만 기본 카탈로그 묶음을 반환합니다. */
export function serviceGalleryImages(item) {
  const catalogNumber = (Number(item?.id || 1) % 5) + 1;
  const uploadedImages = [item?.imageUrl, ...(item?.additionalImageUrls || [])]
    .filter((url) => url && !url.startsWith('http'))
    .map((url) => `${API_BASE_URL}${url}`);
  const catalogImages = [1, 2, 3].map(
    (index) => `${API_BASE_URL}/images/catalog/catalog-hair-${catalogNumber}-${index}.jpg`,
  );
  return [...new Set(uploadedImages.length ? uploadedImages : catalogImages)];
}

/** 카테고리 이름이 한글·영문이어도 같은 시술 그룹으로 분류합니다. */
export function groupServices(items, keyword) {
  const categoryPatterns = {
    CUT: /CUT|컷|커트/i,
    PERM: /PERM|펌/i,
    COLOR: /COLOR|컬러|염색/i,
    CARE: /CARE|CLINIC|클리닉|두피|모발/i,
    STYLE: /HAIR|헤어|CUT|컷|커트|PERM|펌|COLOR|컬러|염색|스타일링/i,
  };
  const pattern = categoryPatterns[keyword];
  return items.filter((item) => !pattern || pattern.test(item.category || ''));
}

/** 예약 건수·관리 순위·ID 순서로 안정적인 월간 TOP5를 만듭니다. */
export function monthlyTopFive(items, keyword) {
  return [...groupServices(items, keyword)]
    .sort((left, right) => (Number(right.reservationCount) - Number(left.reservationCount))
      || (Number(left.topRank ?? 99) - Number(right.topRank ?? 99))
      || (Number(left.id) - Number(right.id)))
    .slice(0, 5);
}

/** 각 인기 그룹의 1위는 BEST, 나머지는 HIT로 표시합니다. */
export function popularityBadges(items) {
  const badges = new Map();
  ['STYLE', 'CARE'].forEach((category) => {
    monthlyTopFive(items, category)
      .filter((item) => Number(item.reservationCount) > 0)
      .forEach((item, index) => badges.set(item.id, index === 0 ? 'BEST' : 'HIT'));
  });
  return badges;
}
