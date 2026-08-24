import { useState } from 'react';
import { reservationApi } from './reservationApi';

/** 두 예약 화면이 같은 방식으로 시간 목록을 조회하도록 API 호출과 상태를 묶습니다. */
export function useReservationSlots() {
  const [slots, setSlots] = useState([]);

  const clearSlots = () => setSlots([]);
  const replaceSlots = (nextSlots) => setSlots(Array.isArray(nextSlots) ? nextSlots : []);

  const loadSlots = async (serviceId, date, arrangeSlots) => {
    if (!serviceId || !date) {
      clearSlots();
      return [];
    }

    const result = await reservationApi.availableSlots(serviceId, date);
    const availableSlots = Array.isArray(result.availableSlots) ? result.availableSlots : [];
    const nextSlots = arrangeSlots ? arrangeSlots(availableSlots) : availableSlots;
    setSlots(nextSlots);
    return nextSlots;
  };

  return { slots, clearSlots, replaceSlots, loadSlots };
}
