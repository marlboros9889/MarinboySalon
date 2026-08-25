import { all, call } from 'redux-saga/effects';
import authSaga from './authSaga';
import serviceItemSaga from './serviceItemSaga';
import reservationSaga from './reservationSaga';

export default function* rootSaga() {
  yield all([call(authSaga), call(serviceItemSaga), call(reservationSaga)]);
}
