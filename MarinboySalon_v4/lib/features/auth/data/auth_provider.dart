import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';

class AuthState {
  const AuthState({this.user, this.loading = false, this.message});
  final Map<String, dynamic>? user;
  final bool loading;
  final String? message;

  bool get isAdmin => user?['role']?.toString() == 'ADMIN';
  AuthState copyWith({
    Map<String, dynamic>? user,
    bool? loading,
    String? message,
  }) {
    return AuthState(
      user: user ?? this.user,
      loading: loading ?? this.loading,
      message: message,
    );
  }
}

class AuthNotifier extends Notifier<AuthState> {
  @override
  AuthState build() => const AuthState();

  Future<bool> login(String email, String password) async {
    state = state.copyWith(loading: true, message: null);
    try {
      final response = await ref
          .read(dioProvider)
          .post('/auth/login', data: {'email': email, 'password': password});
      final data = Map<String, dynamic>.from(response.data as Map);
      final token = data['accessToken']?.toString();
      final user = Map<String, dynamic>.from(data['user'] as Map);
      if (token == null || token.isEmpty) throw Exception('토큰이 없습니다.');
      await ref
          .read(secureStorageProvider)
          .write(key: 'accessToken', value: token);
      await ref
          .read(secureStorageProvider)
          .write(key: 'user', value: jsonEncode(user));
      state = AuthState(user: user);
      return true;
    } catch (error) {
      state = AuthState(message: apiErrorMessage(error));
      return false;
    }
  }

  Future<void> restore() async {
    final savedUser = await ref.read(secureStorageProvider).read(key: 'user');
    if (savedUser == null) return;
    state = AuthState(
      user: Map<String, dynamic>.from(jsonDecode(savedUser) as Map),
    );
  }

  Future<void> logout() async {
    await ref.read(secureStorageProvider).deleteAll();
    state = const AuthState();
  }
}

final authProvider = NotifierProvider<AuthNotifier, AuthState>(
  AuthNotifier.new,
);
