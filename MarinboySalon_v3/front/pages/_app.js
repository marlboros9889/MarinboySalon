import 'bootstrap/dist/css/bootstrap.min.css';
import { Noto_Sans_KR, Playfair_Display } from 'next/font/google';
import '../styles/global.css';
import '../styles/services.css';
import { wrapper } from '../store/configureStore';

const playfairDisplay = Playfair_Display({
  subsets: ['latin'],
  weight: ['400', '600', '800'],
  style: ['normal', 'italic'],
  display: 'swap',
  variable: '--font-playfair-display',
});

const notoSansKr = Noto_Sans_KR({
  weight: ['300', '400', '500', '700'],
  display: 'swap',
  preload: false,
  variable: '--font-noto-sans-kr',
});

function MarinboySalonApp({ Component, pageProps }) {
  return (
    <div className={`${playfairDisplay.variable} ${notoSansKr.variable} app-font-root`}>
      <Component {...pageProps} />
    </div>
  );
}

export default wrapper.withRedux(MarinboySalonApp);
