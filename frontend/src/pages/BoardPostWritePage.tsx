import { FormEvent, useState } from 'react';
import { createFreePost } from '../api/app';
import { getErrorMessage } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';

function BoardPostWritePage() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('자유게시판에 공유할 내용을 작성해 주세요.');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');

    try {
      const response = await createFreePost({ title, content });
      setResult('success');
      setMessage(`게시글이 등록되었습니다. 글 번호: ${response.id}`);
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
      <PageHeader eyebrow="COMMUNITY" title="자유게시판 글쓰기" description="교육생들과 공유할 게시글을 작성합니다." />
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submit}>
          <label htmlFor="free-title">제목</label>
          <input id="free-title" onChange={(event) => setTitle(event.target.value)} required value={title} />
          <label htmlFor="free-content">내용</label>
          <textarea id="free-content" onChange={(event) => setContent(event.target.value)} required rows={10} value={content} />
          <button className="primary-action" disabled={submitting} type="submit">
            {submitting ? '등록 중' : '등록'}
          </button>
        </form>
        <FormMessage message={message} result={result} />
      </section>
    </section>
  );
}

function FormMessage({ message, result }: { message: string; result: 'idle' | 'success' | 'error' }) {
  return (
    <div className="check-result" aria-live="polite">
      <StatusPill tone={result === 'success' ? 'green' : result === 'error' ? 'red' : 'gray'}>
        {result === 'success' ? '완료' : result === 'error' ? '오류' : '대기'}
      </StatusPill>
      <p>{message}</p>
    </div>
  );
}

export default BoardPostWritePage;
