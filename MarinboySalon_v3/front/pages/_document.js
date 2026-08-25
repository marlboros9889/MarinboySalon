import Document, { Head, Html, Main, NextScript } from 'next/document';

/**
 * 모든 페이지에 UTF-8과 프로젝트 파비콘을 공통 적용합니다.
 */
export default class MarinboyDocument extends Document {
  render() {
    return (
      <Html lang="ko">
        <Head>
          <meta charSet="UTF-8" />
          <link rel="icon" href="/favicon.svg" type="image/svg+xml" />
        </Head>
        <body>
          <Main />
          <NextScript />
        </body>
      </Html>
    );
  }
}
