// Next.js 화면과 서버 렌더링 코드를 같은 Jest 환경에서 검증하기 위한 테스트 설정입니다.
const nextJest = require('next/jest');

const createJestConfig = nextJest({ dir: './' });

module.exports = createJestConfig({
  testEnvironment: 'node',
});
