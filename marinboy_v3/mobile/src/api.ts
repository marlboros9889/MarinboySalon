import type { User } from 'firebase/auth';

export type Reservation = { id: number; customerName: string; customerPhone?: string; serviceName: string; reservationDateTime: string; status: string };
export type ReservationNotification = { id: number; reservationId: number; message: string; read: boolean; createdAt: string; customerName: string; serviceName: string; reservationDateTime: string };

const API_URL = (process.env.EXPO_PUBLIC_API_URL || 'http://10.0.2.2:8082').replace(/\/$/, '');

async function request<T>(user: User, path: string, init?: RequestInit): Promise<T> {
  const idToken = await user.getIdToken();
  const response = await fetch(`${API_URL}${path}`, { ...init, headers: { Authorization: `Bearer ${idToken}`, 'Content-Type': 'application/json', ...init?.headers } });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || `서버 요청 실패 (${response.status})`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export function loadDashboard(user: User) {
  return Promise.all([
    request<{ items: Reservation[] }>(user, '/api/mobile/admin/reservations?size=20'),
    request<ReservationNotification[]>(user, '/api/mobile/admin/notifications'),
  ]);
}

export function registerDevice(user: User, pushToken: string, platform: string) {
  return request<void>(user, '/api/mobile/admin/devices', { method: 'POST', body: JSON.stringify({ pushToken, platform }) });
}
