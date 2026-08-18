import { StatusBar } from 'expo-status-bar';
import { onAuthStateChanged, signInWithEmailAndPassword, signOut, type User } from 'firebase/auth';
import { useEffect, useState } from 'react';
import { ActivityIndicator, FlatList, Pressable, RefreshControl, SafeAreaView, StyleSheet, Text, TextInput, View } from 'react-native';
import { loadDashboard, registerDevice, type Reservation, type ReservationNotification } from './src/api';
import { firebaseAuth, firebaseConfigured } from './src/firebase';
import { getPushRegistration } from './src/push';

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    if (!firebaseAuth) { setInitializing(false); return; }
    return onAuthStateChanged(firebaseAuth, next => { setUser(next); setInitializing(false); });
  }, []);

  if (initializing) return <Loading />;
  return user ? <Dashboard user={user} /> : <Login />;
}

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState(firebaseConfigured ? '' : 'mobile/.env에 Firebase 설정을 입력해 주세요.');
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    if (!firebaseAuth) return;
    setLoading(true); setMessage('');
    try { await signInWithEmailAndPassword(firebaseAuth, email.trim(), password); }
    catch { setMessage('Firebase 관리자 이메일과 비밀번호를 확인해 주세요.'); }
    finally { setLoading(false); }
  };

  return <SafeAreaView style={styles.safe}><View style={styles.loginCard}>
    <Text style={styles.brand}>MARINBOY</Text><Text style={styles.title}>예약 관리자 앱</Text>
    <Text style={styles.subtitle}>새 예약을 모바일에서 바로 확인하세요.</Text>
    <TextInput style={styles.input} autoCapitalize="none" keyboardType="email-address" placeholder="관리자 이메일" value={email} onChangeText={setEmail} />
    <TextInput style={styles.input} secureTextEntry placeholder="비밀번호" value={password} onChangeText={setPassword} />
    {message ? <Text style={styles.error}>{message}</Text> : null}
    <Pressable style={[styles.primaryButton, (!firebaseAuth || loading) && styles.disabled]} disabled={!firebaseAuth || loading} onPress={submit}>
      <Text style={styles.primaryText}>{loading ? '로그인 중...' : 'Firebase 로그인'}</Text>
    </Pressable>
  </View><StatusBar style="dark" /></SafeAreaView>;
}

function Dashboard({ user }: { user: User }) {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [notifications, setNotifications] = useState<ReservationNotification[]>([]);
  const [tab, setTab] = useState<'reservations' | 'notifications'>('reservations');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  const refresh = async () => {
    setLoading(true);
    try {
      const [page, alerts] = await loadDashboard(user);
      setReservations(page.items); setNotifications(alerts); setMessage('');
    } catch (error) { setMessage(error instanceof Error ? error.message : '데이터를 불러오지 못했습니다.'); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    refresh();
    getPushRegistration().then(({ token, platform }) => registerDevice(user, token, platform))
      .catch(error => setMessage(error instanceof Error ? error.message : '푸시 알림 설정에 실패했습니다.'));
  }, [user]);

  const data: Array<Reservation | ReservationNotification> = tab === 'reservations' ? reservations : notifications;
  return <SafeAreaView style={styles.safe}>
    <View style={styles.header}><View><Text style={styles.brand}>MARINBOY</Text><Text style={styles.headerTitle}>예약 운영</Text></View>
      <Pressable onPress={() => firebaseAuth && signOut(firebaseAuth)}><Text style={styles.logout}>로그아웃</Text></Pressable></View>
    <View style={styles.tabs}>
      <Tab active={tab === 'reservations'} label={`예약 ${reservations.length}`} onPress={() => setTab('reservations')} />
      <Tab active={tab === 'notifications'} label={`새 알림 ${notifications.filter(item => !item.read).length}`} onPress={() => setTab('notifications')} />
    </View>
    {message ? <Text style={styles.notice}>{message}</Text> : null}
    <FlatList<Reservation | ReservationNotification> data={data} keyExtractor={item => `${tab}-${item.id}`} contentContainerStyle={styles.list}
      refreshControl={<RefreshControl refreshing={loading} onRefresh={refresh} tintColor="#4f6548" />}
      ListEmptyComponent={loading ? <ActivityIndicator color="#4f6548" /> : <Text style={styles.empty}>표시할 내용이 없습니다.</Text>}
      renderItem={({ item }) => 'status' in item ? <ReservationCard item={item} /> : <NotificationCard item={item} />} />
    <StatusBar style="dark" />
  </SafeAreaView>;
}

function Tab({ active, label, onPress }: { active: boolean; label: string; onPress: () => void }) {
  return <Pressable style={[styles.tab, active && styles.activeTab]} onPress={onPress}><Text style={[styles.tabText, active && styles.activeTabText]}>{label}</Text></Pressable>;
}

function ReservationCard({ item }: { item: Reservation }) {
  return <View style={styles.card}><View style={styles.cardTop}><Text style={styles.customer}>{item.customerName}</Text><Text style={styles.status}>{item.status}</Text></View>
    <Text style={styles.service}>{item.serviceName}</Text><Text style={styles.date}>{formatDate(item.reservationDateTime)}</Text>
    {item.customerPhone ? <Text style={styles.meta}>{item.customerPhone}</Text> : null}</View>;
}

function NotificationCard({ item }: { item: ReservationNotification }) {
  return <View style={[styles.card, !item.read && styles.unreadCard]}><Text style={styles.customer}>{item.message}</Text>
    <Text style={styles.service}>{item.customerName} · {item.serviceName}</Text><Text style={styles.date}>{formatDate(item.reservationDateTime)}</Text></View>;
}

function Loading() { return <SafeAreaView style={styles.safe}><ActivityIndicator style={styles.loader} size="large" color="#4f6548" /></SafeAreaView>; }
function formatDate(value: string) { return value ? new Date(value).toLocaleString('ko-KR') : '-'; }

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#f5f1e8' }, loader: { flex: 1 },
  loginCard: { margin: 24, marginTop: 90, padding: 28, borderRadius: 30, backgroundColor: '#fffdf7', gap: 14, elevation: 4 },
  brand: { color: '#708167', fontSize: 13, fontWeight: '800', letterSpacing: 3 }, title: { color: '#263520', fontSize: 31, fontWeight: '800', marginTop: 4 },
  subtitle: { color: '#6f776b', fontSize: 15, marginBottom: 14 }, input: { backgroundColor: '#f4f4ee', borderWidth: 1, borderColor: '#dce2d7', borderRadius: 16, paddingHorizontal: 17, paddingVertical: 15, fontSize: 16 },
  primaryButton: { backgroundColor: '#4f6548', borderRadius: 16, padding: 17, alignItems: 'center', marginTop: 4 }, primaryText: { color: 'white', fontWeight: '800', fontSize: 16 }, disabled: { opacity: 0.45 }, error: { color: '#a63f3f', lineHeight: 20 },
  header: { paddingHorizontal: 22, paddingTop: 18, paddingBottom: 15, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, headerTitle: { color: '#263520', fontSize: 28, fontWeight: '800' }, logout: { color: '#63705e', fontWeight: '700' },
  tabs: { flexDirection: 'row', gap: 9, paddingHorizontal: 20, paddingBottom: 12 }, tab: { flex: 1, borderRadius: 15, backgroundColor: '#e4e8df', padding: 13, alignItems: 'center' }, activeTab: { backgroundColor: '#4f6548' }, tabText: { color: '#52604e', fontWeight: '800' }, activeTabText: { color: '#fff' },
  notice: { marginHorizontal: 20, marginBottom: 8, color: '#9a5a2c', backgroundColor: '#fff1dc', padding: 12, borderRadius: 12 }, list: { padding: 20, paddingTop: 8, gap: 12, flexGrow: 1 },
  card: { backgroundColor: '#fffdf7', borderRadius: 22, padding: 19, borderWidth: 1, borderColor: '#e4e1d8' }, unreadCard: { borderWidth: 2, borderColor: '#708167', backgroundColor: '#fafff7' }, cardTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  customer: { color: '#263520', fontSize: 17, fontWeight: '800', flexShrink: 1 }, status: { color: '#fff', backgroundColor: '#85967c', borderRadius: 12, paddingHorizontal: 10, paddingVertical: 5, overflow: 'hidden', fontSize: 12, fontWeight: '700' },
  service: { color: '#54604f', fontSize: 15, marginTop: 10 }, date: { color: '#263520', fontSize: 15, fontWeight: '700', marginTop: 7 }, meta: { color: '#81877d', marginTop: 5 }, empty: { textAlign: 'center', color: '#7b8178', marginTop: 80 },
});
