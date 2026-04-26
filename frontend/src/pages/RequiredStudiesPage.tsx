import { useEffect, useState } from 'react';
import { completeRequiredStudy, getRequiredStudies, getRequiredStudy } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, RequiredStudyItem } from '../types';

interface RequiredStudiesPageProps {
  studyId?: number;
}

const statusLabels: Record<RequiredStudyItem['status'], { label: string; tone: 'gray' | 'yellow' | 'green' | 'red' }> = {
  not_started: { label: '미시작', tone: 'gray' },
  in_progress: { label: '학습중', tone: 'yellow' },
  completed: { label: '완료', tone: 'green' },
  overdue: { label: '기한 초과', tone: 'red' },
};

function RequiredStudiesPage({ studyId }: RequiredStudiesPageProps) {
  const [items, setItems] = useState<RequiredStudyItem[]>([]);
  const [selected, setSelected] = useState<RequiredStudyItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [pendingStudyId, setPendingStudyId] = useState<number>();
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    setActionMessage('');

    const request = studyId
      ? getRequiredStudy(studyId).then((item) => ({ items: [item], selected: item }))
      : getRequiredStudies({ page: 1, size: 50 }).then((response) => ({ items: response.items, selected: response.items[0] }));

    request
      .then((response) => {
        if (ignore) return;
        setItems(response.items);
        setSelected(response.selected);
        setLoadState(response.items.length === 0 ? 'empty' : 'loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });

    return () => {
      ignore = true;
    };
  }, [studyId, retryToken]);

  const handleComplete = (item: RequiredStudyItem) => {
    if (!canCompleteRequiredStudy(item)) {
      setActionMessage(requiredStudyDisabledReason(item));
      return;
    }
    setActionMessage('필수학습 이수 상태를 저장하는 중입니다.');
    setPendingStudyId(item.id);
    completeRequiredStudy(item.id)
      .then((response) => {
        setSelected(response.item);
        setItems((current) => current.map((study) => (study.id === response.item.id ? response.item : study)));
        setActionMessage(`${response.item.title} 이수 처리가 완료되었습니다.`);
      })
      .catch((error) => setActionMessage(getErrorMessage(error)))
      .finally(() => setPendingStudyId(undefined));
  };

  const openContent = (item: RequiredStudyItem) => {
    if (isLaunchableStudyContent(item.contentUrl)) {
      window.open(item.contentUrl, '_blank', 'noopener,noreferrer');
      return;
    }
    setActionMessage('학습 콘텐츠 링크가 아직 준비되지 않았습니다.');
  };

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="필수학습" description="트랙별 필수 학습 콘텐츠와 마감/이수 상태를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="필수학습을 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} /> : null}
      {loadState === 'empty' ? <DataState title="등록된 필수학습이 없습니다." message="현재 계정에 배정된 활성 필수학습이 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <>
        <RequiredStudyPolicyPanel items={items} />
        <div className="content-grid">
          <section className="panel">
            <h2>필수학습 목록</h2>
            <div className="list-stack">
              {items.map((item) => {
                const status = statusLabels[item.status];
                return (
                  <article className="list-card" key={item.id}>
                    <div>
                      <p className="eyebrow">{[item.category, item.requiredForTrack].filter(Boolean).join(' · ') || 'REQUIRED'}</p>
                      <h3>{item.title}</h3>
                      <p>{item.description || '등록된 설명이 없습니다.'}</p>
                      <div className="progress-track" aria-label={`${item.title} 진행률 ${item.progressPercent}%`}>
                        <span style={{ width: `${item.progressPercent}%` }} />
                      </div>
                      <p className="muted">마감 {formatDateTime(item.dueAt)} · 완료 {formatDateTime(item.completedAt)} · {requiredStudyPolicyLabel(item)}</p>
                      {!canCompleteRequiredStudy(item) && item.status !== 'completed' ? <p className="muted">{requiredStudyDisabledReason(item)}</p> : null}
                    </div>
                    <StatusPill tone={status.tone}>{status.label}</StatusPill>
                    <a className="text-link" href={`/learning/required-studies/${item.id}`}>상세</a>
                  </article>
                );
              })}
            </div>
          </section>
          <section className="panel">
            <h2>상세 보기</h2>
            {selected ? (
              <div className="detail-stack">
                <p className="eyebrow">{selected.category || '필수학습'}</p>
                <h3>{selected.title}</h3>
                <StatusPill tone={statusLabels[selected.status].tone}>{statusLabels[selected.status].label}</StatusPill>
                <p>{selected.description || '등록된 설명이 없습니다.'}</p>
                <dl className="info-list">
                  <InfoRow label="대상 트랙" value={selected.requiredForTrack || '전체'} />
                  <InfoRow label="콘텐츠 유형" value={selected.contentType} />
                  <InfoRow label="마감" value={formatDateTime(selected.dueAt)} />
                  <InfoRow label="진행률" value={`${selected.progressPercent}%`} />
                  <InfoRow label="완료일" value={formatDateTime(selected.completedAt)} />
                </dl>
                <RequiredStudyCompletionPolicy item={selected} />
                <div className="action-row">
                  <button className="ghost-button" disabled={!isLaunchableStudyContent(selected.contentUrl)} onClick={() => openContent(selected)} type="button" title={isLaunchableStudyContent(selected.contentUrl) ? undefined : '학습 콘텐츠 링크가 준비되지 않았습니다.'}>콘텐츠 열기</button>
                  <button className="primary-action" disabled={!canCompleteRequiredStudy(selected) || pendingStudyId === selected.id} onClick={() => handleComplete(selected)} type="button" title={canCompleteRequiredStudy(selected) ? undefined : requiredStudyDisabledReason(selected)}>
                    {pendingStudyId === selected.id ? '이수 저장 중' : selected.status === 'completed' ? '이수 완료' : '이수 처리'}
                  </button>
                </div>
                {actionMessage ? <p className="form-message" role="status">{actionMessage}</p> : null}
              </div>
            ) : (
              <DataState title="선택된 필수학습이 없습니다." message="목록에서 필수학습을 선택해 주세요." />
            )}
          </section>
        </div>
        </>
      ) : null}
    </section>
  );
}

function RequiredStudyPolicyPanel({ items }: { items: RequiredStudyItem[] }) {
  const completedCount = items.filter((item) => item.status === 'completed').length;
  const overdueCount = items.filter((item) => item.status === 'overdue').length;
  const readyCount = items.filter(canCompleteRequiredStudy).length;

  return (
    <section className="panel required-study-policy-panel" aria-label="필수학습 이수 조건과 만료 상태">
      <div className="section-heading compact-heading">
        <div>
          <p>REQUIRED STUDY</p>
          <h2>이수 조건/만료 요약</h2>
        </div>
        <span>{completedCount}/{items.length} 완료</span>
      </div>
      <div className="required-study-policy-grid">
        <PolicyMetric label="완료" value={`${completedCount}개`} />
        <PolicyMetric label="이수 처리 가능" value={`${readyCount}개`} />
        <PolicyMetric label="기한 초과" value={`${overdueCount}개`} />
      </div>
    </section>
  );
}

function PolicyMetric({ label, value }: { label: string; value: string }) {
  return (
    <div className="required-study-policy-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function RequiredStudyCompletionPolicy({ item }: { item: RequiredStudyItem }) {
  return (
    <div className="required-study-completion-policy" aria-label="필수학습 이수 조건">
      <StatusPill tone={canCompleteRequiredStudy(item) ? 'green' : item.status === 'overdue' ? 'red' : 'gray'}>{requiredStudyPolicyLabel(item)}</StatusPill>
      <p>{canCompleteRequiredStudy(item) ? '학습 진행률이 100%라 이수 처리할 수 있습니다.' : requiredStudyDisabledReason(item)}</p>
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  return value.replace('T', ' ').slice(0, 16);
}

function canCompleteRequiredStudy(item: RequiredStudyItem): boolean {
  return item.status !== 'completed' && item.status !== 'overdue' && item.progressPercent >= 100;
}

function requiredStudyPolicyLabel(item: RequiredStudyItem): string {
  if (item.status === 'completed') return '이수 완료';
  if (item.status === 'overdue') return '재학습/관리자 확인 필요';
  if (item.progressPercent >= 100) return '이수 처리 가능';
  return '학습 진행 필요';
}

function requiredStudyDisabledReason(item: RequiredStudyItem): string {
  if (item.status === 'completed') return '이미 이수 완료된 필수학습입니다.';
  if (item.status === 'overdue') return '마감이 지나 재학습 또는 관리자 확인이 필요합니다.';
  if (item.progressPercent < 100) return '진행률 100%를 달성해야 이수 처리할 수 있습니다.';
  return '현재 이수 처리할 수 없습니다.';
}

function isLaunchableStudyContent(value?: string | null): boolean {
  if (!value) return false;
  const normalized = value.trim().toLowerCase();
  return normalized !== '#' && normalized !== '#none' && normalized !== '#none;';
}

export default RequiredStudiesPage;
