class Reservation {
  const Reservation({
    required this.id,
    required this.serviceName,
    required this.servicePrice,
    required this.reservationStart,
    required this.status,
    this.userName,
    this.userPhone,
    this.requestMemo,
    this.calendarEventId,
  });
  final int id;
  final String serviceName;
  final int servicePrice;
  final DateTime reservationStart;
  final String status;
  final String? userName;
  final String? userPhone;
  final String? requestMemo;
  final String? calendarEventId;

  factory Reservation.fromJson(Map<String, dynamic> json) => Reservation(
    id: (json['id'] as num).toInt(),
    serviceName: json['serviceName']?.toString() ?? '시술 정보 없음',
    servicePrice: (json['servicePrice'] as num?)?.toInt() ?? 0,
    reservationStart: DateTime.parse(json['reservationStart'].toString()),
    status: json['status']?.toString() ?? 'PENDING',
    userName: json['userName']?.toString(),
    userPhone: json['userPhone']?.toString(),
    requestMemo: json['requestMemo']?.toString(),
    calendarEventId: json['calendarEventId']?.toString(),
  );
}
