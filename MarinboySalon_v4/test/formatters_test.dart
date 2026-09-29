import 'package:flutter_test/flutter_test.dart';
import 'package:marinboy_salon_v4/core/utils/formatters.dart';

void main() {
  test('금액을 천 단위 구분 기호와 원 단위로 표시한다', () {
    expect(formatWon(125000), '125,000원');
  });

  test('예약 일시를 발표용으로 읽기 쉬운 형식으로 표시한다', () {
    expect(formatDateTime(DateTime(2026, 9, 29, 9, 5)), '2026.09.29 09:05');
  });
}
