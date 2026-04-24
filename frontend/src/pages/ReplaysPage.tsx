import { FormEvent, useEffect, useState } from 'react';
import { getReplays } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, ReplayItem } from '../types';

function ReplaysPage() {
  const [items, setItems] = useState<ReplayItem[]>([]);
  const [keyword, setKeyword] = useState('');
  const [submittedKeyword, setSubmittedKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getReplays({ keyword: submittedKeyword })
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
  }, [submittedKeyword]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmittedKeyword(keyword.trim());
  };

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="강의 다시보기" description="지난 강의를 검색하고 다시 학습합니다." />
      <div className="filter-bar">
        <form className="search-form" onSubmit={submit}>
          <input onChange={(event) => setKeyword(event.target.value)} placeholder="강의 제목 검색" value={keyword} />
          <button type="submit">검색</button>
        </form>
      </div>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="다시보기를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="다시보기 강의가 없습니다." message="검색어를 바꿔 보세요." /> : null}
      {loadState === 'loaded' ? <ReplayTable items={items} /> : null}
    </section>
  );
}

function ReplayTable({ items }: { items: ReplayItem[] }) {
  return (
    <div className="simple-table" role="table" aria-label="강의 다시보기 목록">
      <div className="simple-row table-head" role="row">
        <span role="columnheader">제목</span>
        <span role="columnheader">분류</span>
        <span role="columnheader">강사</span>
        <span role="columnheader">시간</span>
        <span role="columnheader">상태</span>
      </div>
      {items.map((item) => (
        <div className="simple-row" key={item.id} role="row">
          <strong role="cell">{item.title}</strong>
          <span role="cell">{item.category}</span>
          <span role="cell">{item.instructor}</span>
          <span role="cell">{item.duration}</span>
          <span role="cell">
            <StatusPill tone={item.watched ? 'green' : 'gray'}>{item.watched ? '시청완료' : '미시청'}</StatusPill>
          </span>
        </div>
      ))}
    </div>
  );
}

export default ReplaysPage;
