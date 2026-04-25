import { FormEvent, useState } from 'react';
import { checkProfilePassword } from '../api/app';
import { getErrorMessage } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';

interface ProfileCheckPageProps {
  onVerified: () => void;
}

function ProfileCheckPage({ onVerified }: ProfileCheckPageProps) {
  const [password, setPassword] = useState('');
  const [checking, setChecking] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('회원정보 수정 전 비밀번호를 다시 확인합니다.');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setChecking(true);
    setResult('idle');

    try {
      const response = await checkProfilePassword(password);
      setResult(response.verified ? 'success' : 'error');
      setMessage(response.verified ? '비밀번호가 확인되었습니다.' : '비밀번호가 일치하지 않습니다.');
      if (response.verified) onVerified();
    } catch (error) {
      setResult('error');
      setMessage(getErrorMessage(error));
    } finally {
      setChecking(false);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="PROFILE" title="회원정보 확인" description="개인정보 보호를 위해 비밀번호를 한 번 더 입력합니다." />
      <section className="panel profile-check">
        <form className="login-form" onSubmit={submit}>
          <label htmlFor="profile-password">비밀번호</label>
          <input
            id="profile-password"
            onChange={(event) => setPassword(event.target.value)}
            required
            type="password"
            value={password}
          />
          <button className="primary-action" disabled={checking} type="submit">
            {checking ? '확인 중' : '확인'}
          </button>
        </form>
        <div className="check-result" aria-live="polite">
          <StatusPill tone={result === 'success' ? 'green' : result === 'error' ? 'red' : 'gray'}>
            {result === 'success' ? '성공' : result === 'error' ? '확인 필요' : '대기'}
          </StatusPill>
          <p>{message}</p>
        </div>
      </section>
    </section>
  );
}

export default ProfileCheckPage;
