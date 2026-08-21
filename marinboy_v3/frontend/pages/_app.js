// frontend/pages/_app.js: 전 페이지에 Redux Store와 공통 스타일을 적용합니다.
import 'bootstrap/dist/css/bootstrap.min.css';
import '../styles/globals.css';
import '../styles/home.css';
import { Provider } from 'react-redux';
import { wrapper } from '../store/configureStore';
import Head from 'next/head';

export default function MarinboyApp({ Component, ...rest }) {
  // 최신 next-redux-wrapper 방식으로 SSR Store를 React Provider에 직접 연결합니다.
  const { store, props } = wrapper.useWrappedStore(rest);
  return <Provider store={store}><Head><link rel="icon" href="/favicon.svg" type="image/svg+xml" /></Head><Component {...props.pageProps} /></Provider>;
}
