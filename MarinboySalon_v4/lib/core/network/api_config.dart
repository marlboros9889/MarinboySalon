class ApiConfig {
  const ApiConfig._();

  // 기본값은 AWS에 배포된 MarinboySalon V3 서버입니다.
  // 필요하면 --dart-define=API_BASE_URL=주소 옵션으로 다른 서버를 지정합니다.
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://marinboysalon.duckdns.org',
  );
}
