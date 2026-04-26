import { FormEvent, useEffect, useState } from 'react';
import { getClassmates, sendClassmateNotification } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { Classmate, ClassmateSummary, LoadState } from '../types';

const roleFilters = [
  { label: '전체', value: 'all' },
  { label: '교육생', value: 'learner' },
  { label: '코치', value: 'coach' },
  { label: '운영진', value: 'assistant' },
] as const;

const emptySummary: ClassmateSummary = {
  totalCount: 0,
  learnerCount: 0,
  coachCount: 0,
  staffCount: 0,
};

interface ClassmatesPageProps {
  canSendNotifications?: boolean;
}

function ClassmatesPage({ canSendNotifications = false }: ClassmatesPageProps) {
  const [items, setItems] = useState<Classmate[]>([]);
  const [summary, setSummary] = useState<ClassmateSummary>(emptySummary);
  const [keyword, setKeyword] = useState('');
  const [submittedKeyword, setSubmittedKeyword] = useState('');
  const [memberRole, setMemberRole] = useState<(typeof roleFilters)[number]['value']>('all');
  const [notificationText, setNotificationText] = useState('같이 스터디해요!');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [notifyingId, setNotifyingId] = useState<number | null>(null);
  const [notificationMessage, setNotificationMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getClassmates({ keyword: submittedKeyword, memberRole })
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
  }, [submittedKeyword, memberRole]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmittedKeyword(keyword.trim());
  };

  const notifyClassmate = async (classmate: Classmate) => {
    if (!canSendNotifications) {
      setNotificationMessage('알림 발송은 코치 또는 관리자 권한이 필요합니다.');
      return;
    }

    setNotifyingId(classmate.id);
    setNotificationMessage('');

    try {
      const response = await sendClassmateNotification(classmate.id, notificationText);
      setNotificationMessage(`${classmate.name}님에게 알림을 보냈습니다.${response.id ? ` 알림번호: ${response.id}` : ''}`);
    } catch (error) {
      setNotificationMessage(getErrorMessage(error));
    } finally {
      setNotifyingId(null);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="COMMUNITY" title="우리반 보기" description="같은 반 구성원을 검색하고 역할별로 확인한 뒤 알림을 보냅니다." />
      <div className="summary-grid" aria-label="우리반 구성 요약">
        <SummaryCard title="전체" value={summary.totalCount} description="우리반 구성원" />
        <SummaryCard title="교육생" value={summary.learnerCount} description="함께 학습" />
        <SummaryCard title="코치" value={summary.coachCount} description="학습 지원" />
        <SummaryCard title="운영진" value={summary.staffCount} description="행정 지원" />
      </div>
      <div className="filter-bar">
        <div className="category-strip">
          {roleFilters.map((option) => (
            <button className={memberRole === option.value ? 'category-chip active' : 'category-chip'} key={option.value} onClick={() => setMemberRole(option.value)} type="button">
              {option.label}
            </button>
          ))}
        </div>
        <form className="search-form" onSubmit={submit}>
          <input onChange={(event) => setKeyword(event.target.value)} placeholder="이름, 이메일, 캠퍼스, 트랙, 반 검색" value={keyword} />
          <button type="submit">검색</button>
        </form>
        <label className="field-group">
          알림 메시지
          <textarea
            disabled={!canSendNotifications}
            maxLength={1000}
            onChange={(event) => setNotificationText(event.target.value)}
            rows={2}
            value={notificationText}
          />
        </label>
      </div>
      {!canSendNotifications ? (
        <div className="inline-alert">
          알림 보내기는 코치 또는 관리자 권한으로만 사용할 수 있습니다.
        </div>
      ) : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="우리반 정보를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="조건에 맞는 교육생이 없습니다." message="검색어 또는 역할 필터를 바꿔 보세요." /> : null}
      {notificationMessage ? (
        <div className="check-result" aria-live="polite">
          <StatusPill tone={notificationMessage.includes('보냈습니다') ? 'green' : 'red'}>
            {notificationMessage.includes('보냈습니다') ? '발송완료' : '오류'}
          </StatusPill>
          <p>{notificationMessage}</p>
        </div>
      ) : null}
      {loadState === 'loaded' ? (
        <ClassmateGrid
          canSendNotifications={canSendNotifications}
          items={items}
          notifyingId={notifyingId}
          onNotify={notifyClassmate}
        />
      ) : null}
    </section>
  );
}

function SummaryCard({ title, value, description }: { title: string; value: number; description: string }) {
  return (
    <article className="stat-card">
      <p>{title}</p>
      <strong>{value}</strong>
      <span>{description}</span>
    </article>
  );
}

function ClassmateGrid({
  canSendNotifications,
  items,
  notifyingId,
  onNotify,
}: {
  canSendNotifications: boolean;
  items: Classmate[];
  notifyingId: number | null;
  onNotify: (classmate: Classmate) => void;
}) {
  return (
    <div className="classmate-grid">
      {items.map((item) => (
        <article className="panel classmate-card" key={item.id}>
          <div className="avatar" aria-hidden="true">
            {item.name.slice(0, 1)}
          </div>
          <div>
            <h2>{item.name}</h2>
            <p>
              {item.campusName} 캠퍼스 · {item.trackName}{item.cohortName ? ` · ${item.cohortName}` : ''}
            </p>
            <div className="action-row">
              <StatusPill tone="blue">{item.teamName || '팀 미배정'}</StatusPill>
              <StatusPill tone={item.memberRole === 'coach' || item.role === 'coach' ? 'green' : 'gray'}>{roleLabel(item)}</StatusPill>
            </div>
            <p>{item.email || '이메일 비공개'}</p>
            <p>{item.statusMessage || '상태 메시지가 없습니다.'}</p>
            <button
              className="ghost-button"
              disabled={!canSendNotifications || notifyingId === item.id}
              onClick={() => onNotify(item)}
              type="button"
            >
              {!canSendNotifications ? '알림 권한 없음' : notifyingId === item.id ? '알림 발송 중' : '알림 보내기'}
            </button>
          </div>
        </article>
      ))}
    </div>
  );
}

function roleLabel(item: Classmate): string {
  if (item.memberRole === 'coach' || item.role === 'coach') return '코치';
  if (item.memberRole === 'assistant') return '운영진';
  if (item.role === 'admin') return '관리자';
  return '교육생';
}

export default ClassmatesPage;
