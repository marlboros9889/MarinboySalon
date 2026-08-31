import serviceItemReducer, {
  LOAD_SERVICE_ITEMS_FAILURE,
  LOAD_SERVICE_ITEMS_REQUEST,
  LOAD_SERVICE_ITEMS_SUCCESS,
} from '../reducers/serviceItemReducer';

// 메뉴 조회 결과가 로딩·성공·실패에 따라 올바르게 저장되는지 확인합니다.
describe('서비스 메뉴 상태 흐름', () => {
  test('요청, 성공, 실패 상태를 구분한다', () => {
    const loadingState = serviceItemReducer(undefined, { type: LOAD_SERVICE_ITEMS_REQUEST });
    expect(loadingState.loadServiceItemsLoading).toBe(true);

    const serviceItems = [{ id: 1, name: '남성 커트' }];
    const successState = serviceItemReducer(loadingState, {
      type: LOAD_SERVICE_ITEMS_SUCCESS,
      data: serviceItems,
    });
    expect(successState.loadServiceItemsLoading).toBe(false);
    expect(successState.serviceItems).toEqual(serviceItems);

    const failureState = serviceItemReducer(loadingState, {
      type: LOAD_SERVICE_ITEMS_FAILURE,
      error: '메뉴 조회 실패',
    });
    expect(failureState.loadServiceItemsLoading).toBe(false);
    expect(failureState.loadServiceItemsError).toBe('메뉴 조회 실패');
  });
});
