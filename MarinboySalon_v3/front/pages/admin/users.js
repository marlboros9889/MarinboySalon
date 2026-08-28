import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import AdminNavigation from '../../components/AdminNavigation';
import api from '../../api/axios';

/** 관리자가 고객 계정의 권한과 삭제를 관리합니다. */
export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ email: '', password: '', name: '', phone: '' });
  const [editingUserId, setEditingUserId] = useState(null);
  const loadUsers = async () => { try { setUsers((await api.get('/api/admin/users')).data); } catch (e) { setError(e.response?.data?.message || '계정을 불러오지 못했습니다.'); } };
  useEffect(() => { loadUsers(); }, []);
  const changeRole = async (user, role) => { try { await api.patch(`/api/admin/users/${user.id}/role`, { role }); await loadUsers(); } catch (e) { setError(e.response?.data?.message || '권한 변경에 실패했습니다.'); } };
  const removeUser = async (user) => { if (!window.confirm(`${user.name} 계정을 삭제할까요?`)) return; try { await api.delete(`/api/admin/users/${user.id}`); await loadUsers(); } catch (e) { setError(e.response?.data?.message || '계정 삭제에 실패했습니다.'); } };
  const saveAdmin = async (event) => { event.preventDefault(); try { if (editingUserId) await api.patch(`/api/admin/users/${editingUserId}`, form); else await api.post('/api/admin/users', form); setForm({ email: '', password: '', name: '', phone: '' }); setEditingUserId(null); await loadUsers(); } catch (e) { setError(e.response?.data?.message || '관리자 계정 저장에 실패했습니다.'); } };
  const editUser = (user) => { setEditingUserId(user.id); setForm({ email: user.email, password: '', name: user.name, phone: user.phone }); };
  return <AppLayout><section className="page-section container"><header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="heading-text">계정 관리</h1></header><AdminNavigation />{error && <p className="error-message">{error}</p>}<form className="admin-inline-form" onSubmit={saveAdmin}><label className="admin-field">이메일<input required type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label><label className="admin-field">비밀번호{!editingUserId && <input required minLength="6" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />}</label><label className="admin-field">이름<input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label><label className="admin-field">연락처<input required value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></label><div className="admin-form-actions"><button className="primary-button">{editingUserId ? '관리자 수정 저장' : '관리자 계정 생성'}</button>{editingUserId && <button type="button" className="secondary-button" onClick={() => { setEditingUserId(null); setForm({ email: '', password: '', name: '', phone: '' }); }}>취소</button>}</div></form><div className="table-responsive paper-table-wrap"><table className="table"><thead><tr><th>이름</th><th>이메일</th><th>권한</th><th>관리</th></tr></thead><tbody>{users.map((user) => <tr key={user.id}><td>{user.name}</td><td>{user.email}</td><td>{user.role === 'ADMIN' ? '관리자' : '고객'}</td><td><button className="secondary-button" onClick={() => editUser(user)}>수정</button> <button className="secondary-button" onClick={() => changeRole(user, user.role === 'ADMIN' ? 'CUSTOMER' : 'ADMIN')}>{user.role === 'ADMIN' ? '관리자 해제' : '관리자 지정'}</button> <button className="danger-button" onClick={() => removeUser(user)}>삭제</button></td></tr>)}</tbody></table></div></section></AppLayout>;
}
