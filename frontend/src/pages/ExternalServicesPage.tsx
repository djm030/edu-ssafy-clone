import { useEffect, useState } from 'react';
import { getExternalServices, logExternalServiceAccess } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { ExternalServiceItem, LoadState } from '../types';

function ExternalServicesPage() {
  const [items, setItems] = useState<ExternalServiceItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getExternalServices()
      .then((response) => {
        if (ignore) return;
        setItems(response);
        setLoadState(response.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => { ignore = true; };
  }, []);

  const openService = (item: ExternalServiceItem) => {
    if (!isLaunchable(item)) {
      setActionMessage(item.disabledReason || `${item.name}은 현재 비활성화되어 있습니다.`);
      return;
    }
    setActionMessage('');
    logExternalServiceAccess(item.code)
      .then((access) => {
        if (access.openInNewWindow ?? item.openInNewWindow ?? true) {
          window.open(access.url || item.url, '_blank', 'noopener,noreferrer');
          return;
        }
        window.location.assign(access.url || item.url);
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="EXTERNAL" title="외부 서비스" description="JOB SSAFY, SSAFY GIT, Meeting! SSAFY 이동 링크와 접근 로그를 관리합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="외부 서비스를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="등록된 외부 서비스가 없습니다." message="운영 설정이 완료되면 링크가 표시됩니다." /> : null}
      {actionMessage ? <p className="muted">{actionMessage}</p> : null}
      {loadState === 'loaded' ? (
        <>
          <ExternalServiceBoundaryPanel items={items} />
          <section className="grid-list external-service-grid">
          {items.map((item) => (
            <article className="panel card" key={item.code}>
              <StatusPill tone={isLaunchable(item) ? 'green' : 'gray'}>{serviceStatusLabel(item)}</StatusPill>
              <StatusPill tone={item.launchType === 'SSO_FORM' ? 'yellow' : 'blue'}>{launchLabel(item.launchType)}</StatusPill>
              <h2>{item.name}</h2>
              <p>{item.description}</p>
              <dl className="info-list">
                <div>
                  <dt>정책</dt>
                  <dd>{item.policyLabel || '외부 링크'}</dd>
                </div>
                <div>
                  <dt>인증</dt>
                  <dd>{item.requiresAuth === false ? '공개 링크' : '로그인 사용자 기준 접근 로그'}</dd>
                </div>
                <div>
                  <dt>열기 방식</dt>
                  <dd>{servicePolicyDetail(item)}</dd>
                </div>
              </dl>
              {!isLaunchable(item) ? <p className="form-message">{item.disabledReason || '운영 설정 전까지 열 수 없습니다.'}</p> : null}
              <span className="muted">접근 로그 {item.accessCount}회 · 최근 {formatDateTime(item.lastAccessedAt)} · 실제 SSO 토큰 미발급</span>
              <button className="primary-action" disabled={!isLaunchable(item)} onClick={() => openService(item)} type="button">
                {item.openInNewWindow === false ? '현재 창에서 열기' : '새 창으로 열기'}
              </button>
            </article>
          ))}
          </section>
        </>
      ) : null}
    </section>
  );
}

function ExternalServiceBoundaryPanel({ items }: { items: ExternalServiceItem[] }) {
  const launchableCount = items.filter(isLaunchable).length;
  const disabledCount = items.length - launchableCount;
  const ssoCount = items.filter((item) => item.launchType === 'SSO_FORM').length;
  const newWindowCount = items.filter((item) => item.openInNewWindow !== false).length;

  return (
    <section className="panel external-service-boundary" aria-label="외부 서비스 클론 경계와 실행 정책">
      <div>
        <p className="eyebrow">SSO BOUNDARY</p>
        <h2>실제 SSO 토큰은 발급하지 않고 Launch URL과 접근 로그만 검증합니다.</h2>
        <p>JOB SSAFY, SSAFY GIT, Meeting! SSAFY는 운영 설정이 없는 경우 비활성 상태로 표시하며 사용자가 누른 시점만 감사 로그로 남깁니다.</p>
      </div>
      <div className="external-service-boundary__stats">
        <span><strong>{launchableCount}</strong>사용 가능</span>
        <span><strong>{disabledCount}</strong>권한 없음/준비중</span>
        <span><strong>{ssoCount}</strong>SSO 경계</span>
        <span><strong>{newWindowCount}</strong>noopener 새 창</span>
      </div>
    </section>
  );
}

function serviceStatusLabel(item: ExternalServiceItem): string {
  if (isLaunchable(item)) return item.launchType === 'SSO_FORM' ? 'SSO Launch 가능' : '외부 링크 가능';
  if (!item.enabled) return '권한 없음 또는 운영 비활성';
  if (!item.url || item.url === '#' || item.url.toLowerCase().startsWith('#none')) return 'unconfigured URL';
  return '외부 장애 또는 준비중';
}

function servicePolicyDetail(item: ExternalServiceItem): string {
  const auth = item.requiresAuth === false ? '공개 링크' : '로그인 사용자 기준';
  const windowPolicy = item.openInNewWindow === false ? '현재 창 이동' : '새 창 열기 + rel=noopener 정책';
  return `${auth} · ${windowPolicy}`;
}

function isLaunchable(item: ExternalServiceItem): boolean {
  return item.launchable ?? item.enabled;
}

function launchLabel(value?: string): string {
  if (value === 'SSO_FORM') return 'SSO Launch';
  if (value === 'MEETING_LINK') return 'Meeting Link';
  return 'External Link';
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default ExternalServicesPage;
