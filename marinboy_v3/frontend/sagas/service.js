// frontend/sagas/service.js: 공개 시술 API 비동기 요청을 View에서 분리합니다.
import { call, put, takeLatest } from 'redux-saga/effects';
import {
  LOAD_SERVICES_REQUEST,
  loadServicesFailure,
  loadServicesSuccess,
} from '../reducers/service.js';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '';

export async function fetchServices() {
  const response = await fetch(`${API_BASE_URL}/api/services`, { credentials: 'include' });
  if (!response.ok) throw new Error(`시술 API 응답 오류: ${response.status}`);
  const data = await response.json();
  return Array.isArray(data) ? data : [];
}

export function* loadServices() {
  //1. 마지막 조회 요청만 처리해 빠른 재시도에서 이전 응답이 화면을 덮지 않게 합니다.
  try {
    const items = yield call(fetchServices);
    yield put(loadServicesSuccess(items));
  } catch {
    yield put(loadServicesFailure('시술 메뉴를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'));
  }
}

export default function* serviceSaga() {
  yield takeLatest(LOAD_SERVICES_REQUEST, loadServices);
}
