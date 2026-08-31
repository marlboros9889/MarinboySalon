import { loadServiceItemsForServer } from '../server/serviceItemServer';

describe('시술 메뉴 SSR 조회', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  test('서버 렌더링 전에 메뉴 데이터를 반환한다', async () => {
    const serviceItems = [{ id: 1, name: '남성 커트' }];
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => serviceItems,
    });

    await expect(loadServiceItemsForServer()).resolves.toEqual({ serviceItems, error: null });
    expect(global.fetch).toHaveBeenCalledWith('http://localhost:8082/api/service-items');
  });

  test('백엔드 오류는 직렬화 가능한 오류 문구로 바꾼다', async () => {
    global.fetch = jest.fn().mockResolvedValue({ ok: false, status: 503 });

    await expect(loadServiceItemsForServer()).resolves.toEqual({
      serviceItems: [],
      error: '시술 메뉴를 불러오지 못했습니다.',
    });
  });
});
