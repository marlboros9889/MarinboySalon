import { all, call, fork, put, takeLatest } from 'redux-saga/effects';
import api from '../api/axios';
import {
  LOAD_SERVICE_ITEMS_FAILURE,
  LOAD_SERVICE_ITEMS_REQUEST,
  LOAD_SERVICE_ITEMS_SUCCESS,
} from '../reducers/serviceItemReducer';

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

function* watchLoadServiceItems() {
  yield takeLatest(LOAD_SERVICE_ITEMS_REQUEST, loadServiceItems);
}

export default function* serviceItemSaga() {
  yield all([fork(watchLoadServiceItems)]);
}
