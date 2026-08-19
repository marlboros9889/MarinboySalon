// frontend/reducers/service.js: 공개 시술 목록의 요청·성공·실패 상태를 관리합니다.
export const LOAD_SERVICES_REQUEST = 'service/LOAD_SERVICES_REQUEST';
export const LOAD_SERVICES_SUCCESS = 'service/LOAD_SERVICES_SUCCESS';
export const LOAD_SERVICES_FAILURE = 'service/LOAD_SERVICES_FAILURE';

export const loadServicesRequest = () => ({ type: LOAD_SERVICES_REQUEST });
export const loadServicesSuccess = (items) => ({ type: LOAD_SERVICES_SUCCESS, payload: items });
export const loadServicesFailure = (message) => ({ type: LOAD_SERVICES_FAILURE, payload: message });

const initialState = { items: [], loading: false, error: null };

export default function serviceReducer(state = initialState, action) {
  //1. View와 Saga가 보낸 액션에 따라 화면에서 사용할 상태만 예측 가능하게 변경합니다.
  switch (action.type) {
    case LOAD_SERVICES_REQUEST:
      return { ...state, loading: true, error: null };
    case LOAD_SERVICES_SUCCESS:
      return { ...state, items: action.payload, loading: false, error: null };
    case LOAD_SERVICES_FAILURE:
      return { ...state, loading: false, error: action.payload };
    default:
      return state;
  }
}
