const archiveLooks = {
  cut: {
    category: 'CUT & DESIGN',
    tag: 'LOOK · TEXTURE CUT',
  },
  care: {
    category: 'SCALP & CARE',
    tag: 'RITUAL · SCALP CARE',
  },
  color: {
    category: 'ARTISAN COLOR',
    tag: 'LOOK · DEEP TONE COLOR',
  },
  perm: {
    category: 'WAVE & VOLUME',
    tag: 'LOOK · LAYERED VOLUME',
  },
  bridal: {
    category: 'BRIDAL BEAUTY',
    tag: 'LOOK · BRIDAL MOMENT',
  },
};

const fallbackImageUrl = '/images/salon-background.png';

/** DB 메뉴 이름을 화면용 아카이브 분류로 바꿉니다. */
export function getArchiveLook(serviceName = '') {
  const normalizedName = serviceName.toLowerCase();

  if (normalizedName.includes('두피') || normalizedName.includes('클리닉')) {
    return archiveLooks.care;
  }
  if (normalizedName.includes('신부') || normalizedName.includes('메이크업')) {
    return archiveLooks.bridal;
  }
  if (normalizedName.includes('염색') || normalizedName.includes('컬러')) {
    return archiveLooks.color;
  }
  if (normalizedName.includes('펌') || normalizedName.includes('웨이브')) {
    return archiveLooks.perm;
  }
  return archiveLooks.cut;
}

/** API 이미지가 없을 때도 화면이 깨지지 않도록 기본 이미지를 반환합니다. */
export function getServiceImageUrls(serviceItem = {}) {
  if (!Array.isArray(serviceItem.imageUrls) || serviceItem.imageUrls.length === 0) {
    return [fallbackImageUrl];
  }
  return serviceItem.imageUrls.slice(0, 4);
}

/** URL의 서비스 id가 실제 활성 메뉴에 있을 때만 폼 값으로 반환합니다. */
export function getValidServiceId(serviceItems, requestedServiceId) {
  const numericServiceId = Number(requestedServiceId);
  const serviceExists = serviceItems.some((item) => item.id === numericServiceId);

  if (!serviceExists) {
    return '';
  }
  return String(numericServiceId);
}
