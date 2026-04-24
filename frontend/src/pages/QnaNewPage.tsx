import { FormEvent, useState } from 'react';
import { createSupportTicket } from '../api/app';
import { getErrorMessage } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';

function QnaNewPage() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('문의 내용을 작성한 뒤 등록해 주세요.');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');

    try {
      const response = await createSupportTicket({ title, content });
      setResult('success');
      setMessage(`문의가 등록되었습니다. 접수 번호: ${response.id} · 상태: ${response.status}`);
      setTitle('');
      setContent('');
    } catch (error) {
      setResult('error');
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="HELP DESK" title="1:1 문의 등록" description="운영진에게 문의할 내용을 작성합니다." />
      <section className="panel qna-form-panel">
        <form className="qna-form" onSubmit={submit}>
          <label htmlFor="qna-title">제목</label>
          <input id="qna-title" onChange={(event) => setTitle(event.target.value)} required value={title} />
          <label htmlFor="qna-content">내용</label>
          <textarea id="qna-content" onChange={(event) => setContent(event.target.value)} required rows={8} value={content} />
          <button className="primary-action" disabled={submitting} type="submit">
            {submitting ? '등록 중' : '등록'}
          </button>
        </form>
        <div className="check-result" aria-live="polite">
          <StatusPill tone={result === 'success' ? 'green' : result === 'error' ? 'red' : 'gray'}>
            {result === 'success' ? '등록완료' : result === 'error' ? '오류' : '대기'}
          </StatusPill>
          <p>{message}</p>
        </div>
      </section>
    </section>
  );
}

export default QnaNewPage;
