import { FormEvent, useState } from 'react';
import { createPost } from '../api/boards';
import { getErrorMessage } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { BoardCode } from '../types';

interface BoardPostWritePageProps {
  boardCode?: BoardCode;
  detailPathBase?: string;
  title?: string;
}

function BoardPostWritePage({ boardCode = 'free', detailPathBase = '/community/free', title: pageTitle = '자유게시판 글쓰기' }: BoardPostWritePageProps) {
  const [postTitle, setPostTitle] = useState('');
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState(boardCode === 'anonymous' ? '작성자 정보는 익명으로 표시됩니다.' : '자유게시판에 공유할 내용을 작성해 주세요.');
  const [createdPostId, setCreatedPostId] = useState<number>();

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');
    setCreatedPostId(undefined);

    try {
      const response = await createPost(boardCode, { title: postTitle, content });
      setResult('success');
      setMessage(`게시글이 등록되었습니다. 글 번호: ${response.id}`);
      setCreatedPostId(response.id);
      setPostTitle('');
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
      <PageHeader eyebrow="COMMUNITY" title={pageTitle} description={boardCode === 'anonymous' ? '작성자 정보가 노출되지 않는 게시글을 작성합니다.' : '교육생들과 공유할 게시글을 작성합니다.'} />
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submit}>
          <label htmlFor={`${boardCode}-title`}>제목</label>
          <input id={`${boardCode}-title`} onChange={(event) => setPostTitle(event.target.value)} required value={postTitle} />
          <label htmlFor={`${boardCode}-content`}>내용</label>
          <textarea id={`${boardCode}-content`} onChange={(event) => setContent(event.target.value)} required rows={10} value={content} />
          <button className="primary-action" disabled={submitting} type="submit">
            {submitting ? '등록 중' : '등록'}
          </button>
        </form>
        <FormMessage createdPostId={createdPostId} detailPathBase={detailPathBase} message={message} result={result} />
      </section>
    </section>
  );
}

function FormMessage({
  createdPostId,
  detailPathBase,
  message,
  result,
}: {
  createdPostId?: number;
  detailPathBase: string;
  message: string;
  result: 'idle' | 'success' | 'error';
}) {
  return (
    <div className="check-result" aria-live="polite">
      <StatusPill tone={result === 'success' ? 'green' : result === 'error' ? 'red' : 'gray'}>
        {result === 'success' ? '완료' : result === 'error' ? '오류' : '대기'}
      </StatusPill>
      <p>{message}</p>
      {createdPostId ? <a className="ghost-button" href={`${detailPathBase}/${createdPostId}`}>작성한 글 보기</a> : null}
    </div>
  );
}

export default BoardPostWritePage;
