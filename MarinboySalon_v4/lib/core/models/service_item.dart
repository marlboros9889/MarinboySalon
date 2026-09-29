class ServiceItem {
  const ServiceItem({
    required this.id,
    required this.name,
    required this.price,
    required this.durationMinutes,
    required this.description,
    required this.imageUrls,
  });
  final int id;
  final String name;
  final int price;
  final int durationMinutes;
  final String description;
  final List<String> imageUrls;

  factory ServiceItem.fromJson(Map<String, dynamic> json) {
    final rawImageUrls = json['imageUrls'];
    final imageUrls = <String>[];

    // 백엔드가 전달한 이미지 주소만 화면에 사용합니다.
    if (rawImageUrls is List) {
      for (final rawImageUrl in rawImageUrls) {
        final imageUrl = rawImageUrl.toString().trim();
        if (imageUrl.isNotEmpty) {
          imageUrls.add(imageUrl);
        }
      }
    }

    return ServiceItem(
      id: (json['id'] as num).toInt(),
      name: json['name']?.toString() ?? '이름 없는 시술',
      price: (json['price'] as num?)?.toInt() ?? 0,
      durationMinutes: (json['durationMinutes'] as num?)?.toInt() ?? 0,
      description: json['description']?.toString() ?? '',
      imageUrls: imageUrls,
    );
  }
}
