import { all, call } from 'redux-saga/effects';
import authSaga from './authSaga';
import serviceItemSaga from './serviceItemSaga';
import reservationSaga from './reservationSaga';

// 도메인별 비동기 처리기를 한곳에서 시작해 화면 요청을 분산 처리합니다.
export default function* rootSaga() {
  yield all([call(authSaga), call(serviceItemSaga), call(reservationSaga)]);
}
