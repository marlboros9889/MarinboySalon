import reservationReducer, {
  LOAD_AVAILABLE_TIMES_FAILURE,
  LOAD_AVAILABLE_TIMES_REQUEST,
  LOAD_AVAILABLE_TIMES_SUCCESS,
} from '../reducers/reservationReducer';
import { formatDateInputValue, formatTimeLabel } from '../utils/reservation';

describe('예약 가능 시간 상태 흐름', () => {
  test('30분 시간 목록의 요청, 성공, 실패 상태를 구분한다', () => {
    const loadingState = reservationReducer(undefined, { type: LOAD_AVAILABLE_TIMES_REQUEST });
    expect(loadingState.loadAvailableTimesLoading).toBe(true);

    const successState = reservationReducer(loadingState, {
      type: LOAD_AVAILABLE_TIMES_SUCCESS,
      data: ['10:00', '10:30'],
    });
    expect(successState.availableTimes).toEqual(['10:00', '10:30']);
    expect(successState.loadAvailableTimesLoading).toBe(false);

    const failureState = reservationReducer(loadingState, {
      type: LOAD_AVAILABLE_TIMES_FAILURE,
      error: '조회 실패',
    });
    expect(failureState.availableTimes).toEqual([]);
    expect(failureState.availableTimesError).toBe('조회 실패');
  });

  test('날짜 입력과 시간 버튼 표기를 고객용 형식으로 만든다', () => {
    expect(formatDateInputValue(new Date(2026, 7, 5))).toBe('2026-08-05');
    expect(formatTimeLabel('09:30')).toBe('오전 9:30');
    expect(formatTimeLabel('13:00')).toBe('오후 1:00');
  });
});
