import 'package:flutter/material.dart';

import 'features/home/presentation/home_page.dart';

class MarinboySalonApp extends StatelessWidget {
  const MarinboySalonApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MarinboySalon V4',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xff8e5a4b),
          brightness: Brightness.light,
        ),
        scaffoldBackgroundColor: const Color(0xfffdfaf8),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xfffdfaf8),
          foregroundColor: Color(0xff302522),
        ),
      ),
      home: const HomePage(),
    );
  }
}
