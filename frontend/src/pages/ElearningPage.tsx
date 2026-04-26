import { FormEvent, useEffect, useState } from 'react';
import { getElearningProgress, resumeElearning } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { ElearningProgressItem, LoadState } from '../types';

const statusOptions = [
  { label: '전체', value: 'all' },
  { label: '학습중', value: 'in_progress' },
  { label: '완료', value: 'completed' },
] as const;

const statusLabels = {
  completed: { label: '완료', tone: 'green' },
  in_progress: { label: '학습중', tone: 'yellow' },
  not_started: { label: '미시작', tone: 'gray' },
} as const;

function ElearningPage() {
  const [items, setItems] = useState<ElearningProgressItem[]>([]);
  const [status, setStatus] = useState<(typeof statusOptions)[number]['value']>('all');
  const [keyword, setKeyword] = useState('');
  const [submittedKeyword, setSubmittedKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [resumeMessage, setResumeMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getElearningProgress({ keyword: submittedKeyword, size: 20, status })
      .then((response) => {
        if (ignore) return;
        setItems(response.items);
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
  }, [submittedKeyword, status]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmittedKeyword(keyword.trim());
  };

  const handleResume = (courseId: number) => {
    setResumeMessage('이어보기 이력을 저장하는 중입니다.');
    resumeElearning(courseId)
      .then((response) => {
        setResumeMessage('이어보기 이력이 저장되었습니다. 상세 화면으로 이동할 수 있습니다.');
        const resumeUrl = response.item.resumeUrl || `/mycampus/elearning/${courseId}`;
        window.history.pushState({}, '', resumeUrl);
        window.dispatchEvent(new PopStateEvent('popstate'));
      })
      .catch((error) => setResumeMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="학습중 이러닝" description="진행 중인 이러닝 과정과 최근 학습 상태를 확인합니다." />
      <div className="filter-bar">
        <div className="category-strip">
          {statusOptions.map((option) => (
            <button className={status === option.value ? 'category-chip active' : 'category-chip'} key={option.value} onClick={() => setStatus(option.value)} type="button">
              {option.label}
            </button>
          ))}
        </div>
        <form className="search-form" onSubmit={submit}>
          <input onChange={(event) => setKeyword(event.target.value)} placeholder="과정명 또는 최근 차시 검색" value={keyword} />
          <button type="submit">검색</button>
        </form>
      </div>
      {resumeMessage ? <p className="helper-text" role="status">{resumeMessage}</p> : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="학습중 이러닝을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="학습중인 이러닝이 없습니다." message="상태 필터 또는 검색어를 바꿔 보세요." /> : null}
      {loadState === 'loaded' ? <ElearningList items={items} onResume={handleResume} /> : null}
    </section>
  );
}

function ElearningList({ items, onResume }: { items: ElearningProgressItem[]; onResume: (courseId: number) => void }) {
  return (
    <div className="card-list" aria-label="학습중 이러닝 목록">
      {items.map((item) => {
        const status = statusLabels[item.status];
        return (
          <article className="list-card" key={item.courseId}>
            <div>
              <p className="eyebrow">{[item.category, item.provider].filter(Boolean).join(' · ') || 'SSAFY e-Learning'}</p>
              <h2>{item.title}</h2>
              <p>{item.description || '등록된 설명이 없습니다.'}</p>
              <div className="progress-track" aria-label={`${item.title} 진행률 ${item.progressPercent}%`}>
                <span style={{ width: `${item.progressPercent}%` }} />
              </div>
              <p>
                {item.completedLessons}/{item.totalLessons}차시 완료 · 최근 차시: {item.lastLessonTitle || '-'} · 최근 학습일: {formatDateTime(item.lastLearningAt)}
              </p>
            </div>
            <StatusPill tone={status.tone}>{status.label}</StatusPill>
            <div className="action-row">
              <a className="ghost-button" href={`/mycampus/elearning/${item.courseId}`}>상세</a>
              <button className="primary-action" onClick={() => onResume(item.courseId)} type="button">이어보기</button>
            </div>
          </article>
        );
      })}
    </div>
  );
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  return value.replace('T', ' ').slice(0, 16);
}

export default ElearningPage;
