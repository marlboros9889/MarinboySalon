import { getArchiveLook, getValidServiceId } from '../utils/serviceItem';

describe('서비스 메뉴 화면 도구', () => {
  test('메뉴 이름에 따라 아카이브 분류를 선택한다', () => {
    expect(getArchiveLook('남성 커트').category).toBe('CUT & DESIGN');
    expect(getArchiveLook('전체 염색').category).toBe('ARTISAN COLOR');
    expect(getArchiveLook('디자인 펌').category).toBe('WAVE & VOLUME');
  });

  test('실제 서비스 id만 예약 폼 값으로 사용한다', () => {
    const serviceItems = [{ id: 1 }, { id: 2 }];

    expect(getValidServiceId(serviceItems, '2')).toBe('2');
    expect(getValidServiceId(serviceItems, '999')).toBe('');
    expect(getValidServiceId(serviceItems, '잘못된 값')).toBe('');
  });
});
