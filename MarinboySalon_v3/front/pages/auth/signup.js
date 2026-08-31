import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useRouter } from 'next/router';
import AppLayout from '../../components/AppLayout';
import { SIGN_UP_REQUEST } from '../../reducers/authReducer';

// 회원가입 입력값을 관리하고, 성공하면 로그인 화면으로 안내합니다.
export default function Signup() {
  const dispatch = useDispatch();
  const router = useRouter();
  const { signUpLoading, signUpDone, signUpError } = useSelector((state) => state.auth);
  const [form, setForm] = useState({ email: '', password: '', name: '', phone: '' });

  useEffect(() => {
    // 가입 직후에는 비밀번호를 다시 입력받도록 자동 로그인하지 않습니다.
    if (signUpDone) {
      router.push('/auth/login');
    }
  }, [signUpDone, router]);

  const onChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
  };

  const onSubmit = (event) => {
    event.preventDefault();
    dispatch({ type: SIGN_UP_REQUEST, data: form });
  };

  return (
    <AppLayout>
      <section className="auth-section container">
        <form className="paper-form torn-paper-edge" onSubmit={onSubmit}>
          <p className="eyebrow">JOIN US</p>
          <h1 className="heading-text">회원가입</h1>
          <label htmlFor="email">이메일</label>
          <input id="email" name="email" type="email" value={form.email} onChange={onChange} required />
          <label htmlFor="password">비밀번호</label>
          <input id="password" name="password" type="password" minLength="6" value={form.password} onChange={onChange} required />
          <label htmlFor="name">이름</label>
          <input id="name" name="name" value={form.name} onChange={onChange} required />
          <label htmlFor="phone">연락처</label>
          <input id="phone" name="phone" value={form.phone} onChange={onChange} placeholder="010-0000-0000" required />
          {signUpError && <p className="error-message">{signUpError}</p>}
          <button type="submit" className="primary-button" disabled={signUpLoading}>
            {signUpLoading ? '가입 중...' : '회원가입'}
          </button>
        </form>
      </section>
    </AppLayout>
  );
}
