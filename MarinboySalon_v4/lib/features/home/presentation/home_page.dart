import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/service_item.dart';
import '../../../core/utils/formatters.dart';
import '../../admin/presentation/admin_dashboard_page.dart';
import '../../auth/data/auth_provider.dart';
import '../../auth/presentation/login_page.dart';
import '../../reservation/presentation/my_reservation_page.dart';
import '../../reservation/presentation/reservation_form_page.dart';
import '../../service/data/service_provider.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  void openLogin(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const LoginPage()),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final services = ref.watch(serviceListProvider);
    final auth = ref.watch(authProvider);
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'MARINBOY SALON',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        actions: [
          if (auth.user == null)
            TextButton(
              onPressed: () => openLogin(context),
              child: const Text('로그인'),
            )
          else
            PopupMenuButton<String>(
              onSelected: (value) async {
                if (value == 'logout')
                  await ref.read(authProvider.notifier).logout();
                if (value == 'admin')
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (_) => const AdminDashboardPage(),
                    ),
                  );
              },
              itemBuilder: (context) => [
                if (auth.isAdmin)
                  const PopupMenuItem(value: 'admin', child: Text('관리자 대시보드')),
                const PopupMenuItem(value: 'logout', child: Text('로그아웃')),
              ],
            ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.refresh(serviceListProvider.future),
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
          children: [
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: const Color(0xff3b2926),
                borderRadius: BorderRadius.circular(24),
              ),
              child: const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '오늘, 나를 위한 시간',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 26,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  SizedBox(height: 8),
                  Text(
                    '시술을 선택하고 원하는 시간에 간편하게 예약하세요.',
                    style: TextStyle(color: Color(0xffeaded8)),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 28),
            const Text(
              '시술 메뉴',
              style: TextStyle(fontSize: 21, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            services.when(
              loading: () => const Center(
                child: Padding(
                  padding: EdgeInsets.all(24),
                  child: CircularProgressIndicator(),
                ),
              ),
              error: (error, _) =>
                  _ErrorBox(message: '시술 메뉴를 불러오지 못했습니다.\n${error.toString()}'),
              data: (items) => Column(
                children: items
                    .map((item) => _ServiceCard(item: item))
                    .toList(),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: NavigationBar(
        destinations: [
          const NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: '홈',
          ),
          NavigationDestination(
            icon: const Icon(Icons.calendar_month_outlined),
            label: '내 예약',
            enabled: auth.user != null,
          ),
        ],
        onDestinationSelected: (index) {
          if (index == 1) {
            if (auth.user == null) {
              openLogin(context);
            } else {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const MyReservationPage()),
              );
            }
          }
        },
      ),
    );
  }
}

class _ServiceCard extends StatelessWidget {
  const _ServiceCard({required this.item});
  final ServiceItem item;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      color: Colors.white,
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              item.name,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 6),
            if (item.description.isNotEmpty)
              Text(
                item.description,
                style: const TextStyle(color: Colors.black54),
              ),
            const SizedBox(height: 12),
            Row(
              children: [
                Text(
                  formatWon(item.price),
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
                const SizedBox(width: 12),
                Text(
                  '${item.durationMinutes}분',
                  style: const TextStyle(color: Colors.black54),
                ),
                const Spacer(),
                FilledButton.tonal(
                  onPressed: () => Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (_) => ReservationFormPage(service: item),
                    ),
                  ),
                  child: const Text('예약'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _ErrorBox extends StatelessWidget {
  const _ErrorBox({required this.message});
  final String message;
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(20),
    decoration: BoxDecoration(
      color: Colors.red.shade50,
      borderRadius: BorderRadius.circular(16),
    ),
    child: Text(message),
  );
}
