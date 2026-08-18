# Marinboy Admin Mobile

Expo React Native 관리자 앱입니다. Firebase 이메일 로그인 후 Spring 서버에서 ID 토큰을 검증하고 예약·알림을 조회합니다.

1. `.env.example`을 `.env`로 복사해 Firebase 웹 앱 설정과 API 주소를 입력합니다.
2. Firebase Authentication에서 이메일/비밀번호 로그인을 켜고, `MB_USER.EMAIL`과 같은 이메일의 인증 완료 계정을 만듭니다.
3. 서버에 `FIREBASE_ENABLED=true`, `FIREBASE_PROJECT_ID`, `FIREBASE_CREDENTIALS_PATH`를 설정합니다.
4. `npx eas init` 후 EAS 프로젝트 ID를 `.env`에 입력하고 Android FCM V1/iOS APNs 자격 증명을 등록합니다.
5. `npm start`로 실행합니다. 원격 푸시는 Expo Go가 아닌 development build에서 확인합니다.

서비스 계정 비공개 키는 Git에 커밋하지 않습니다.
