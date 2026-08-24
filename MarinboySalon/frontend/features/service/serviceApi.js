import { requestJson } from '../shared/api/jwtApi';

/** 홈과 예약 화면이 같은 시술 메뉴 조회 기능을 재사용합니다. */
export const serviceApi = {
  list() {
    return requestJson('/api/services', {}, '시술 메뉴를 불러오지 못했습니다.');
  },
};
