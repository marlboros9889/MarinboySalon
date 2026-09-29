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
  DateTime selectedDate = DateTime.now().add(const Duration(days: 1));
  String? selectedTime;
  final memoController = TextEditingController();
  bool submitting = false;

  String get selectedDateKey {
    final year = selectedDate.year.toString().padLeft(4, '0');
    final month = selectedDate.month.toString().padLeft(2, '0');
    final day = selectedDate.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }

  @override
  void dispose() {
    memoController.dispose();
    super.dispose();
  }

  Future<void> chooseDate() async {
    final date = await showDatePicker(
      context: context,
      initialDate: selectedDate,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 90)),
    );
    if (date == null || !mounted) return;
    setState(() {
      selectedDate = DateTime(date.year, date.month, date.day);
      selectedTime = null;
    });
  }

  Future<void> submit() async {
    if (selectedTime == null) {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('예약 가능한 시간을 선택해 주세요.')));
      return;
    }
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
            start: DateTime(
              selectedDate.year,
              selectedDate.month,
              selectedDate.day,
              int.parse(selectedTime!.split(':')[0]),
              int.parse(selectedTime!.split(':')[1]),
            ),
            memo: memoController.text.trim(),
          );
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('예약을 등록했습니다.')));
      Navigator.pop(context);
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(apiErrorMessage(error))));
      }
    } finally {
      if (mounted) {
        setState(() => submitting = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final availableTimes = ref.watch(
      availableTimesProvider((
        serviceId: widget.service.id,
        date: selectedDateKey,
      )),
    );

    return Scaffold(
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
          const Text('예약 날짜', style: TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: chooseDate,
            icon: const Icon(Icons.calendar_month),
            label: Text(selectedDateKey),
          ),
          const SizedBox(height: 18),
          const Text(
            '예약 가능한 시간',
            style: TextStyle(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          availableTimes.when(
            loading: () => const Padding(
              padding: EdgeInsets.all(16),
              child: Center(child: CircularProgressIndicator()),
            ),
            error: (error, _) => Text(apiErrorMessage(error)),
            data: (times) {
              if (times.isEmpty) {
                return const Text('선택한 날짜에 예약 가능한 시간이 없습니다. 다른 날짜를 선택해 주세요.');
              }
              return Wrap(
                spacing: 8,
                runSpacing: 8,
                children: times.map((time) {
                  final label = time.length >= 5 ? time.substring(0, 5) : time;
                  return ChoiceChip(
                    label: Text(label),
                    selected: selectedTime == label,
                    onSelected: (_) => setState(() => selectedTime = label),
                  );
                }).toList(),
              );
            },
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
            onPressed: submitting || selectedTime == null ? null : submit,
            child: Text(submitting ? '예약 중...' : '예약 확정 요청'),
          ),
        ],
      ),
    );
  }
}
