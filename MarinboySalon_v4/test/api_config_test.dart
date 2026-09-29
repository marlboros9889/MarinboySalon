import 'package:flutter_test/flutter_test.dart';
import 'package:marinboy_salon_v4/core/network/api_config.dart';

void main() {
  test('V3 업로드 이미지를 기존 백엔드 경로로 연결한다', () {
    expect(
      ApiConfig.resolveImageUrl('/uploads/service-items/cut.jpg'),
      '${ApiConfig.baseUrl}/uploads/service-items/cut.jpg',
    );
  });

  test('도메인 루트 이미지 경로를 모바일에서 사용할 절대 주소로 변환한다', () {
    final imageUrl = ApiConfig.resolveImageUrl('/images/services/cut.jpg');

    expect(Uri.parse(imageUrl).hasAbsolutePath, isTrue);
    expect(Uri.parse(imageUrl).host, isNotEmpty);
    expect(imageUrl, contains('/images/services/cut.jpg'));
  });

  test('절대 이미지 주소는 그대로 둔다', () {
    const imageUrl = 'https://cdn.example.com/cut.jpg';

    expect(ApiConfig.resolveImageUrl(imageUrl), imageUrl);
  });
}
