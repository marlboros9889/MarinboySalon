// frontend/tests/serviceSaga.test.js: Saga가 API 결과를 성공 액션으로 전달하는지 검증합니다.
import assert from 'node:assert/strict';
import test from 'node:test';
import { call, put } from 'redux-saga/effects';
import { loadServicesSuccess } from '../reducers/service.js';
import { fetchServices, loadServices } from '../sagas/service.js';

test('시술 Saga는 API를 호출하고 성공 결과를 Reducer로 전달한다', () => {
  const generator = loadServices();
  assert.deepEqual(generator.next().value, call(fetchServices));

  const services = [{ id: 1, name: '웨이브 펌' }];
  assert.deepEqual(generator.next(services).value, put(loadServicesSuccess(services)));
  assert.equal(generator.next().done, true);
});
