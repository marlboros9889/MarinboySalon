class ServiceItem {
  const ServiceItem({
    required this.id,
    required this.name,
    required this.price,
    required this.durationMinutes,
    required this.description,
  });
  final int id;
  final String name;
  final int price;
  final int durationMinutes;
  final String description;

  factory ServiceItem.fromJson(Map<String, dynamic> json) => ServiceItem(
    id: (json['id'] as num).toInt(),
    name: json['name']?.toString() ?? '이름 없는 시술',
    price: (json['price'] as num?)?.toInt() ?? 0,
    durationMinutes: (json['durationMinutes'] as num?)?.toInt() ?? 0,
    description: json['description']?.toString() ?? '',
  );
}
