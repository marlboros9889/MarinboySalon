import AsyncStorage from '@react-native-async-storage/async-storage';
import { getApp, getApps, initializeApp } from 'firebase/app';
import * as FirebaseAuthModule from 'firebase/auth';
import { Auth, getAuth, initializeAuth, type Persistence } from 'firebase/auth';

// Metro는 React Native용 Firebase 엔트리를 사용하지만 공용 타입 선언에는 이 함수가 빠져 있습니다.
const getReactNativePersistence = (FirebaseAuthModule as unknown as {
  getReactNativePersistence: (storage: typeof AsyncStorage) => Persistence;
}).getReactNativePersistence;

const firebaseConfig = {
  apiKey: process.env.EXPO_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.EXPO_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.EXPO_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.EXPO_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.EXPO_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.EXPO_PUBLIC_FIREBASE_APP_ID,
};

export const firebaseConfigured = Object.values(firebaseConfig).every(Boolean);

function createAuth(): Auth | null {
  if (!firebaseConfigured) return null;
  const app = getApps().length ? getApp() : initializeApp(firebaseConfig);
  try {
    return initializeAuth(app, { persistence: getReactNativePersistence(AsyncStorage) });
  } catch {
    return getAuth(app);
  }
}

export const firebaseAuth = createAuth();
