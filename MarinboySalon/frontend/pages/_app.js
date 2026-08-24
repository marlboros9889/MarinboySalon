// frontend/pages/_app.js: 전 페이지에 공통 스타일과 아이콘을 적용합니다.
import '../styles/globals.css';
import '../styles/home.css';
import Head from 'next/head';

export default function MarinboyApp({ Component, pageProps }) {
  return <><Head><link rel="icon" href="/favicon.svg" type="image/svg+xml" /></Head><Component {...pageProps} /></>;
}
