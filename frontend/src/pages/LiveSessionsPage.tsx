import { useEffect, useState } from 'react';
import { getCurrentLiveSession, getTodayLiveSessions, joinLiveSession } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LiveSessionItem, LoadState } from '../types';

const statusLabels: Record<LiveSessionItem['status'], { label: string; tone: 'blue' | 'green' | 'gray' }> = {
  scheduled: { label: '오픈 전', tone: 'blue' },
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
    if (!session.joinEnabled) {
      setActionMessage(liveDisabledReason(session));
      return;
    }
    setActionMessage(`${session.title} Meeting 입장 기록을 저장하는 중입니다.`);
    joinLiveSession(session.id)
      .then((response) => {
        setItems((currentItems) => currentItems.map((item) => (item.id === response.item.id ? response.item : item)));
        if (current?.id === response.item.id) setCurrent(response.item);
        setActionMessage(`${response.item.title} Meeting 입장 기록을 저장했습니다.`);
        if (response.item.joinUrl && response.item.joinUrl !== '#') {
          window.open(response.item.joinUrl, '_blank', 'noopener,noreferrer');
        }
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  const liveSummary = summarizeLiveSessions(items);

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="라이브 바로가기" description="오늘 진행되는 라이브 강의와 현재 입장 가능한 세션을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="라이브 일정을 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} /> : null}
      {loadState === 'empty' ? <DataState title="오늘 예정된 라이브가 없습니다." message="배정된 트랙/반 기준으로 오늘 활성화된 라이브 세션이 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <>
        <LiveAccessSummaryPanel summary={liveSummary} />
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
        </>
      ) : null}
    </section>
  );
}

function LiveCard({ item, onJoin }: { item: LiveSessionItem; onJoin: (item: LiveSessionItem) => void; primary?: boolean }) {
  const status = statusLabels[item.status];
  return (
    <article className={`list-card ${item.joinEnabled ? 'joinable' : 'disabled'}`}>
      <div>
        <p className="eyebrow">{[item.track, item.cohort, item.classRoom].filter(Boolean).join(' · ') || 'LIVE'}</p>
        <h3>{item.title}</h3>
        <p>{formatDateTime(item.startsAt)} ~ {formatDateTime(item.endsAt)}</p>
        <p className="muted">입장 {item.joinCount}회 · 최근 입장 {formatDateTime(item.lastJoinedAt)} · Meeting {meetingLinkLabel(item.joinUrl)}</p>
        <div className="live-policy-row" aria-label="Meeting 입장 정책">
          <StatusPill tone={item.joinEnabled ? 'green' : isPlaceholderLiveUrl(item.joinUrl) ? 'yellow' : 'gray'}>{item.joinEnabled ? '입장 가능' : liveAccessStateLabel(item)}</StatusPill>
          <span>{item.joinEnabled ? '입장 로그 저장 후 새 창으로 이동' : liveDisabledReason(item)}</span>
        </div>
      </div>
      <StatusPill tone={status.tone}>{status.label}</StatusPill>
      <button className="primary-action" disabled={!item.joinEnabled} onClick={() => onJoin(item)} type="button" title={item.joinEnabled ? undefined : liveDisabledReason(item)}>
        {item.joinEnabled ? item.actionLabel : liveAccessStateLabel(item)}
      </button>
    </article>
  );
}

interface LiveSessionSummary {
  totalCount: number;
  liveCount: number;
  scheduledCount: number;
  endedCount: number;
  blockedCount: number;
}

function LiveAccessSummaryPanel({ summary }: { summary: LiveSessionSummary }) {
  return (
    <section className="panel live-access-summary-panel" aria-label="라이브 Meeting 상태 요약">
      <div className="section-heading compact-heading">
        <div>
          <p>MEETING STATUS</p>
          <h2>오늘 라이브 입장 상태</h2>
        </div>
        <span>{summary.liveCount}/{summary.totalCount} 진행 중</span>
      </div>
      <div className="live-access-summary-grid">
        <LiveSummaryMetric label="진행 중" value={`${summary.liveCount}개`} />
        <LiveSummaryMetric label="오픈 전" value={`${summary.scheduledCount}개`} />
        <LiveSummaryMetric label="종료" value={`${summary.endedCount}개`} />
        <LiveSummaryMetric label="권한/링크 대기" value={`${summary.blockedCount}개`} />
      </div>
    </section>
  );
}

function LiveSummaryMetric({ label, value }: { label: string; value: string }) {
  return (
    <div className="live-access-summary-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function summarizeLiveSessions(items: LiveSessionItem[]): LiveSessionSummary {
  return items.reduce<LiveSessionSummary>((summary, item) => {
    summary.totalCount += 1;
    if (item.status === 'live') summary.liveCount += 1;
    if (item.status === 'scheduled') summary.scheduledCount += 1;
    if (item.status === 'ended') summary.endedCount += 1;
    if (!item.joinEnabled) summary.blockedCount += 1;
    return summary;
  }, { totalCount: 0, liveCount: 0, scheduledCount: 0, endedCount: 0, blockedCount: 0 });
}

function liveAccessStateLabel(item: LiveSessionItem): string {
  if (isPlaceholderLiveUrl(item.joinUrl)) return '링크 준비중';
  if (item.status === 'scheduled') return '오픈 전';
  if (item.status === 'ended') return '종료';
  return '권한 없음';
}

function liveDisabledReason(item: LiveSessionItem): string {
  if (item.disabledReason) return item.disabledReason;
  if (isPlaceholderLiveUrl(item.joinUrl)) return 'Meeting 링크가 아직 준비되지 않았습니다.';
  if (item.status === 'scheduled') return '라이브 시작 전에는 입장할 수 없습니다.';
  if (item.status === 'ended') return '종료된 라이브는 다시 입장할 수 없습니다.';
  return '이 계정으로는 Meeting에 입장할 수 없습니다.';
}

function isPlaceholderLiveUrl(value?: string | null): boolean {
  if (!value) return true;
  const normalized = value.trim().toLowerCase();
  return normalized === '#' || normalized === '#none' || normalized === '#none;';
}

function meetingLinkLabel(value?: string | null): string {
  return isPlaceholderLiveUrl(value) ? '미연결' : '새 창';
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  return value.replace('T', ' ').slice(0, 16);
}

export default LiveSessionsPage;
