import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';

void main() {
  // Riverpod ProviderScope가 앱 전체의 로그인·예약 상태를 관리합니다.
  runApp(const ProviderScope(child: MarinboySalonApp()));
}
