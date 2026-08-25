import { combineReducers } from 'redux';
import auth from './authReducer';
import serviceItem from './serviceItemReducer';
import reservation from './reservationReducer';

const rootReducer = combineReducers({
  auth,
  serviceItem,
  reservation,
});

export default rootReducer;
