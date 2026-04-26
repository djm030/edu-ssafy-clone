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
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [confirmTarget, setConfirmTarget] = useState<BookmarkItem | null>(null);

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

  const requestRemoveBookmark = (bookmarkId: number) => {
    const target = items.find((item) => item.id === bookmarkId) ?? null;
    setConfirmTarget(target);
    setMutationMessage(target ? `${target.title} 찜 해제를 확인해 주세요.` : '삭제할 찜 항목을 찾지 못했습니다.');
  };

  const confirmRemoveBookmark = () => {
    if (!confirmTarget || deletingId !== null) return;
    const target = confirmTarget;
    setDeletingId(target.id);
    setMutationMessage('찜을 해제하는 중입니다.');
    deleteBookmark(target.id)
      .then(() => {
        const nextItems = items.filter((item) => item.id !== target.id);
        setItems(nextItems);
        setSummary((current) => decrementBookmarkSummary(current, target.targetType));
        setLoadState(nextItems.length ? 'loaded' : 'empty');
        setMutationMessage('찜이 해제되었습니다.');
        setConfirmTarget(null);
      })
      .catch((error) => setMutationMessage(getErrorMessage(error)))
      .finally(() => setDeletingId(null));
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
      {confirmTarget ? (
        <BookmarkDeleteConfirm
          disabled={deletingId !== null}
          item={confirmTarget}
          onCancel={() => { setConfirmTarget(null); setMutationMessage('찜 해제를 취소했습니다.'); }}
          onConfirm={confirmRemoveBookmark}
        />
      ) : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="찜한 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="찜한 콘텐츠가 없습니다." message="학습자료나 이러닝 상세에서 찜을 추가해 보세요." /> : null}
      {loadState === 'loaded' ? <BookmarkList items={items} deletingId={deletingId} onDelete={requestRemoveBookmark} /> : null}
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

function BookmarkDeleteConfirm({ disabled, item, onCancel, onConfirm }: { disabled: boolean; item: BookmarkItem; onCancel: () => void; onConfirm: () => void }) {
  const target = targetLabels[item.targetType];

  return (
    <section className="bookmark-confirm-panel" aria-label="찜 해제 확인">
      <div>
        <StatusPill tone={target.tone}>{target.label}</StatusPill>
        <strong>{item.title}</strong>
        <p>이 항목의 찜을 해제하면 현재 탭 목록에서 제거됩니다. 실패하면 기존 목록을 유지합니다.</p>
      </div>
      <div className="button-row">
        <button className="ghost-button" disabled={disabled} onClick={onCancel} type="button">취소</button>
        <button className="primary-action" disabled={disabled} onClick={onConfirm} type="button">찜 해제 확인</button>
      </div>
    </section>
  );
}

function BookmarkList({ items, deletingId, onDelete }: { items: BookmarkItem[]; deletingId: number | null; onDelete: (bookmarkId: number) => void }) {
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
              <button className="ghost-button" disabled={deletingId !== null} onClick={() => onDelete(item.id)} type="button">
                {deletingId === item.id ? '해제 중' : '찜 해제'}
              </button>
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
