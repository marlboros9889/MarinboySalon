import 'bootstrap/dist/css/bootstrap.min.css';
import '../styles/global.css';
import '../styles/services.css';
import { wrapper } from '../store/configureStore';

function MarinboySalonApp({ Component, pageProps }) {
  return <Component {...pageProps} />;
}

export default wrapper.withRedux(MarinboySalonApp);
