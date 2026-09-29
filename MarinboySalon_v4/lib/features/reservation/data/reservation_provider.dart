import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/reservation.dart';
import '../../../core/network/api_client.dart';

typedef AvailableTimeRequest = ({int serviceId, String date});

final availableTimesProvider =
    FutureProvider.family<List<String>, AvailableTimeRequest>((
      ref,
      request,
    ) async {
      // V3 서버가 휴무일과 기존 예약을 반영해 실제 예약 가능한 시간만 돌려줍니다.
      final response = await ref
          .read(dioProvider)
          .get(
            '/api/reservations/available-times',
            queryParameters: {
              'date': request.date,
              'serviceId': request.serviceId,
            },
          );
      final times = response.data as List<dynamic>;
      return times.map((time) => time.toString()).toList();
    });

final myReservationProvider = FutureProvider<List<Reservation>>((ref) async {
  final response = await ref.read(dioProvider).get('/api/reservations/my');
  final list = response.data as List<dynamic>;
  return list
      .map(
        (item) => Reservation.fromJson(Map<String, dynamic>.from(item as Map)),
      )
      .toList();
});

final adminReservationProvider = FutureProvider<List<Reservation>>((ref) async {
  final response = await ref.read(dioProvider).get('/api/admin/reservations');
  final list = response.data as List<dynamic>;
  return list
      .map(
        (item) => Reservation.fromJson(Map<String, dynamic>.from(item as Map)),
      )
      .toList();
});

class ReservationRepository {
  ReservationRepository(this.ref);
  final Ref ref;

  Future<void> create({
    required int serviceId,
    required DateTime start,
    required String memo,
  }) async {
    // 서버가 LocalDateTime으로 읽을 수 있도록 초 단위 없는 ISO 문자열을 전송합니다.
    final value = start.toIso8601String().split('.').first;
    await ref
        .read(dioProvider)
        .post(
          '/api/reservations',
          data: {
            'serviceId': serviceId,
            'reservationStart': value,
            'requestMemo': memo,
          },
        );
    // 예약을 완료한 뒤에는 다른 화면에서도 최신 예약 가능 시간을 다시 조회합니다.
    ref.invalidate(availableTimesProvider);
    ref.invalidate(myReservationProvider);
  }

  Future<void> cancel(int id) async {
    await ref.read(dioProvider).delete('/api/reservations/$id');
    ref.invalidate(myReservationProvider);
  }

  Future<void> updateStatus(int id, String status) async {
    await ref
        .read(dioProvider)
        .put('/api/admin/reservations/$id/status', data: {'status': status});
    ref.invalidate(adminReservationProvider);
  }
}

final reservationRepositoryProvider = Provider<ReservationRepository>(
  ReservationRepository.new,
);
