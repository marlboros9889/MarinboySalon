import 'package:flutter_test/flutter_test.dart';
import 'package:marinboy_salon_v4/core/models/service_item.dart';

void main() {
  test('서비스 응답의 이미지 주소를 빈 값 없이 보관한다', () {
    final item = ServiceItem.fromJson({
      'id': 1,
      'name': '커트',
      'price': 20000,
      'durationMinutes': 30,
      'description': '기본 커트',
      'imageUrls': ['/uploads/cut.jpg', '  ', '/uploads/cut-detail.jpg'],
    });

    expect(item.imageUrls, ['/uploads/cut.jpg', '/uploads/cut-detail.jpg']);
  });
}
