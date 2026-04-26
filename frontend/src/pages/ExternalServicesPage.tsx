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
        <section className="grid-list">
          {items.map((item) => (
            <article className="panel card" key={item.code}>
              <StatusPill tone={isLaunchable(item) ? 'green' : 'gray'}>{isLaunchable(item) ? '사용 가능' : '비활성'}</StatusPill>
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
                  <dd>{item.openInNewWindow === false ? '현재 창 이동' : '새 창 열기'}</dd>
                </div>
              </dl>
              {!isLaunchable(item) ? <p className="form-message">{item.disabledReason || '운영 설정 전까지 열 수 없습니다.'}</p> : null}
              <span className="muted">접근 {item.accessCount}회 · 최근 {formatDateTime(item.lastAccessedAt)}</span>
              <button className="primary-action" disabled={!isLaunchable(item)} onClick={() => openService(item)} type="button">
                {item.openInNewWindow === false ? '현재 창에서 열기' : '새 창으로 열기'}
              </button>
            </article>
          ))}
        </section>
      ) : null}
    </section>
  );
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
