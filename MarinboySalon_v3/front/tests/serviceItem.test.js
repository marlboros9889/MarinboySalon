import { getArchiveLook, getServiceImageUrls, getValidServiceId } from '../utils/serviceItem';

// 메뉴 화면에 전달할 분류·이미지·예약값의 안전한 변환 규칙을 확인합니다.
describe('서비스 메뉴 화면 도구', () => {
  test('메뉴 이름에 따라 아카이브 분류를 선택한다', () => {
    expect(getArchiveLook('남성 커트').category).toBe('CUT & DESIGN');
    expect(getArchiveLook('전체 염색').category).toBe('ARTISAN COLOR');
    expect(getArchiveLook('디자인 펌').category).toBe('WAVE & VOLUME');
  });

  test('등록된 메뉴 이미지는 최대 네 장까지만 화면에 전달한다', () => {
    const serviceItem = {
      imageUrls: ['/1.jpg', '/2.jpg', '/3.jpg', '/4.jpg', '/5.jpg'],
    };

    expect(getServiceImageUrls(serviceItem)).toEqual(['/1.jpg', '/2.jpg', '/3.jpg', '/4.jpg']);
    expect(getServiceImageUrls({})).toEqual(['/images/salon-background.png']);
  });

  test('실제 서비스 id만 예약 폼 값으로 사용한다', () => {
    const serviceItems = [{ id: 1 }, { id: 2 }];

    expect(getValidServiceId(serviceItems, '2')).toBe('2');
    expect(getValidServiceId(serviceItems, '999')).toBe('');
    expect(getValidServiceId(serviceItems, '잘못된 값')).toBe('');
  });
});
