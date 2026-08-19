// frontend/sagas/index.js: 기능별 Saga를 동시에 시작합니다.
import { all, fork } from 'redux-saga/effects';
import serviceSaga from './service.js';

export default function* rootSaga() {
  yield all([fork(serviceSaga)]);
}
