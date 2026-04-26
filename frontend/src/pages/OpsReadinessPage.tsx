import { useEffect, useMemo, useState } from 'react';
import { getAccessPolicy } from '../api/app';
import { runReadinessChecks, summarizeReadiness, type ReadinessCheckResult } from '../api/readiness';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import { screenSmokeRoutes } from '../routes';
import type { AccessPolicyItem } from '../types';

type LoadState = 'idle' | 'loading' | 'loaded' | 'error';

function OpsReadinessPage() {
  const [results, setResults] = useState<ReadinessCheckResult[]>([]);
  const [accessPolicies, setAccessPolicies] = useState<AccessPolicyItem[]>([]);
  const [state, setState] = useState<LoadState>('idle');
  const [error, setError] = useState('');
  const [checkedAt, setCheckedAt] = useState('');

  const summary = useMemo(() => summarizeReadiness(results), [results]);

  const refresh = () => {
    setState('loading');
    setError('');
    Promise.all([runReadinessChecks(), getAccessPolicy()])
      .then(([nextResults, policyResponse]) => {
        setResults(nextResults);
        setAccessPolicies(policyResponse.items);
        setCheckedAt(new Date().toLocaleString('ko-KR'));
        setState('loaded');
      })
      .catch((nextError: unknown) => {
        setError(nextError instanceof Error ? nextError.message : '운영 점검을 실행하지 못했습니다.');
        setState('error');
      });
  };

  useEffect(() => {
    refresh();
  }, []);

  return (
    <main className="page">
      <PageHeader
        action={<button className="primary-action" disabled={state === 'loading'} onClick={refresh} type="button">다시 점검</button>}
        description="Nginx, backend health, actuator metrics, session, 핵심 도메인 API를 실제 엔드포인트로 호출해 배포 직전 smoke 상태를 확인합니다."
        eyebrow="PRODUCTION SMOKE"
        title="운영 준비 점검"
      />

      {state === 'loading' ? <LoadingRows /> : null}
      {state === 'error' ? <DataState actionLabel="다시 점검" message={error} onAction={refresh} title="운영 점검 실패" /> : null}

      {state === 'loaded' ? (
        <>
          <section className="summary-grid" aria-label="운영 준비 요약">
            <article className="stat-card">
              <span>전체 점검</span>
              <strong>{summary.total}</strong>
              <p>{checkedAt ? `${checkedAt} 기준` : '아직 실행 전'}</p>
            </article>
            <article className="stat-card">
              <span>통과</span>
              <strong>{summary.passed}</strong>
              <p>실제 API/프록시 응답 확인</p>
            </article>
            <article className="stat-card">
              <span>실패</span>
              <strong>{summary.failed}</strong>
              <p>권한, 세션, 배포 상태 확인 필요</p>
            </article>
            <article className="stat-card">
              <span>판정</span>
              <strong>{summary.ready ? 'READY' : 'CHECK'}</strong>
              <p>{summary.ready ? '필수 smoke가 모두 통과했습니다.' : '실패 항목을 먼저 해결하세요.'}</p>
            </article>
          </section>

          <section className="panel" aria-labelledby="ops-readiness-results">
            <h2 id="ops-readiness-results">점검 결과</h2>
            <div className="simple-table readiness-table" role="table" aria-label="운영 준비 점검 결과">
              <div className="simple-table-row simple-table-head" role="row">
                <span role="columnheader">상태</span>
                <span role="columnheader">항목</span>
                <span role="columnheader">대상</span>
                <span role="columnheader">증거</span>
                <span role="columnheader">메시지</span>
              </div>
              {results.map((result) => (
                <div className="simple-table-row" key={result.id} role="row">
                  <span role="cell">
                    <StatusPill tone={result.status === 'pass' ? 'green' : 'red'}>
                      {result.status === 'pass' ? 'PASS' : 'FAIL'}
                    </StatusPill>
                  </span>
                  <strong role="cell">{result.label}</strong>
                  <code role="cell">{result.target}</code>
                  <span role="cell">{formatEvidence(result)}</span>
                  <span role="cell">{result.message}</span>
                </div>
              ))}
            </div>
          </section>

          <section className="panel" aria-labelledby="ops-access-policy">
            <h2 id="ops-access-policy">권한 정책 매트릭스</h2>
            <p className="muted">
              운영 전 staff/admin 전용 API가 백엔드 정책으로 잠겨 있는지 확인하는 접근 제어 기준입니다.
            </p>
            {accessPolicies.length === 0 ? (
              <DataState title="권한 정책이 없습니다." message="백엔드 접근 정책 응답을 확인하세요." />
            ) : (
              <div className="simple-table readiness-table" role="table" aria-label="권한 정책 매트릭스">
                <div className="simple-table-row simple-table-head" role="row">
                  <span role="columnheader">기능</span>
                  <span role="columnheader">메서드</span>
                  <span role="columnheader">경로</span>
                  <span role="columnheader">허용 역할</span>
                </div>
                {accessPolicies.map((policy) => (
                  <div className="simple-table-row" key={policy.id} role="row">
                    <strong role="cell">{policy.feature}</strong>
                    <code role="cell">{policy.method}</code>
                    <code role="cell">{policy.pathPattern}</code>
                    <span role="cell">{policy.allowedRoles.join(', ')}</span>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="panel" aria-labelledby="ops-screen-smoke">
            <h2 id="ops-screen-smoke">화면 smoke 대상</h2>
            <p className="muted">
              우선순위 1~9 핵심 화면의 라우팅 진입점을 명시해 배포 전 브라우저/수동 smoke 범위를 누락 없이 확인합니다.
            </p>
            <div className="simple-table readiness-table" role="table" aria-label="화면 smoke 대상">
              <div className="simple-table-row simple-table-head" role="row">
                <span role="columnheader">우선순위</span>
                <span role="columnheader">기능</span>
                <span role="columnheader">화면</span>
                <span role="columnheader">경로</span>
              </div>
              {screenSmokeRoutes.map((route) => (
                <div className="simple-table-row" key={route.id} role="row">
                  <span role="cell">#{route.priority}</span>
                  <span role="cell">{route.feature}</span>
                  <strong role="cell">{route.label}</strong>
                  <a role="cell" href={route.path}>{route.path}</a>
                </div>
              ))}
            </div>
          </section>
        </>
      ) : null}
    </main>
  );
}

function formatEvidence(result: ReadinessCheckResult): string {
  const checkedAt = new Date(result.checkedAt);
  const timestamp = Number.isNaN(checkedAt.getTime())
    ? result.checkedAt
    : new Intl.DateTimeFormat('ko-KR', { timeStyle: 'medium' }).format(checkedAt);
  return `${timestamp} · ${result.durationMs}ms`;
}

export default OpsReadinessPage;
