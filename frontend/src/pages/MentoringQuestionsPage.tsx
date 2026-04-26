import { FormEvent, useEffect, useState } from 'react';
import { answerMentoringQuestion, closeMentoringQuestion, createMentoringQuestion, getMentoringQuestion, getMentoringQuestions } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, MentoringQuestionDraft, MentoringQuestionItem, PageMeta } from '../types';

function MentoringQuestionsPage({ canAnswer = false, questionId, mode }: { canAnswer?: boolean; questionId?: number; mode?: 'new' }) {
  if (mode === 'new') return <MentoringQuestionForm />;
  if (questionId) return <MentoringQuestionDetail canAnswer={canAnswer} questionId={questionId} />;
  return <MentoringQuestionList />;
}

function MentoringQuestionList() {
  const [questions, setQuestions] = useState<MentoringQuestionItem[]>([]);
  const [page, setPage] = useState<PageMeta>({ page: 1, size: 20, totalItems: 0, totalPages: 0 });
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringQuestions({ keyword, page: page.page, size: page.size })
      .then((response) => {
        if (ignore) return;
        setQuestions(response.items);
        setPage(response.page);
        setLoadState(response.items.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [keyword, page.page, page.size]);

  const submitSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setPage((current) => ({ ...current, page: 1 }));
    setKeyword(keywordInput.trim());
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="멘토링 Q&A" description="현업 멘토에게 커리어, 학습, 면접 질문을 남기고 답변 상태를 확인합니다." />
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submitSearch}>
          <label htmlFor="mentoring-question-keyword">질문 검색</label>
          <input id="mentoring-question-keyword" onChange={(event) => setKeywordInput(event.target.value)} placeholder="제목, 내용, 카테고리 검색" value={keywordInput} />
          <button className="primary-action" type="submit">검색</button>
          <a className="ghost-button" href="/mentoring/questions/new">질문 작성</a>
        </form>
      </section>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="멘토링 질문을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="멘토링 질문이 없습니다." message="첫 질문을 작성하거나 다른 검색어를 입력해 주세요." /> : null}
      {loadState === 'loaded' ? <QuestionCards questions={questions} /> : null}
    </section>
  );
}

function QuestionCards({ questions }: { questions: MentoringQuestionItem[] }) {
  return (
    <section className="grid-list">
      {questions.map((question) => (
        <article className="panel card" key={question.id}>
          <StatusPill tone={statusTone(question.status)}>{statusLabel(question.status)}</StatusPill>
          <h2><a href={`/mentoring/questions/${question.id}`}>{question.title}</a></h2>
          <p>{question.summary || question.content || '멘토 답변을 기다리는 질문입니다.'}</p>
          <span className="muted">{question.category} · {question.authorName} · 답변 {question.answerCount.toLocaleString('ko-KR')}개 · {formatDate(question.createdAt)}</span>
        </article>
      ))}
    </section>
  );
}

function MentoringQuestionForm() {
  const [draft, setDraft] = useState<MentoringQuestionDraft>({ title: '', content: '', anonymousAllowed: false });
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage('');
    createMentoringQuestion(draft)
      .then((question) => {
        window.location.href = `/mentoring/questions/${question.id}`;
      })
      .catch((error) => setErrorMessage(getErrorMessage(error)))
      .finally(() => setSubmitting(false));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="멘토링 질문 작성" description="질문은 로그인 사용자 기준으로 저장되며, 익명 공개 여부를 선택할 수 있습니다." />
      <form className="panel stack-form" onSubmit={submit}>
        <label htmlFor="mentoring-question-title">제목</label>
        <input id="mentoring-question-title" maxLength={240} required value={draft.title} onChange={(event) => setDraft((current) => ({ ...current, title: event.target.value }))} />
        <label htmlFor="mentoring-question-content">질문 내용</label>
        <textarea id="mentoring-question-content" maxLength={3800} required rows={8} value={draft.content} onChange={(event) => setDraft((current) => ({ ...current, content: event.target.value }))} />
        <label className="inline-check">
          <input checked={draft.anonymousAllowed} type="checkbox" onChange={(event) => setDraft((current) => ({ ...current, anonymousAllowed: event.target.checked }))} />
          목록과 상세에서 익명 질문자로 표시
        </label>
        {errorMessage ? <DataState title="질문을 저장하지 못했습니다." message={errorMessage} /> : null}
        <button className="primary-action" disabled={submitting} type="submit">{submitting ? '저장 중...' : '질문 등록'}</button>
      </form>
    </section>
  );
}

function MentoringQuestionDetail({ canAnswer, questionId }: { canAnswer: boolean; questionId: number }) {
  const [question, setQuestion] = useState<MentoringQuestionItem>();
  const [answer, setAnswer] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringQuestion(questionId)
      .then((response) => {
        if (ignore) return;
        setQuestion(response);
        setLoadState('loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [questionId]);

  const submitAnswer = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setActionMessage('');
    answerMentoringQuestion(questionId, answer)
      .then((updated) => {
        setQuestion(updated);
        setAnswer('');
        setActionMessage('멘토 답변이 저장되었습니다.');
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  const closeQuestion = () => {
    setActionMessage('');
    closeMentoringQuestion(questionId)
      .then((updated) => {
        setQuestion(updated);
        setActionMessage('질문을 마감했습니다.');
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="멘토링 Q&A 상세" description="질문 내용과 멘토 답변, 마감 상태를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="멘토링 질문을 불러오지 못했습니다." message={errorMessage} /> : null}
      {question ? (
        <article className="panel article-detail">
          <StatusPill tone={statusTone(question.status)}>{statusLabel(question.status)}</StatusPill>
          <h2>{question.title}</h2>
          <p className="muted">{question.category} · {question.authorName} · {formatDate(question.createdAt)}</p>
          <p>{question.content}</p>
          <section className="stack-form">
            <h3>멘토 답변 {question.answerCount.toLocaleString('ko-KR')}개</h3>
            {question.answers?.length ? question.answers.map((item) => (
              <div className="panel" key={item.id}>
                <strong>{item.mentorName}</strong>
                <p>{item.content}</p>
                <span className="muted">{formatDate(item.createdAt)}</span>
              </div>
            )) : <DataState title="아직 답변이 없습니다." message="멘토 답변이 등록되면 이 영역에 표시됩니다." />}
          </section>
          {canAnswer ? (
            <form className="stack-form" onSubmit={submitAnswer}>
              <label htmlFor="mentoring-answer">멘토 답변 작성</label>
              <textarea id="mentoring-answer" maxLength={4000} required rows={4} value={answer} onChange={(event) => setAnswer(event.target.value)} />
              <button className="primary-action" type="submit">답변 등록</button>
            </form>
          ) : (
            <DataState title="답변 권한이 없습니다." message="멘토 또는 운영자 계정으로 로그인하면 답변을 등록할 수 있습니다." />
          )}
          <button className="ghost-button" onClick={closeQuestion} type="button">질문 마감</button>
          {actionMessage ? <p className="muted">{actionMessage}</p> : null}
          <a className="ghost-button" href="/mentoring/questions">목록으로</a>
        </article>
      ) : null}
    </section>
  );
}

function statusLabel(status: MentoringQuestionItem['status']): string {
  if (status === 'CLOSED') return '마감';
  if (status === 'ANSWERED') return '답변 완료';
  return '답변 대기';
}

function statusTone(status: MentoringQuestionItem['status']): 'blue' | 'green' | 'gray' {
  if (status === 'CLOSED') return 'gray';
  if (status === 'ANSWERED') return 'green';
  return 'blue';
}

function formatDate(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(date);
}

export default MentoringQuestionsPage;
