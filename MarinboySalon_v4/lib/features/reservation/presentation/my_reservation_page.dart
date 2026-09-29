import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/utils/formatters.dart';
import '../data/reservation_provider.dart';

class MyReservationPage extends ConsumerWidget {
  const MyReservationPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final reservations = ref.watch(myReservationProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('내 예약')),
      body: reservations.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(child: Text(error.toString())),
        data: (items) => items.isEmpty
            ? const Center(child: Text('예약 내역이 없습니다.'))
            : RefreshIndicator(
                onRefresh: () => ref.refresh(myReservationProvider.future),
                child: ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: items.length,
                  itemBuilder: (context, index) {
                    final item = items[index];
                    return Card(
                      child: ListTile(
                        title: Text(item.serviceName),
                        subtitle: Text(
                          '${formatDateTime(item.reservationStart)}\n${formatWon(item.servicePrice)} · ${item.status}',
                        ),
                        isThreeLine: true,
                        trailing: item.status == 'CANCELLED'
                            ? null
                            : TextButton(
                                onPressed: () async {
                                  await ref
                                      .read(reservationRepositoryProvider)
                                      .cancel(item.id);
                                },
                                child: const Text('취소'),
                              ),
                      ),
                    );
                  },
                ),
              ),
      ),
    );
  }
}
