class ApiConfig {
  const ApiConfig._();

  // 기본값은 AWS에 배포된 MarinboySalon V3 서버입니다.
  // 필요하면 --dart-define=API_BASE_URL=주소 옵션으로 다른 서버를 지정합니다.
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://marinboysalon.duckdns.org',
  );

  // /uploads/로 시작하는 기존 V3 이미지는 백엔드 주소를 붙여 사용합니다.
  static String resolveImageUrl(String imageUrl) {
    if (imageUrl.startsWith('/uploads/')) {
      return '$baseUrl$imageUrl';
    }
    return imageUrl;
  }
}
