# Marinboy Frontend

- 실행: `npm run dev` (`http://127.0.0.1:3000`)
- SSR: `pages/index.js`의 `getServerSideProps`
- 상태 흐름: `View → Saga → Reducer → Store → View`
- 기능 분류: `features/auth`, `features/home`, `reducers`, `sagas`, `store`
- Spring API: 기본 `http://127.0.0.1:8082`, 필요 시 `SSR_API_BASE_URL`로 변경
