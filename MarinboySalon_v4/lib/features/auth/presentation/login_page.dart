import 'dart:async';

import 'package:app_links/app_links.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/auth_provider.dart';

class LoginPage extends ConsumerStatefulWidget {
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  final appLinks = AppLinks();
  StreamSubscription<Uri>? linkSubscription;

  @override
  void initState() {
    super.initState();
    linkSubscription = appLinks.uriLinkStream.listen(handleMobileOAuthLink);
  }

  @override
  void dispose() {
    linkSubscription?.cancel();
    emailController.dispose();
    passwordController.dispose();
    super.dispose();
  }

  Future<void> handleMobileOAuthLink(Uri uri) async {
    if (uri.scheme != 'marinboysalon' || uri.host != 'oauth') return;
    final code = uri.queryParameters['code'];
    if (code == null || code.isEmpty) return;
    final success = await ref
        .read(authProvider.notifier)
        .exchangeMobileOAuthCode(code);
    if (!mounted) return;
    if (success) {
      Navigator.pop(context);
    }
  }

  Future<void> openSocialLogin(String provider) async {
    final opened = await ref
        .read(authProvider.notifier)
        .openSocialLogin(provider);
    if (!mounted || opened) return;
    ScaffoldMessenger.of(context)
        .showSnackBar(const SnackBar(content: Text('소셜 로그인 화면을 열지 못했습니다.')));
  }

  Future<void> submit() async {
    if (emailController.text.trim().isEmpty ||
        passwordController.text.isEmpty) {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('이메일과 비밀번호를 입력해 주세요.')));
      return;
    }
    final success = await ref
        .read(authProvider.notifier)
        .login(emailController.text.trim(), passwordController.text);
    if (!mounted) return;
    if (success) Navigator.pop(context);
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(authProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('로그인')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const SizedBox(height: 24),
            const Text(
              'MARINBOY SALON',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            const Text('예약 조회와 관리 기능을 이용하려면 로그인해 주세요.'),
            const SizedBox(height: 28),
            TextField(
              controller: emailController,
              keyboardType: TextInputType.emailAddress,
              decoration: const InputDecoration(
                labelText: '이메일',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: passwordController,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: '비밀번호',
                border: OutlineInputBorder(),
              ),
            ),
            if (state.message != null)
              Padding(
                padding: const EdgeInsets.only(top: 12),
                child: Text(
                  state.message!,
                  style: const TextStyle(color: Colors.red),
                ),
              ),
            const SizedBox(height: 18),
            FilledButton(
              onPressed: state.loading ? null : submit,
              child: Text(state.loading ? '로그인 중...' : '로그인'),
            ),
            const SizedBox(height: 16),
            const Text('소셜 계정으로 로그인', textAlign: TextAlign.center),
            const SizedBox(height: 8),
            Wrap(
              alignment: WrapAlignment.center,
              spacing: 8,
              children: [
                OutlinedButton(
                  onPressed: state.loading
                      ? null
                      : () => openSocialLogin('google'),
                  child: const Text('Google'),
                ),
                OutlinedButton(
                  onPressed: state.loading
                      ? null
                      : () => openSocialLogin('kakao'),
                  child: const Text('Kakao'),
                ),
                OutlinedButton(
                  onPressed: state.loading
                      ? null
                      : () => openSocialLogin('naver'),
                  child: const Text('Naver'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
