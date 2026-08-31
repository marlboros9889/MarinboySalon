import { combineReducers } from 'redux';
import auth from './authReducer';
import serviceItem from './serviceItemReducer';
import reservation from './reservationReducer';

// 인증·메뉴·예약 상태를 하나의 Redux 저장소 구조로 묶습니다.
const rootReducer = combineReducers({
  auth,
  serviceItem,
  reservation,
});

export default rootReducer;
