// frontend/store/configureStore.js: Reducer와 Saga를 연결한 공통 Redux Store입니다.
import { configureStore } from '@reduxjs/toolkit';
import { createWrapper } from 'next-redux-wrapper';
import createSagaMiddleware from 'redux-saga';
import rootReducer from '../reducers/index.js';
import rootSaga from '../sagas/index.js';

const makeStore = () => {
  const sagaMiddleware = createSagaMiddleware();
  const store = configureStore({
    reducer: rootReducer,
    middleware: (getDefaultMiddleware) => getDefaultMiddleware({ thunk: false }).concat(sagaMiddleware),
  });

  // Next.js SSR과 브라우저가 같은 비동기 흐름을 사용하도록 Store 생성 시 Saga를 시작합니다.
  store.sagaTask = sagaMiddleware.run(rootSaga);
  return store;
};

export const wrapper = createWrapper(makeStore, { debug: false });
