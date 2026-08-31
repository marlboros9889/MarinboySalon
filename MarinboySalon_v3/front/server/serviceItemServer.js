/** Next.js 서버가 최초 HTML을 만들기 전에 시술 메뉴를 조회합니다. */
import { internalApiBaseUrl } from '../api/apiConfig';

export async function loadServiceItemsForServer() {
  try {
    const response = await fetch(`${internalApiBaseUrl}/api/service-items`);
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
