import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/service_item.dart';
import '../../../core/network/api_client.dart';
import '../../../core/utils/formatters.dart';
import '../../auth/data/auth_provider.dart';
import '../../auth/presentation/login_page.dart';
import '../data/reservation_provider.dart';

class ReservationFormPage extends ConsumerStatefulWidget {
  const ReservationFormPage({super.key, required this.service});
  final ServiceItem service;

  @override
  ConsumerState<ReservationFormPage> createState() =>
      _ReservationFormPageState();
}

class _ReservationFormPageState extends ConsumerState<ReservationFormPage> {
  DateTime selected = DateTime.now().add(const Duration(days: 1, hours: 10));
  final memoController = TextEditingController();
  bool submitting = false;

  @override
  void dispose() {
    memoController.dispose();
    super.dispose();
  }

  Future<void> chooseDate() async {
    final date = await showDatePicker(
      context: context,
      initialDate: selected,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 90)),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(selected),
    );
    if (time == null) return;
    setState(
      () => selected = DateTime(
        date.year,
        date.month,
        date.day,
        time.hour,
        time.minute,
      ),
    );
  }

  Future<void> submit() async {
    if (ref.read(authProvider).user == null) {
      await Navigator.push(
        context,
        MaterialPageRoute(builder: (_) => const LoginPage()),
      );
      if (ref.read(authProvider).user == null) return;
    }
    setState(() => submitting = true);
    try {
      await ref
          .read(reservationRepositoryProvider)
          .create(
            serviceId: widget.service.id,
            start: selected,
            memo: memoController.text.trim(),
          );
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('예약을 등록했습니다.')));
      Navigator.pop(context);
    } catch (error) {
      if (mounted)
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(apiErrorMessage(error))));
    } finally {
      if (mounted) setState(() => submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('예약하기')),
    body: ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Text(
          widget.service.name,
          style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 8),
        Text(
          '${formatWon(widget.service.price)} · 약 ${widget.service.durationMinutes}분',
        ),
        const SizedBox(height: 28),
        const Text('예약 일시', style: TextStyle(fontWeight: FontWeight.bold)),
        const SizedBox(height: 8),
        OutlinedButton.icon(
          onPressed: chooseDate,
          icon: const Icon(Icons.calendar_month),
          label: Text(formatDateTime(selected)),
        ),
        const SizedBox(height: 22),
        TextField(
          controller: memoController,
          maxLength: 500,
          maxLines: 4,
          decoration: const InputDecoration(
            labelText: '요청사항',
            hintText: '예: 조용한 자리 부탁드립니다.',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 16),
        FilledButton(
          onPressed: submitting ? null : submit,
          child: Text(submitting ? '예약 중...' : '예약 확정 요청'),
        ),
      ],
    ),
  );
}
