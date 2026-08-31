/** Next.js 서버가 최초 HTML을 만들기 전에 시술 메뉴를 조회합니다. */
export async function loadServiceItemsForServer() {
  const apiUrl = process.env.INTERNAL_API_URL
    || process.env.NEXT_PUBLIC_API_URL
    || 'http://localhost:8082';

  try {
    const response = await fetch(`${apiUrl}/api/service-items`);
    if (!response.ok) {
      throw new Error(`메뉴 API 응답 오류: ${response.status}`);
    }

    const serviceItems = await response.json();
    return { serviceItems, error: null };
  } catch (error) {
    return {
      serviceItems: [],
      error: '시술 메뉴를 불러오지 못했습니다.',
    };
  }
}
