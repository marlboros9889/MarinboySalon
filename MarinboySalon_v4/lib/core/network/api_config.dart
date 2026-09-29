class ApiConfig {
  const ApiConfig._();

  // 기본값은 AWS에서 Spring Boot로 연결되는 MarinboySalon V3 API 경로입니다.
  // 필요하면 --dart-define=API_BASE_URL=주소 옵션으로 다른 서버를 지정합니다.
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://marinboysalon.duckdns.org/backend',
  );

  // /uploads/로 시작하는 기존 V3 이미지는 백엔드 주소를 붙여 사용합니다.
  static String resolveImageUrl(String imageUrl) {
    if (imageUrl.startsWith('/uploads/')) {
      return '$baseUrl$imageUrl';
    }
    if (imageUrl.startsWith('/')) {
      // 도메인 루트의 공개 이미지도 모바일에서 절대 주소로 열 수 있게 변환합니다.
      return Uri.parse(baseUrl).resolve(imageUrl).toString();
    }
    return imageUrl;
  }
}
