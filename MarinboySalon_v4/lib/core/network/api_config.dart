class ApiConfig {
  const ApiConfig._();

  // 실행 환경마다 서버 주소가 다르므로 --dart-define으로 안전하게 주입합니다.
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );
}
