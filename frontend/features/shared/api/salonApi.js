import {
  clearAccessToken,
  getCurrentUser,
  jsonRequest,
  requestJson,
  saveAccessToken,
} from './jwtApi';

/** 인증 화면은 URL·JWT 저장 방식을 모르고 이 도구의 기능명만 사용합니다. */
export const authApi = {
  currentUser: getCurrentUser,

  socialProviders() {
    return requestJson('/api/auth/social/providers', {}, '소셜 로그인 설정을 확인하지 못했습니다.');
  },

  checkDuplicate(field, value) {
    const path = field === 'username' ? 'check-username?username=' : 'check-email?email=';
    return requestJson(`/api/auth/${path}${encodeURIComponent(value)}`, {}, '중복 확인에 실패했습니다.');
  },

  async login(username, password) {
    const result = await requestJson(
      '/api/auth/login',
      jsonRequest('POST', { username, password }),
      '아이디 또는 비밀번호를 확인해 주세요.',
    );
    saveAccessToken(result.accessToken);
    return result.user;
  },

  signup(signup) {
    return requestJson('/api/auth/signup', jsonRequest('POST', signup), '회원가입에 실패했습니다.');
  },

  async logout() {
    try {
      await requestJson('/api/auth/logout', { method: 'POST' }, '로그아웃 요청에 실패했습니다.');
    } finally {
      clearAccessToken();
    }
  },

  updateProfile(profile) {
    return requestJson(
      '/api/customers/me',
      jsonRequest('PUT', profile),
      '고객 정보를 저장하지 못했습니다.',
    );
  },
};

/** 시술 메뉴 조회는 홈·예약 화면이 함께 사용하는 하나의 API 기능입니다. */
export const serviceApi = {
  list() {
    return requestJson('/api/services', {}, '시술 메뉴를 불러오지 못했습니다.');
  },
};

/** 예약 화면은 필요한 기능을 이 객체에서 골라 사용하고 HTTP 세부 코드는 반복하지 않습니다. */
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
};

/** 관리자 화면의 여러 API를 한 업무 단위로 묶어 화면의 중복 요청 코드를 줄입니다. */
export const adminApi = {
  async dashboard(page, size = 5) {
    const [reservationData, services, calendar, businessHours, holidays] = await Promise.all([
      requestJson(`/api/admin/reservations?page=${page}&size=${size}`, {}, '예약 현황을 불러오지 못했습니다.'),
      requestJson('/api/admin/services', {}, '시술 메뉴를 불러오지 못했습니다.'),
      requestJson('/api/admin/calendar', {}, 'Calendar 설정을 불러오지 못했습니다.'),
      requestJson('/api/admin/business-hours', {}, '영업시간을 불러오지 못했습니다.'),
      requestJson('/api/admin/holidays', {}, '휴무일을 불러오지 못했습니다.'),
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
