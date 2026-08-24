import test from 'node:test';
import assert from 'node:assert/strict';
import { groupServices, monthlyTopFive, popularityBadges } from '../features/home/homeRules.js';

const services = [
  { id: 3, category: 'CUT', reservationCount: 2 },
  { id: 2, category: '펌', reservationCount: 5 },
  { id: 1, category: 'CARE', reservationCount: 3 },
];

// 카테고리 분류 도구는 화면과 무관하게 한글·영문 데이터를 같은 규칙으로 처리합니다.
test('카테고리별 시술을 분류한다', () => {
  assert.deepEqual(groupServices(services, 'PERM').map((item) => item.id), [2]);
  assert.deepEqual(groupServices(services, 'CARE').map((item) => item.id), [1]);
});

// 인기 순위와 배지는 홈의 여러 컴포넌트가 같은 계산 결과를 재사용합니다.
test('예약 건수 순으로 인기 메뉴와 배지를 만든다', () => {
  assert.deepEqual(monthlyTopFive(services, 'STYLE').map((item) => item.id), [2, 3]);
  const badges = popularityBadges(services);
  assert.equal(badges.get(2), 'BEST');
  assert.equal(badges.get(3), 'HIT');
  assert.equal(badges.get(1), 'BEST');
});
