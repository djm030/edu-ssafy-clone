import { useEffect, useState } from 'react';
import { deleteBookmark, getBookmarks } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { BookmarkItem, BookmarkSummary, BookmarkTargetType, LoadState } from '../types';

const targetFilters = [
  { label: '전체', value: 'all' },
  { label: '학습자료', value: 'material' },
  { label: '이러닝', value: 'elearning' },
  { label: '다시보기', value: 'replay' },
] as const;

const targetLabels = {
  elearning: { label: '이러닝', tone: 'yellow' },
  material: { label: '학습자료', tone: 'blue' },
  replay: { label: '다시보기', tone: 'green' },
} as const;

function BookmarksPage() {
  const [items, setItems] = useState<BookmarkItem[]>([]);
  const [summary, setSummary] = useState<BookmarkSummary>();
  const [targetType, setTargetType] = useState<(typeof targetFilters)[number]['value']>('all');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [mutationMessage, setMutationMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getBookmarks({ size: 50, targetType })
      .then((response) => {
        if (ignore) return;
        setItems(response.items);
        setSummary(response.summary);
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
  }, [targetType]);

  const removeBookmark = (bookmarkId: number) => {
    setMutationMessage('찜을 해제하는 중입니다.');
    deleteBookmark(bookmarkId)
      .then(() => {
        const removed = items.find((item) => item.id === bookmarkId);
        const nextItems = items.filter((item) => item.id !== bookmarkId);
        setItems(nextItems);
        if (removed) {
          setSummary((current) => decrementBookmarkSummary(current, removed.targetType));
        }
        setLoadState(nextItems.length ? 'loaded' : 'empty');
        setMutationMessage('찜이 해제되었습니다.');
      })
      .catch((error) => setMutationMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="찜한 목록" description="저장한 학습자료, 이러닝, 강의 다시보기를 한 곳에서 관리합니다." />
      <div className="filter-bar">
        <div className="category-strip">
          {targetFilters.map((option) => (
            <button className={targetType === option.value ? 'category-chip active' : 'category-chip'} key={option.value} onClick={() => setTargetType(option.value)} type="button">
              {option.label} <span>{summaryCount(summary, option.value)}</span>
            </button>
          ))}
        </div>
      </div>
      {summary ? <BookmarkSummaryPanel summary={summary} /> : null}
      {mutationMessage ? <p className="helper-text" role="status">{mutationMessage}</p> : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="찜한 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="찜한 콘텐츠가 없습니다." message="학습자료나 이러닝 상세에서 찜을 추가해 보세요." /> : null}
      {loadState === 'loaded' ? <BookmarkList items={items} onDelete={removeBookmark} /> : null}
    </section>
  );
}


function BookmarkSummaryPanel({ summary }: { summary: BookmarkSummary }) {
  return (
    <div className="bookmark-summary-grid" aria-label="찜한 목록 유형별 요약">
      <SummaryCard title="전체" value={`${summary.totalCount}개`} />
      <SummaryCard title="학습자료" value={`${summary.materialCount}개`} />
      <SummaryCard title="이러닝" value={`${summary.elearningCount}개`} />
      <SummaryCard title="다시보기" value={`${summary.replayCount}개`} />
    </div>
  );
}

function SummaryCard({ title, value }: { title: string; value: string }) {
  return (
    <section className="stat-card compact">
      <span>{title}</span>
      <strong>{value}</strong>
    </section>
  );
}

function summaryCount(summary: BookmarkSummary | undefined, targetType: (typeof targetFilters)[number]['value']) {
  if (!summary) return '-';
  if (targetType === 'all') return summary.totalCount;
  if (targetType === 'material') return summary.materialCount;
  if (targetType === 'elearning') return summary.elearningCount;
  return summary.replayCount;
}

function decrementBookmarkSummary(summary: BookmarkSummary | undefined, targetType: BookmarkTargetType): BookmarkSummary | undefined {
  if (!summary) return summary;
  return {
    ...summary,
    totalCount: Math.max(summary.totalCount - 1, 0),
    materialCount: targetType === 'material' ? Math.max(summary.materialCount - 1, 0) : summary.materialCount,
    elearningCount: targetType === 'elearning' ? Math.max(summary.elearningCount - 1, 0) : summary.elearningCount,
    replayCount: targetType === 'replay' ? Math.max(summary.replayCount - 1, 0) : summary.replayCount,
  };
}

function BookmarkList({ items, onDelete }: { items: BookmarkItem[]; onDelete: (bookmarkId: number) => void }) {
  return (
    <div className="card-list" aria-label="찜한 목록">
      {items.map((item) => {
        const target = targetLabels[item.targetType];
        return (
          <article className="list-card" key={item.id}>
            <div>
              <p className="eyebrow">{formatDate(item.createdAt)}</p>
              <h2>{item.title}</h2>
              <p>{item.description || '저장된 설명이 없습니다.'}</p>
            </div>
            <StatusPill tone={target.tone}>{target.label}</StatusPill>
            <div className="action-row">
              <a className="ghost-button" href={item.targetUrl || '/learning/materials'}>이동</a>
              <button className="ghost-button" onClick={() => onDelete(item.id)} type="button">찜 해제</button>
            </div>
          </article>
        );
      })}
    </div>
  );
}

function formatDate(value?: string | null): string {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

export default BookmarksPage;
