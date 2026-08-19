// frontend/tests/serviceReducer.test.js: 시술 조회 액션별 상태 전이를 검증합니다.
import assert from 'node:assert/strict';
import test from 'node:test';
import serviceReducer, {
  loadServicesFailure,
  loadServicesRequest,
  loadServicesSuccess,
} from '../reducers/service.js';

test('시술 조회 요청·성공·실패 상태를 순서대로 변경한다', () => {
  const loading = serviceReducer(undefined, loadServicesRequest());
  assert.equal(loading.loading, true);

  const success = serviceReducer(loading, loadServicesSuccess([{ id: 1, name: '웨이브 펌' }]));
  assert.deepEqual(success.items, [{ id: 1, name: '웨이브 펌' }]);
  assert.equal(success.loading, false);

  const failure = serviceReducer(success, loadServicesFailure('조회 실패'));
  assert.equal(failure.error, '조회 실패');
  assert.equal(failure.loading, false);
});
