export const LOG_IN_REQUEST = 'LOG_IN_REQUEST';
export const LOG_IN_SUCCESS = 'LOG_IN_SUCCESS';
export const LOG_IN_FAILURE = 'LOG_IN_FAILURE';
export const SIGN_UP_REQUEST = 'SIGN_UP_REQUEST';
export const SIGN_UP_SUCCESS = 'SIGN_UP_SUCCESS';
export const SIGN_UP_FAILURE = 'SIGN_UP_FAILURE';
export const LOAD_ME_REQUEST = 'LOAD_ME_REQUEST';
export const LOAD_ME_SUCCESS = 'LOAD_ME_SUCCESS';
export const LOAD_ME_FAILURE = 'LOAD_ME_FAILURE';
export const LOG_OUT_REQUEST = 'LOG_OUT_REQUEST';
export const LOG_OUT_SUCCESS = 'LOG_OUT_SUCCESS';
export const LOG_OUT_FAILURE = 'LOG_OUT_FAILURE';

const initialState = {
  me: null,
  logInLoading: false,
  logInError: null,
  signUpLoading: false,
  signUpDone: false,
  signUpError: null,
};

// 인증 상태를 요청/성공/실패 세 단계로 나눠 화면에서 로딩과 오류를 표시합니다.
export default function authReducer(state = initialState, action) {
  switch (action.type) {
    case LOG_IN_REQUEST:
      return { ...state, logInLoading: true, logInError: null };
    case LOG_IN_SUCCESS:
      return { ...state, logInLoading: false, me: action.data };
    case LOG_IN_FAILURE:
      return { ...state, logInLoading: false, logInError: action.error };
    case SIGN_UP_REQUEST:
      return { ...state, signUpLoading: true, signUpDone: false, signUpError: null };
    case SIGN_UP_SUCCESS:
      return { ...state, signUpLoading: false, signUpDone: true };
    case SIGN_UP_FAILURE:
      return { ...state, signUpLoading: false, signUpError: action.error };
    case LOAD_ME_SUCCESS:
      return { ...state, me: action.data };
    case LOAD_ME_FAILURE:
    case LOG_OUT_SUCCESS:
      return { ...state, me: null };
    default:
      return state;
  }
}
