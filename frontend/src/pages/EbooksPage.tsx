import { useEffect, useState } from 'react';
import { getEbook, getEbooks, recordEbookAccess } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { EbookItem, LoadState } from '../types';

interface EbooksPageProps {
  ebookId?: number;
}

function EbooksPage({ ebookId }: EbooksPageProps) {
  const [items, setItems] = useState<EbookItem[]>([]);
  const [selected, setSelected] = useState<EbookItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    setActionMessage('');

    const request = ebookId ? getEbook(ebookId).then((item) => ({ items: [item], selected: item })) : getEbooks({ page: 1, size: 50 }).then((response) => ({ items: response.items, selected: response.items[0] }));

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
  }, [ebookId, retryToken]);

  const openEbook = (item: EbookItem) => {
    if (!item.accessEnabled) {
      setActionMessage(ebookDisabledReason(item));
      return;
    }
    setActionMessage('');
    recordEbookAccess(item.id)
      .then((response) => {
        setSelected(response.item);
        setItems((current) => current.map((ebook) => (ebook.id === response.item.id ? response.item : ebook)));
        setActionMessage(`${response.item.title} 열람 기록을 저장했습니다.`);
        if (response.item.externalUrl && response.item.externalUrl !== '#') {
          window.open(response.item.externalUrl, '_blank', 'noopener,noreferrer');
        }
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  const availability = summarizeEbookAvailability(items);

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="SSAFY e-book" description="학습자료와 별도로 제공되는 e-book을 확인하고 열람 기록을 남깁니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="e-book을 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} /> : null}
      {loadState === 'empty' ? <DataState title="등록된 e-book이 없습니다." message="활성화된 e-book이 등록되면 이곳에 표시됩니다." /> : null}
      {loadState === 'loaded' ? (
        <>
        <EbookReadinessPanel availability={availability} />
        <div className="content-grid">
          <section className="panel">
            <h2>e-book 목록</h2>
            <div className="list-stack">
              {items.map((item) => (
                <article className={`list-card ${item.accessEnabled ? 'joinable' : 'disabled'}`} key={item.id}>
                  <div>
                    <div className="ebook-card-status">
                      <p className="eyebrow">{item.category || 'E-BOOK'}</p>
                      <StatusPill tone={item.accessEnabled ? 'green' : isPlaceholderEbookUrl(item.externalUrl) ? 'yellow' : 'gray'}>{ebookStatusLabel(item)}</StatusPill>
                    </div>
                    <h3>{item.title}</h3>
                    <p>{item.description || '설명이 등록되지 않았습니다.'}</p>
                    <p className="muted">열람 {item.accessCount}회 · 최근 열람 {formatDateTime(item.lastAccessedAt)} · 뷰어 {ebookHostLabel(item.externalUrl)}</p>
                    {!item.accessEnabled ? <p className="muted">{ebookDisabledReason(item)}</p> : null}
                  </div>
                  <a className="text-link" href={`/mycampus/ebooks/${item.id}`}>상세</a>
                </article>
              ))}
            </div>
          </section>
          <section className="panel">
            <h2>상세 보기</h2>
            {selected ? (
              <div className="detail-stack">
                <p className="eyebrow">{selected.category || 'SSAFY e-book'}</p>
                <h3>{selected.title}</h3>
                <p>{selected.description || '설명이 등록되지 않았습니다.'}</p>
                <dl className="info-list">
                  <InfoRow label="등록일" value={selected.createdAt ? selected.createdAt.slice(0, 10) : '-'} />
                  <InfoRow label="열람 횟수" value={`${selected.accessCount}회`} />
                  <InfoRow label="최근 열람" value={formatDateTime(selected.lastAccessedAt)} />
                </dl>
                <EbookViewerPolicyPanel item={selected} />
                <button className="primary-action" disabled={!selected.accessEnabled} onClick={() => openEbook(selected)} type="button" title={selected.accessEnabled ? undefined : ebookDisabledReason(selected)}>{selected.accessEnabled ? selected.actionLabel : ebookStatusLabel(selected)}</button>
                {actionMessage ? <p className="form-message">{actionMessage}</p> : null}
              </div>
            ) : (
              <DataState title="선택된 e-book이 없습니다." message="목록에서 e-book을 선택해 주세요." />
            )}
          </section>
        </div>
        </>
      ) : null}
    </section>
  );
}

function EbookReadinessPanel({ availability }: { availability: EbookAvailability }) {
  return (
    <section className="panel ebook-readiness-panel" aria-label="SSAFY e-book 권한 및 뷰어 상태">
      <div className="section-heading compact-heading">
        <div>
          <p>E-BOOK STATUS</p>
          <h2>권한/뷰어 준비 상태</h2>
        </div>
        <span>{availability.enabledCount}/{availability.totalCount} 열람 가능</span>
      </div>
      <div className="ebook-readiness-grid">
        <ReadinessMetric label="사용 가능" value={`${availability.enabledCount}권`} detail="외부 뷰어 URL과 접근 권한이 모두 준비된 e-book" />
        <ReadinessMetric label="준비중" value={`${availability.pendingCount}권`} detail="#none 또는 미설정 URL로 외부 뷰어가 아직 연결되지 않음" />
        <ReadinessMetric label="권한 없음" value={`${availability.lockedCount}권`} detail="로그인 사용자 조건 또는 운영 정책상 비활성화된 e-book" />
      </div>
    </section>
  );
}

function ReadinessMetric({ label, value, detail }: { label: string; value: string; detail: string }) {
  return (
    <div className="ebook-readiness-card">
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{detail}</small>
    </div>
  );
}

function EbookViewerPolicyPanel({ item }: { item: EbookItem }) {
  return (
    <div className="ebook-viewer-policy" aria-label="e-book 외부 뷰어 정책">
      <StatusPill tone={item.accessEnabled ? 'green' : isPlaceholderEbookUrl(item.externalUrl) ? 'yellow' : 'gray'}>{ebookStatusLabel(item)}</StatusPill>
      <dl className="info-list">
        <InfoRow label="뷰어" value={ebookHostLabel(item.externalUrl)} />
        <InfoRow label="열기 방식" value={item.accessEnabled ? '새 창 열기 · noopener' : '비활성 버튼'} />
        <InfoRow label="정책" value={item.accessEnabled ? '로그인 사용자 기준 열람 로그 저장 후 이동' : ebookDisabledReason(item)} />
      </dl>
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

interface EbookAvailability {
  totalCount: number;
  enabledCount: number;
  pendingCount: number;
  lockedCount: number;
}

function summarizeEbookAvailability(items: EbookItem[]): EbookAvailability {
  return items.reduce<EbookAvailability>((summary, item) => {
    summary.totalCount += 1;
    if (item.accessEnabled) {
      summary.enabledCount += 1;
    } else if (isPlaceholderEbookUrl(item.externalUrl)) {
      summary.pendingCount += 1;
    } else {
      summary.lockedCount += 1;
    }
    return summary;
  }, { totalCount: 0, enabledCount: 0, pendingCount: 0, lockedCount: 0 });
}

function ebookStatusLabel(item: EbookItem): string {
  if (item.accessEnabled) return '사용 가능';
  if (isPlaceholderEbookUrl(item.externalUrl)) return '준비중';
  return '권한 없음';
}

function ebookDisabledReason(item: EbookItem): string {
  if (item.disabledReason) return item.disabledReason;
  if (isPlaceholderEbookUrl(item.externalUrl)) return '외부 e-book 뷰어가 아직 준비되지 않았습니다.';
  return '이 계정으로는 e-book을 열람할 수 없습니다.';
}

function isPlaceholderEbookUrl(value?: string | null): boolean {
  if (!value) return true;
  const normalized = value.trim().toLowerCase();
  return normalized === '#' || normalized === '#none' || normalized === '#none;';
}

function ebookHostLabel(value?: string | null): string {
  if (isPlaceholderEbookUrl(value)) return '미연결';
  try {
    return new URL(value || '').host;
  } catch {
    return '외부 뷰어';
  }
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value.slice(0, 16).replace('T', ' ');
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default EbooksPage;
