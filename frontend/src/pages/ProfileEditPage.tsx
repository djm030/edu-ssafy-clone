import { FormEvent, useState } from 'react';
import { updateProfile } from '../api/app';
import { getErrorMessage } from '../api/client';
import { mockUser } from '../data/mockData';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';

function ProfileEditPage() {
  const [name, setName] = useState(mockUser.name);
  const [email, setEmail] = useState(mockUser.email);
  const [trackName, setTrackName] = useState(mockUser.trackName);
  const [statusMessage, setStatusMessage] = useState('프로젝트 진행 중');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('수정할 프로필 정보를 입력해 주세요.');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');

    try {
      const response = await updateProfile({ name, email, trackName, statusMessage });
      setResult('success');
      setMessage(`${response.user.name}님의 회원정보가 저장되었습니다.`);
    } catch (error) {
      setResult('error');
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="PROFILE" title="회원정보 수정" description="프로필 기본 정보를 수정합니다." />
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submit}>
          <label htmlFor="profile-name">이름</label>
          <input id="profile-name" onChange={(event) => setName(event.target.value)} required value={name} />
          <label htmlFor="profile-email">이메일</label>
          <input id="profile-email" onChange={(event) => setEmail(event.target.value)} required type="email" value={email} />
          <label htmlFor="profile-track">트랙</label>
          <input id="profile-track" onChange={(event) => setTrackName(event.target.value)} required value={trackName} />
          <label htmlFor="profile-status">상태 메시지</label>
          <textarea id="profile-status" onChange={(event) => setStatusMessage(event.target.value)} rows={4} value={statusMessage} />
          <button className="primary-action" disabled={submitting} type="submit">{submitting ? '저장 중' : '저장'}</button>
        </form>
        <div className="check-result" aria-live="polite">
          <StatusPill tone={result === 'success' ? 'green' : result === 'error' ? 'red' : 'gray'}>{result === 'success' ? '저장완료' : result === 'error' ? '오류' : '대기'}</StatusPill>
          <p>{message}</p>
        </div>
      </section>
    </section>
  );
}

export default ProfileEditPage;
