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
    setActionMessage('필수학습 이수 상태를 저장하는 중입니다.');
    completeRequiredStudy(item.id)
      .then((response) => {
        setSelected(response.item);
        setItems((current) => current.map((study) => (study.id === response.item.id ? response.item : study)));
        setActionMessage(`${response.item.title} 이수 처리가 완료되었습니다.`);
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  const openContent = (item: RequiredStudyItem) => {
    if (item.contentUrl && item.contentUrl !== '#') {
      window.open(item.contentUrl, '_blank', 'noopener,noreferrer');
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="필수학습" description="트랙별 필수 학습 콘텐츠와 마감/이수 상태를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="필수학습을 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} /> : null}
      {loadState === 'empty' ? <DataState title="등록된 필수학습이 없습니다." message="현재 계정에 배정된 활성 필수학습이 없습니다." /> : null}
      {loadState === 'loaded' ? (
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
                      <p className="muted">마감 {formatDateTime(item.dueAt)} · 완료 {formatDateTime(item.completedAt)}</p>
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
                <div className="action-row">
                  <button className="ghost-button" onClick={() => openContent(selected)} type="button">콘텐츠 열기</button>
                  <button className="primary-action" disabled={selected.status === 'completed'} onClick={() => handleComplete(selected)} type="button">
                    {selected.status === 'completed' ? '이수 완료' : '이수 처리'}
                  </button>
                </div>
                {actionMessage ? <p className="form-message" role="status">{actionMessage}</p> : null}
              </div>
            ) : (
              <DataState title="선택된 필수학습이 없습니다." message="목록에서 필수학습을 선택해 주세요." />
            )}
          </section>
        </div>
      ) : null}
    </section>
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

export default RequiredStudiesPage;
