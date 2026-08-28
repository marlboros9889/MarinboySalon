import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import AdminNavigation from '../../components/AdminNavigation';
import api from '../../api/axios';

/** 관리자가 고객 계정의 권한과 삭제를 관리합니다. */
export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const loadUsers = async () => { try { setUsers((await api.get('/api/admin/users')).data); } catch (e) { setError(e.response?.data?.message || '계정을 불러오지 못했습니다.'); } };
  useEffect(() => { loadUsers(); }, []);
  const changeRole = async (user, role) => { try { await api.patch(`/api/admin/users/${user.id}/role`, { role }); await loadUsers(); } catch (e) { setError(e.response?.data?.message || '권한 변경에 실패했습니다.'); } };
  const removeUser = async (user) => { if (!window.confirm(`${user.name} 계정을 삭제할까요?`)) return; try { await api.delete(`/api/admin/users/${user.id}`); await loadUsers(); } catch (e) { setError(e.response?.data?.message || '계정 삭제에 실패했습니다.'); } };
  return <AppLayout><section className="page-section container"><header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="heading-text">계정 관리</h1></header><AdminNavigation />{error && <p className="error-message">{error}</p>}<div className="table-responsive paper-table-wrap"><table className="table"><thead><tr><th>이름</th><th>이메일</th><th>권한</th><th>관리</th></tr></thead><tbody>{users.map((user) => <tr key={user.id}><td>{user.name}</td><td>{user.email}</td><td>{user.role === 'ADMIN' ? '관리자' : '고객'}</td><td><button className="secondary-button" onClick={() => changeRole(user, user.role === 'ADMIN' ? 'CUSTOMER' : 'ADMIN')}>{user.role === 'ADMIN' ? '관리자 해제' : '관리자 지정'}</button> <button className="danger-button" onClick={() => removeUser(user)}>삭제</button></td></tr>)}</tbody></table></div></section></AppLayout>;
}
