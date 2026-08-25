const archiveLooks = {
  cut: {
    category: 'CUT & DESIGN',
    image: 'https://images.unsplash.com/photo-1562322140-8baeececf3df?auto=format&fit=crop&w=1000&q=85',
    tag: 'LOOK · TEXTURE CUT',
  },
  color: {
    category: 'ARTISAN COLOR',
    image: 'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=1000&q=85',
    tag: 'LOOK · DEEP TONE COLOR',
  },
  perm: {
    category: 'WAVE & VOLUME',
    image: 'https://images.unsplash.com/photo-1595476108010-b4d1f102b1b1?auto=format&fit=crop&w=1000&q=85',
    tag: 'LOOK · LAYERED VOLUME',
  },
};

/** DB 메뉴 이름을 화면용 아카이브 분류로 바꿉니다. */
export function getArchiveLook(serviceName = '') {
  const normalizedName = serviceName.toLowerCase();

  if (normalizedName.includes('염색') || normalizedName.includes('컬러')) {
    return archiveLooks.color;
  }
  if (normalizedName.includes('펌') || normalizedName.includes('웨이브')) {
    return archiveLooks.perm;
  }
  return archiveLooks.cut;
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
