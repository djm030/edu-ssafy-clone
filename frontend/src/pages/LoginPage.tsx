import { FormEvent, useEffect, useState } from 'react';
import { getErrorMessage } from '../api/client';
import { login } from '../api/app';
import type { UserProfile } from '../types';

interface LoginPageProps {
  message?: string;
  onLogin: (user: UserProfile) => void;
}

function LoginPage({ message: initialMessage, onLogin }: LoginPageProps) {
  const [email, setEmail] = useState(() => localStorage.getItem('eduSSAFY.email') || 'student@ssafy.com');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState(initialMessage || '세션이 만료되었거나 로그인이 필요한 경우 다시 로그인해 주세요.');
  const [credentialHint, setCredentialHint] = useState('발급된 교육생 계정 정보로 로그인하세요.');

  useEffect(() => {
    if (initialMessage) setMessage(initialMessage);
  }, [initialMessage]);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setMessage('');

    try {
      const response = await login(email, password);
      if (remember) localStorage.setItem('eduSSAFY.email', email);
      setCredentialHint(`${response.user.name} 계정으로 로그인되었습니다.`);
      onLogin(response.user);
    } catch (error) {
      setMessage(getErrorMessage(error));
      setCredentialHint('등록된 사용자만 로그인할 수 있습니다. 계정 정보는 운영자에게 문의하세요.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="login-page">
      <section className="login-panel" aria-labelledby="login-title">
        <div>
          <p className="eyebrow">eduSSAFY</p>
          <h1 id="login-title">로그인</h1>
          <p>교육생 포털에 접속하려면 계정 정보를 입력하세요.</p>
          <p className="form-message">{credentialHint}</p>
        </div>

        {message ? <div className="inline-alert">{message}</div> : null}

        <form className="login-form" onSubmit={submit}>
          <label htmlFor="email">이메일 또는 아이디</label>
          <input id="email" onChange={(event) => setEmail(event.target.value)} required value={email} />

          <label htmlFor="password">비밀번호</label>
          <input id="password" onChange={(event) => setPassword(event.target.value)} required type="password" value={password} />

          <div className="form-row">
            <label className="checkbox-label">
              <input checked={remember} onChange={(event) => setRemember(event.target.checked)} type="checkbox" />
              아이디 저장
            </label>
            <a href="/help/password">비밀번호 찾기</a>
          </div>

          <button className="primary-action" disabled={submitting} type="submit">
            {submitting ? '로그인 중' : '로그인'}
          </button>
        </form>
      </section>
    </main>
  );
}

export default LoginPage;
