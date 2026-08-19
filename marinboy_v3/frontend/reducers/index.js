// frontend/reducers/index.js: 기능별 Reducer와 Next.js SSR 상태를 하나로 합칩니다.
import { combineReducers } from '@reduxjs/toolkit';
import { HYDRATE } from 'next-redux-wrapper';
import serviceReducer from './service.js';

const combinedReducer = combineReducers({ service: serviceReducer });

export default function rootReducer(state, action) {
  // SSR 결과를 브라우저 Store에 병합해야 첫 화면과 hydration 결과가 동일해집니다.
  if (action.type === HYDRATE) {
    return {
      ...state,
      ...action.payload,
      service: { ...state?.service, ...action.payload.service },
    };
  }
  return combinedReducer(state, action);
}
