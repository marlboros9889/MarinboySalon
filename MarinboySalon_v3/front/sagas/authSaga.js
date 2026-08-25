import { all, call, fork, put, takeLatest } from 'redux-saga/effects';
import api from '../api/axios';
import {
  LOAD_ME_FAILURE,
  LOAD_ME_REQUEST,
  LOAD_ME_SUCCESS,
  LOG_IN_FAILURE,
  LOG_IN_REQUEST,
  LOG_IN_SUCCESS,
  LOG_OUT_FAILURE,
  LOG_OUT_REQUEST,
  LOG_OUT_SUCCESS,
  SIGN_UP_FAILURE,
  SIGN_UP_REQUEST,
  SIGN_UP_SUCCESS,
} from '../reducers/authReducer';

function logInAPI(data) {
  return api.post('/auth/login', data);
}

function* logIn(action) {
  try {
    const result = yield call(logInAPI, action.data);
    window.localStorage.setItem('accessToken', result.data.accessToken);
    yield put({ type: LOG_IN_SUCCESS, data: result.data.user });
  } catch (error) {
    yield put({ type: LOG_IN_FAILURE, error: error.response?.data?.message || '로그인에 실패했습니다.' });
  }
}

function signUpAPI(data) {
  return api.post('/auth/signup', data);
}

function* signUp(action) {
  try {
    yield call(signUpAPI, action.data);
    yield put({ type: SIGN_UP_SUCCESS });
  } catch (error) {
    yield put({ type: SIGN_UP_FAILURE, error: error.response?.data?.message || '회원가입에 실패했습니다.' });
  }
}

function* loadMe() {
  try {
    const result = yield call(() => api.get('/auth/me'));
    yield put({ type: LOAD_ME_SUCCESS, data: result.data });
  } catch (error) {
    yield put({ type: LOAD_ME_FAILURE, error: error.response?.data?.message });
  }
}

function* logOut() {
  try {
    yield call(() => api.post('/auth/logout'));
    window.localStorage.removeItem('accessToken');
    yield put({ type: LOG_OUT_SUCCESS });
    // 로그아웃 뒤 관리자 화면의 이전 데이터가 남지 않도록 홈으로 이동합니다.
    window.location.href = '/';
  } catch (error) {
    yield put({ type: LOG_OUT_FAILURE, error: error.response?.data?.message || '로그아웃에 실패했습니다.' });
  }
}

function* watchLogIn() {
  yield takeLatest(LOG_IN_REQUEST, logIn);
}

function* watchSignUp() {
  yield takeLatest(SIGN_UP_REQUEST, signUp);
}

function* watchLoadMe() {
  yield takeLatest(LOAD_ME_REQUEST, loadMe);
}

function* watchLogOut() {
  yield takeLatest(LOG_OUT_REQUEST, logOut);
}

export default function* authSaga() {
  yield all([fork(watchLogIn), fork(watchSignUp), fork(watchLoadMe), fork(watchLogOut)]);
}
