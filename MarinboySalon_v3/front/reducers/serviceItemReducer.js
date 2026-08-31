export const LOAD_SERVICE_ITEMS_REQUEST = 'LOAD_SERVICE_ITEMS_REQUEST';
export const LOAD_SERVICE_ITEMS_SUCCESS = 'LOAD_SERVICE_ITEMS_SUCCESS';
export const LOAD_SERVICE_ITEMS_FAILURE = 'LOAD_SERVICE_ITEMS_FAILURE';

// 메뉴 조회 중 상태와 결과를 화면에서 구분하기 위한 초기 저장소 값입니다.
const initialState = {
  serviceItems: [],
  loadServiceItemsLoading: false,
  loadServiceItemsError: null,
};

export default function serviceItemReducer(state = initialState, action) {
  switch (action.type) {
    case LOAD_SERVICE_ITEMS_REQUEST:
      return { ...state, loadServiceItemsLoading: true, loadServiceItemsError: null };
    case LOAD_SERVICE_ITEMS_SUCCESS:
      return { ...state, loadServiceItemsLoading: false, serviceItems: action.data };
    case LOAD_SERVICE_ITEMS_FAILURE:
      return { ...state, loadServiceItemsLoading: false, loadServiceItemsError: action.error };
    default:
      return state;
  }
}
