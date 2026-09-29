import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/reservation.dart';
import '../../../core/utils/formatters.dart';
import '../../reservation/data/reservation_provider.dart';

class AdminDashboardPage extends ConsumerWidget {
  const AdminDashboardPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final reservations = ref.watch(adminReservationProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('관리자 대시보드')),
      body: reservations.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text('관리자 예약 정보를 불러오지 못했습니다.\n$error'),
          ),
        ),
        data: (items) => RefreshIndicator(
          onRefresh: () => ref.refresh(adminReservationProvider.future),
          child: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              const Text(
                '예약 현황',
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              const Text('예약 데이터를 기준으로 운영 현황과 예상 매출을 확인합니다.'),
              const SizedBox(height: 18),
              Wrap(
                spacing: 10,
                runSpacing: 10,
                children: [
                  _SummaryCard(
                    label: '전체 예약',
                    value: '${items.length}건',
                    icon: Icons.calendar_month,
                  ),
                  _SummaryCard(
                    label: '확정 예약',
                    value:
                        '${items.where((item) => item.status == 'CONFIRMED').length}건',
                    icon: Icons.check_circle_outline,
                  ),
                  _SummaryCard(
                    label: '예상 매출',
                    value: formatWon(_expectedSales(items)),
                    icon: Icons.payments_outlined,
                  ),
                ],
              ),
              const SizedBox(height: 28),
              const Text(
                '예약 목록',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              if (items.isEmpty)
                const Padding(
                  padding: EdgeInsets.all(24),
                  child: Center(child: Text('예약이 없습니다.')),
                ),
              ...items.map((item) => _ReservationTile(item: item)),
            ],
          ),
        ),
      ),
    );
  }

  int _expectedSales(List<Reservation> items) {
    return items
        .where((item) => item.status != 'CANCELLED')
        .fold(0, (sum, item) => sum + item.servicePrice);
  }
}

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({
    required this.label,
    required this.value,
    required this.icon,
  });
  final String label;
  final String value;
  final IconData icon;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 160,
    child: Card(
      color: Colors.white,
      elevation: 0,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, color: Theme.of(context).colorScheme.primary),
            const SizedBox(height: 12),
            Text(label, style: const TextStyle(color: Colors.black54)),
            const SizedBox(height: 4),
            Text(
              value,
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
            ),
          ],
        ),
      ),
    ),
  );
}

class _ReservationTile extends ConsumerWidget {
  const _ReservationTile({required this.item});
  final Reservation item;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '${item.userName ?? '고객'} · ${item.serviceName}',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 5),
            Text(
              '${formatDateTime(item.reservationStart)} · ${formatWon(item.servicePrice)}',
            ),
            if (item.requestMemo != null && item.requestMemo!.isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 5),
                child: Text('요청: ${item.requestMemo}'),
              ),
            const SizedBox(height: 8),
            DropdownButton<String>(
              value: item.status,
              items: const ['PENDING', 'CONFIRMED', 'CANCELLED']
                  .map(
                    (status) =>
                        DropdownMenuItem(value: status, child: Text(status)),
                  )
                  .toList(),
              onChanged: (status) async {
                if (status == null || status == item.status) return;
                await ref
                    .read(reservationRepositoryProvider)
                    .updateStatus(item.id, status);
              },
            ),
          ],
        ),
      ),
    );
  }
}
