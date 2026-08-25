import { all, call, fork, put, takeLatest } from 'redux-saga/effects';
import api from '../api/axios';
import {
  CANCEL_RESERVATION_FAILURE,
  CANCEL_RESERVATION_REQUEST,
  CANCEL_RESERVATION_SUCCESS,
  CREATE_RESERVATION_FAILURE,
  CREATE_RESERVATION_REQUEST,
  CREATE_RESERVATION_SUCCESS,
  LOAD_MY_RESERVATIONS_FAILURE,
  LOAD_MY_RESERVATIONS_REQUEST,
  LOAD_MY_RESERVATIONS_SUCCESS,
  LOAD_AVAILABLE_TIMES_FAILURE,
  LOAD_AVAILABLE_TIMES_REQUEST,
  LOAD_AVAILABLE_TIMES_SUCCESS,
} from '../reducers/reservationReducer';

function* loadAvailableTimes(action) {
  try {
    const result = yield call(() => api.get('/api/reservations/available-times', { params: action.data }));
    yield put({ type: LOAD_AVAILABLE_TIMES_SUCCESS, data: result.data });
  } catch (error) {
    yield put({
      type: LOAD_AVAILABLE_TIMES_FAILURE,
      error: error.response?.data?.message || '예약 가능 시간을 불러오지 못했습니다.',
    });
  }
}

function* loadMyReservations() {
  try {
    const result = yield call(() => api.get('/api/reservations/my'));
    yield put({ type: LOAD_MY_RESERVATIONS_SUCCESS, data: result.data });
  } catch (error) {
    yield put({ type: LOAD_MY_RESERVATIONS_FAILURE, error: error.response?.data?.message || '예약을 불러오지 못했습니다.' });
  }
}

function* createReservation(action) {
  try {
    const result = yield call(() => api.post('/api/reservations', action.data));
    yield put({ type: CREATE_RESERVATION_SUCCESS, data: result.data });
  } catch (error) {
    yield put({ type: CREATE_RESERVATION_FAILURE, error: error.response?.data?.message || '예약에 실패했습니다.' });
  }
}

function* cancelReservation(action) {
  try {
    yield call(() => api.delete(`/api/reservations/${action.data}`));
    yield put({ type: CANCEL_RESERVATION_SUCCESS, data: action.data });
  } catch (error) {
    yield put({ type: CANCEL_RESERVATION_FAILURE, error: error.response?.data?.message || '예약 취소에 실패했습니다.' });
  }
}

function* watchLoadMyReservations() {
  yield takeLatest(LOAD_MY_RESERVATIONS_REQUEST, loadMyReservations);
}

function* watchCreateReservation() {
  yield takeLatest(CREATE_RESERVATION_REQUEST, createReservation);
}

function* watchCancelReservation() {
  yield takeLatest(CANCEL_RESERVATION_REQUEST, cancelReservation);
}

function* watchLoadAvailableTimes() {
  yield takeLatest(LOAD_AVAILABLE_TIMES_REQUEST, loadAvailableTimes);
}

export default function* reservationSaga() {
  yield all([
    fork(watchLoadMyReservations),
    fork(watchCreateReservation),
    fork(watchCancelReservation),
    fork(watchLoadAvailableTimes),
  ]);
}
