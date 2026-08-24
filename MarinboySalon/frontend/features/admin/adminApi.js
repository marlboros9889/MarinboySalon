import { jsonRequest, requestJson } from '../shared/api/jwtApi.js';

/** 관리자 도메인별 조회를 분리해 변경된 영역만 다시 불러올 수 있게 합니다. */
export const adminApi = {
  reservations(page, size = 5) {
    return requestJson(
      `/api/admin/reservations?page=${page}&size=${size}`,
      {},
      '예약 현황을 불러오지 못했습니다.',
    );
  },

  services() {
    return requestJson('/api/admin/services', {}, '시술 메뉴를 불러오지 못했습니다.');
  },

  calendar() {
    return requestJson('/api/admin/calendar', {}, 'Calendar 설정을 불러오지 못했습니다.');
  },

  businessHours() {
    return requestJson('/api/admin/business-hours', {}, '영업시간을 불러오지 못했습니다.');
  },

  holidays() {
    return requestJson('/api/admin/holidays', {}, '휴무일을 불러오지 못했습니다.');
  },

  async dashboard(page, size = 5) {
    const [reservationData, services, calendar, businessHours, holidays] = await Promise.all([
      this.reservations(page, size),
      this.services(),
      this.calendar(),
      this.businessHours(),
      this.holidays(),
    ]);
    return { reservationData, services, calendar, businessHours, holidays };
  },

  changeReservationStatus(id, status) {
    return requestJson(
      `/api/admin/reservations/${id}/status?status=${encodeURIComponent(status)}`,
      { method: 'PATCH' },
      '상태를 변경할 수 없습니다.',
    );
  },

  saveService(id, form) {
    return requestJson(
      id ? `/api/admin/services/${id}` : '/api/admin/services',
      { method: id ? 'PATCH' : 'POST', body: form },
      '메뉴 저장에 실패했습니다.',
    );
  },

  deleteService(id) {
    return requestJson(
      `/api/admin/services/${id}`,
      { method: 'DELETE' },
      '메뉴 삭제에 실패했습니다.',
    );
  },

  saveBusinessHour(dayOfWeek, businessHour) {
    return requestJson(
      `/api/admin/business-hours/${dayOfWeek}`,
      jsonRequest('PUT', businessHour),
      '영업 규칙을 저장하지 못했습니다.',
    );
  },

  saveHoliday(holiday) {
    return requestJson('/api/admin/holidays', jsonRequest('POST', holiday), '휴무일을 등록하지 못했습니다.');
  },

  deleteHoliday(holidayDate) {
    return requestJson(
      `/api/admin/holidays?holidayDate=${encodeURIComponent(holidayDate)}`,
      { method: 'DELETE' },
      '휴무일을 해제하지 못했습니다.',
    );
  },
};
