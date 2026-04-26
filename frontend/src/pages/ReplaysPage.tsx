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
  const [trackFilter, setTrackFilter] = useState('all');
  const [instructorFilter, setInstructorFilter] = useState('all');
  const [periodFilter, setPeriodFilter] = useState('all');
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

  const resetFilters = () => {
    setKeyword('');
    setSubmittedKeyword('');
    setTrackFilter('all');
    setInstructorFilter('all');
    setPeriodFilter('all');
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
  const filteredItems = filterReplayItems(items, { trackFilter, instructorFilter, periodFilter });
  const trackOptions = uniqueOptions(items.map((item) => item.scope || item.classroom || item.category));
  const instructorOptions = uniqueOptions(items.map((item) => item.instructor));

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
      <section className="panel replay-filter-panel" aria-label="다시보기 기간 트랙 강사 필터">
        <div className="replay-filter-grid">
          <label>
            <span>기간/시청상태</span>
            <select onChange={(event) => setPeriodFilter(event.target.value)} value={periodFilter}>
              <option value="all">전체 기간</option>
              <option value="week">최근 7일</option>
              <option value="month">최근 30일</option>
              <option value="watched">시청 완료</option>
              <option value="unwatched">미시청</option>
            </select>
          </label>
          <label>
            <span>트랙/반</span>
            <select onChange={(event) => setTrackFilter(event.target.value)} value={trackFilter}>
              <option value="all">전체 트랙</option>
              {trackOptions.map((option) => <option key={option} value={option}>{option}</option>)}
            </select>
          </label>
          <label>
            <span>강사</span>
            <select onChange={(event) => setInstructorFilter(event.target.value)} value={instructorFilter}>
              <option value="all">전체 강사</option>
              {instructorOptions.map((option) => <option key={option} value={option}>{option}</option>)}
            </select>
          </label>
          <button className="ghost-button" onClick={resetFilters} type="button">조건 초기화</button>
        </div>
        <p className="muted">검색 결과 {items.length}개 중 {filteredItems.length}개 표시 · 시청 이력과 공개 범위를 함께 확인합니다.</p>
      </section>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="다시보기를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="다시보기 강의가 없습니다." message="검색어를 바꿔 보세요." /> : null}
      {actionMessage ? <p className="helper-text" role="status">{actionMessage}</p> : null}
      {loadState === 'loaded' && filteredItems.length === 0 ? <DataState title="필터 조건에 맞는 다시보기가 없습니다." message="기간, 트랙, 강사 조건을 조정해 주세요." onAction={resetFilters} /> : null}
      {loadState === 'loaded' && filteredItems.length > 0 ? <ReplayTable items={filteredItems} onWatch={handleWatch} /> : null}
    </section>
  );
}

function ReplayTable({ items, onWatch }: { items: ReplayItem[]; onWatch: (item: ReplayItem) => void }) {
  return (
    <div className="simple-table replay-table" role="table" aria-label="강의 다시보기 목록">
      <div className="simple-row table-head" role="row">
        <span role="columnheader">제목</span>
        <span role="columnheader">분류</span>
        <span role="columnheader">강사</span>
        <span role="columnheader">일자/시간</span>
        <span role="columnheader">시청 이력</span>
        <span role="columnheader">상태</span>
        <span role="columnheader">동작</span>
      </div>
      {items.map((item) => (
        <div className="simple-row" key={item.id} role="row">
          <strong role="cell">{item.title}</strong>
          <span role="cell">{item.category}</span>
          <span role="cell">{item.instructor}</span>
          <span role="cell">{item.date} · {item.duration}</span>
          <span role="cell">{item.watchCount ?? 0}회 · 최근 {formatDateTime(item.lastWatchedAt)}</span>
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

interface ReplayFilterState {
  trackFilter: string;
  instructorFilter: string;
  periodFilter: string;
}

function filterReplayItems(items: ReplayItem[], filters: ReplayFilterState): ReplayItem[] {
  return items.filter((item) => {
    const track = item.scope || item.classroom || item.category || '';
    if (filters.trackFilter !== 'all' && track !== filters.trackFilter) return false;
    if (filters.instructorFilter !== 'all' && item.instructor !== filters.instructorFilter) return false;
    if (filters.periodFilter === 'watched') return item.watched;
    if (filters.periodFilter === 'unwatched') return !item.watched;
    if (filters.periodFilter === 'week') return isWithinDays(item.date, 7);
    if (filters.periodFilter === 'month') return isWithinDays(item.date, 30);
    return true;
  });
}

function uniqueOptions(values: Array<string | null | undefined>): string[] {
  return Array.from(new Set(values.map((value) => value?.trim()).filter((value): value is string => Boolean(value)))).sort((a, b) => a.localeCompare(b, 'ko-KR'));
}

function isWithinDays(value: string, days: number): boolean {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return true;
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  return diff >= 0 && diff <= days * 24 * 60 * 60 * 1000;
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value.slice(0, 16).replace('T', ' ');
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default ReplaysPage;
