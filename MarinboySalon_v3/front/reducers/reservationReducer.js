export const LOAD_MY_RESERVATIONS_REQUEST = 'LOAD_MY_RESERVATIONS_REQUEST';
export const LOAD_MY_RESERVATIONS_SUCCESS = 'LOAD_MY_RESERVATIONS_SUCCESS';
export const LOAD_MY_RESERVATIONS_FAILURE = 'LOAD_MY_RESERVATIONS_FAILURE';
export const CREATE_RESERVATION_REQUEST = 'CREATE_RESERVATION_REQUEST';
export const CREATE_RESERVATION_SUCCESS = 'CREATE_RESERVATION_SUCCESS';
export const CREATE_RESERVATION_FAILURE = 'CREATE_RESERVATION_FAILURE';
export const CANCEL_RESERVATION_REQUEST = 'CANCEL_RESERVATION_REQUEST';
export const CANCEL_RESERVATION_SUCCESS = 'CANCEL_RESERVATION_SUCCESS';
export const CANCEL_RESERVATION_FAILURE = 'CANCEL_RESERVATION_FAILURE';
export const LOAD_AVAILABLE_TIMES_REQUEST = 'LOAD_AVAILABLE_TIMES_REQUEST';
export const LOAD_AVAILABLE_TIMES_SUCCESS = 'LOAD_AVAILABLE_TIMES_SUCCESS';
export const LOAD_AVAILABLE_TIMES_FAILURE = 'LOAD_AVAILABLE_TIMES_FAILURE';

const initialState = {
  reservations: [],
  loadReservationsLoading: false,
  createReservationLoading: false,
  createReservationDone: false,
  reservationError: null,
  availableTimes: [],
  loadAvailableTimesLoading: false,
  availableTimesError: null,
};

export default function reservationReducer(state = initialState, action) {
  switch (action.type) {
    case LOAD_MY_RESERVATIONS_REQUEST:
      return { ...state, loadReservationsLoading: true, reservationError: null };
    case LOAD_MY_RESERVATIONS_SUCCESS:
      return { ...state, loadReservationsLoading: false, reservations: action.data };
    case CREATE_RESERVATION_REQUEST:
      return { ...state, createReservationLoading: true, createReservationDone: false, reservationError: null };
    case CREATE_RESERVATION_SUCCESS:
      return {
        ...state,
        createReservationLoading: false,
        createReservationDone: true,
        reservations: [action.data, ...state.reservations],
      };
    case LOAD_AVAILABLE_TIMES_REQUEST:
      return {
        ...state,
        availableTimes: [],
        loadAvailableTimesLoading: true,
        availableTimesError: null,
      };
    case LOAD_AVAILABLE_TIMES_SUCCESS:
      return {
        ...state,
        availableTimes: action.data,
        loadAvailableTimesLoading: false,
      };
    case LOAD_AVAILABLE_TIMES_FAILURE:
      return {
        ...state,
        availableTimes: [],
        loadAvailableTimesLoading: false,
        availableTimesError: action.error,
      };
    case CANCEL_RESERVATION_SUCCESS:
      return {
        ...state,
        reservations: state.reservations.map((item) => (
          item.id === action.data ? { ...item, status: 'CANCELLED' } : item
        )),
      };
    case LOAD_MY_RESERVATIONS_FAILURE:
    case CREATE_RESERVATION_FAILURE:
    case CANCEL_RESERVATION_FAILURE:
      return {
        ...state,
        loadReservationsLoading: false,
        createReservationLoading: false,
        reservationError: action.error,
      };
    default:
      return state;
  }
}
