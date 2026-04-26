import { useEffect, useState } from 'react';
import { getCurrentLiveSession, getTodayLiveSessions, joinLiveSession } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LiveSessionItem, LoadState } from '../types';

const statusLabels: Record<LiveSessionItem['status'], { label: string; tone: 'blue' | 'green' | 'gray' }> = {
  scheduled: { label: '예정', tone: 'blue' },
  live: { label: '진행 중', tone: 'green' },
  ended: { label: '종료', tone: 'gray' },
};

function LiveSessionsPage() {
  const [items, setItems] = useState<LiveSessionItem[]>([]);
  const [current, setCurrent] = useState<LiveSessionItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    setActionMessage('');

    Promise.all([getTodayLiveSessions(), getCurrentLiveSession()])
      .then(([today, currentSession]) => {
        if (ignore) return;
        setItems(today.items);
        setCurrent(currentSession);
        setLoadState(today.items.length === 0 ? 'empty' : 'loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });

    return () => {
      ignore = true;
    };
  }, [retryToken]);

  const handleJoin = (session: LiveSessionItem) => {
    setActionMessage(`${session.title} 입장 기록을 저장하는 중입니다.`);
    joinLiveSession(session.id)
      .then((response) => {
        setItems((currentItems) => currentItems.map((item) => (item.id === response.item.id ? response.item : item)));
        if (current?.id === response.item.id) setCurrent(response.item);
        setActionMessage(`${response.item.title} 입장 기록을 저장했습니다.`);
        if (response.item.joinUrl && response.item.joinUrl !== '#') {
          window.open(response.item.joinUrl, '_blank', 'noopener,noreferrer');
        }
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="라이브 바로가기" description="오늘 진행되는 라이브 강의와 현재 입장 가능한 세션을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="라이브 일정을 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} /> : null}
      {loadState === 'empty' ? <DataState title="오늘 예정된 라이브가 없습니다." message="배정된 트랙/반 기준으로 오늘 활성화된 라이브 세션이 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <div className="content-grid">
          <section className="panel">
            <h2>현재 라이브</h2>
            {current ? (
              <LiveCard item={current} primary onJoin={handleJoin} />
            ) : (
              <DataState title="현재 진행 중인 라이브가 없습니다." message="오늘 일정에서 예정된 세션을 확인해 주세요." />
            )}
            {actionMessage ? <p className="form-message" role="status">{actionMessage}</p> : null}
          </section>
          <section className="panel">
            <h2>오늘 일정</h2>
            <div className="list-stack">
              {items.map((item) => <LiveCard item={item} key={item.id} onJoin={handleJoin} />)}
            </div>
          </section>
        </div>
      ) : null}
    </section>
  );
}

function LiveCard({ item, onJoin }: { item: LiveSessionItem; onJoin: (item: LiveSessionItem) => void; primary?: boolean }) {
  const status = statusLabels[item.status];
  const canJoin = item.status !== 'ended';
  return (
    <article className="list-card">
      <div>
        <p className="eyebrow">{[item.track, item.cohort, item.classRoom].filter(Boolean).join(' · ') || 'LIVE'}</p>
        <h3>{item.title}</h3>
        <p>{formatDateTime(item.startsAt)} ~ {formatDateTime(item.endsAt)}</p>
        <p className="muted">입장 {item.joinCount}회 · 최근 입장 {formatDateTime(item.lastJoinedAt)}</p>
      </div>
      <StatusPill tone={status.tone}>{status.label}</StatusPill>
      <button className="primary-action" disabled={!canJoin} onClick={() => onJoin(item)} type="button">
        {canJoin ? '입장' : '종료됨'}
      </button>
    </article>
  );
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  return value.replace('T', ' ').slice(0, 16);
}

export default LiveSessionsPage;
