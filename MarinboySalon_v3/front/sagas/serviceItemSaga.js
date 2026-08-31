import { all, call, fork, put, takeLatest } from 'redux-saga/effects';
import api from '../api/axios';
import {
  LOAD_SERVICE_ITEMS_FAILURE,
  LOAD_SERVICE_ITEMS_REQUEST,
  LOAD_SERVICE_ITEMS_SUCCESS,
} from '../reducers/serviceItemReducer';

// 시술 메뉴 목록을 API에서 가져와 화면 상태로 전달합니다.
function* loadServiceItems() {
  try {
    const result = yield call(() => api.get('/api/service-items'));
    yield put({ type: LOAD_SERVICE_ITEMS_SUCCESS, data: result.data });
  } catch (error) {
    yield put({
      type: LOAD_SERVICE_ITEMS_FAILURE,
      error: error.response?.data?.message || '시술 메뉴를 불러오지 못했습니다.',
    });
  }
}

// 같은 메뉴 조회 요청이 연속으로 발생하면 마지막 요청 결과만 반영합니다.
function* watchLoadServiceItems() {
  yield takeLatest(LOAD_SERVICE_ITEMS_REQUEST, loadServiceItems);
}

export default function* serviceItemSaga() {
  yield all([fork(watchLoadServiceItems)]);
}
