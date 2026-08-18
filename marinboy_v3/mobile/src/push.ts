import Constants from 'expo-constants';
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';

Notifications.setNotificationHandler({
  handleNotification: async () => ({ shouldShowBanner: true, shouldShowList: true, shouldPlaySound: true, shouldSetBadge: true }),
});

export async function getPushRegistration() {
  if (!Device.isDevice) throw new Error('푸시 알림은 실제 기기 또는 지원되는 에뮬레이터에서 설정해 주세요.');
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('reservation', { name: '새 예약 알림', importance: Notifications.AndroidImportance.MAX, vibrationPattern: [0, 250, 250, 250] });
  }
  const current = await Notifications.getPermissionsAsync();
  const permission = current.status === 'granted' ? current.status : (await Notifications.requestPermissionsAsync()).status;
  if (permission !== 'granted') throw new Error('알림 권한이 허용되지 않았습니다.');
  const projectId = process.env.EXPO_PUBLIC_EAS_PROJECT_ID || Constants.easConfig?.projectId || Constants.expoConfig?.extra?.eas?.projectId;
  if (!projectId) throw new Error('EXPO_PUBLIC_EAS_PROJECT_ID 설정이 필요합니다.');
  return { token: (await Notifications.getExpoPushTokenAsync({ projectId })).data, platform: Platform.OS };
}
