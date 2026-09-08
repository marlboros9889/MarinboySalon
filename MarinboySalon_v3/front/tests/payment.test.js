import { calculatePayment } from '../utils/payment';

describe('calculatePayment', () => {
  test('applies a percentage discount by rounding down to won', () => {
    expect(calculatePayment(30000, { name: '첫 방문 할인', discountRate: 15 })).toEqual({
      originalPrice: 30000,
      discountRate: 15,
      discountAmount: 4500,
      finalPrice: 25500,
      eventName: '첫 방문 할인',
    });
  });

  test('keeps the menu price when there is no active event', () => {
    expect(calculatePayment(35000, null)).toMatchObject({ discountAmount: 0, finalPrice: 35000 });
  });
});
