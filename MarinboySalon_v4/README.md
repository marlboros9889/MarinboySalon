# MarinboySalon V4

Flutter(Dart) 앱과 기존 MarinboySalon V3 Spring Boot REST API를 연결하는 예약·관리 프로젝트입니다.

## 범위

- 고객: 시술 조회, 로그인, 예약 등록, 내 예약 조회·취소
- 관리자: 전체 예약 조회, 예약 상태 변경, 예약 기반 예상 매출 확인
- 제외: 직원관리, 직원배정, 근무표

## 실행

```powershell
flutter pub get
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

Android 에뮬레이터는 `10.0.2.2`로 PC의 Spring 서버에 접근합니다. 웹·Windows 앱은 실행 환경의 Spring API 주소를 `API_BASE_URL`에 넣습니다.

## 검증

```powershell
flutter test --no-pub
flutter build web --no-pub
```

## 평가 자료

- [문항 1 프로젝트 설계 답안](제출자료/01_문항1_프로젝트_설계_답안.md)
- [REST API 명세서](제출자료/api_spec.md)
- [문항 2 결과보고서 및 PPT 구성](제출자료/02_문항2_결과보고서_발표구성.md)
