import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/service_item.dart';
import '../../../core/network/api_client.dart';

final serviceListProvider = FutureProvider<List<ServiceItem>>((ref) async {
  // 발표 화면 확인용 모드이며, 기본값 false에서는 항상 실제 Spring API를 호출합니다.
  const demoMode = bool.fromEnvironment('DEMO_MODE', defaultValue: false);
  if (demoMode) {
    return const [
      ServiceItem(
        id: 1,
        name: '디자인 커트',
        price: 25000,
        durationMinutes: 40,
        description: '얼굴형과 모발 상태를 고려한 커트',
        imageUrls: [],
      ),
      ServiceItem(
        id: 2,
        name: '클리닉 펌',
        price: 120000,
        durationMinutes: 120,
        description: '손상 모발 케어를 포함한 펌',
        imageUrls: [],
      ),
      ServiceItem(
        id: 3,
        name: '헤어 컬러',
        price: 95000,
        durationMinutes: 90,
        description: '상담 후 원하는 색상으로 진행',
        imageUrls: [],
      ),
    ];
  }
  // 공개 시술 API는 로그인하지 않은 고객도 첫 화면에서 조회할 수 있습니다.
  final response = await ref.read(dioProvider).get('/api/service-items');
  final list = response.data as List<dynamic>;
  return list
      .map(
        (item) => ServiceItem.fromJson(Map<String, dynamic>.from(item as Map)),
      )
      .toList();
});
