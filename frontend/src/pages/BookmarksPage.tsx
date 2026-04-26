import { useEffect, useState } from 'react';
import { deleteBookmark, getBookmarks } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { BookmarkItem, LoadState } from '../types';

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
        const nextItems = items.filter((item) => item.id !== bookmarkId);
        setItems(nextItems);
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
              {option.label}
            </button>
          ))}
        </div>
      </div>
      {mutationMessage ? <p className="helper-text" role="status">{mutationMessage}</p> : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="찜한 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="찜한 콘텐츠가 없습니다." message="학습자료나 이러닝 상세에서 찜을 추가해 보세요." /> : null}
      {loadState === 'loaded' ? <BookmarkList items={items} onDelete={removeBookmark} /> : null}
    </section>
  );
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
