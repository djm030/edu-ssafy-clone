import { FormEvent, useEffect, useState } from 'react';
import { getAllReplays, getMyReplays, recordReplayWatch } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, ReplayItem } from '../types';

interface ReplaysPageProps {
  mode?: 'my' | 'all';
}

function ReplaysPage({ mode = 'my' }: ReplaysPageProps) {
  const [items, setItems] = useState<ReplayItem[]>([]);
  const [keyword, setKeyword] = useState('');
  const [submittedKeyword, setSubmittedKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    setActionMessage('');
    const request = mode === 'all' ? getAllReplays : getMyReplays;
    request({ keyword: submittedKeyword })
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
  }, [mode, submittedKeyword]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmittedKeyword(keyword.trim());
  };

  const handleWatch = (replay: ReplayItem) => {
    setActionMessage(`${replay.title} 시청 기록을 저장하는 중입니다.`);
    recordReplayWatch(replay.id)
      .then((response) => {
        setItems((current) => current.map((item) => (item.id === response.item.id ? response.item : item)));
        setActionMessage(`${response.item.title} 시청 기록을 저장했습니다.`);
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  const title = mode === 'all' ? '전체강의 다시보기' : '내강의 다시보기';
  const description = mode === 'all' ? '공개 범위 내 전체 다시보기 강의를 검색합니다.' : '내 트랙/반에 배정된 다시보기 강의를 확인합니다.';

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title={title} description={description} />
      <div className="filter-bar">
        <div className="category-strip">
          <a className={mode === 'my' ? 'category-chip active' : 'category-chip'} href="/learning/replays/my">내강의</a>
          <a className={mode === 'all' ? 'category-chip active' : 'category-chip'} href="/learning/replays/all">전체강의</a>
        </div>
        <form className="search-form" onSubmit={submit}>
          <input onChange={(event) => setKeyword(event.target.value)} placeholder="강의 제목 검색" value={keyword} />
          <button type="submit">검색</button>
        </form>
      </div>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="다시보기를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="다시보기 강의가 없습니다." message="검색어를 바꿔 보세요." /> : null}
      {actionMessage ? <p className="helper-text" role="status">{actionMessage}</p> : null}
      {loadState === 'loaded' ? <ReplayTable items={items} onWatch={handleWatch} /> : null}
    </section>
  );
}

function ReplayTable({ items, onWatch }: { items: ReplayItem[]; onWatch: (item: ReplayItem) => void }) {
  return (
    <div className="simple-table" role="table" aria-label="강의 다시보기 목록">
      <div className="simple-row table-head" role="row">
        <span role="columnheader">제목</span>
        <span role="columnheader">분류</span>
        <span role="columnheader">강사</span>
        <span role="columnheader">시간</span>
        <span role="columnheader">상태</span>
        <span role="columnheader">동작</span>
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
          <span role="cell">
            <button className="ghost-button" onClick={() => onWatch(item)} type="button">시청 기록</button>
          </span>
        </div>
      ))}
    </div>
  );
}

export default ReplaysPage;
