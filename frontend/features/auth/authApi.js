import {
  clearAccessToken,
  getCurrentUser,
  jsonRequest,
  requestJson,
  saveAccessToken,
} from '../shared/api/jwtApi';

/** 인증 화면은 URL·JWT 저장 방식을 모르고 인증 기능명만 사용합니다. */
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
