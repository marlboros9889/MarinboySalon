import { authApi } from '../auth/authApi';
import { serviceApi } from '../service/serviceApi';
import { jsonRequest, requestJson } from '../shared/api/jwtApi';

/** 예약 화면은 예약 기능만 골라 사용하고 HTTP 세부 처리는 공통 도구에 맡깁니다. */
export const reservationApi = {
  async bookingPage() {
    const [services, user] = await Promise.all([serviceApi.list(), authApi.currentUser()]);
    return { services, user };
  },

  availableSlots(serviceId, date) {
    return requestJson(
      `/api/services/${serviceId}/available-slots?date=${encodeURIComponent(date)}`,
      {},
      '예약 가능 시간을 불러오지 못했습니다.',
    );
  },

  create(reservation) {
    return requestJson(
      '/api/reservations',
      jsonRequest('POST', reservation),
      '예약에 실패했습니다. 선택한 시간을 다시 확인해 주세요.',
    );
  },

  listMine() {
    return requestJson('/api/customers/my-reservations', {}, '예약 목록을 불러오지 못했습니다.');
  },

  update(reservationId, reservation) {
    return requestJson(
      `/api/customers/my-reservations/${reservationId}`,
      jsonRequest('PUT', reservation),
      '예약 수정에 실패했습니다. 가능한 시간과 상태를 확인해 주세요.',
    );
  },

  cancel(reservationId) {
    return requestJson(
      `/api/customers/my-reservations/${reservationId}`,
      { method: 'DELETE' },
      '예약 취소에 실패했습니다. 예약 상태를 다시 확인해 주세요.',
    );
  },
};
