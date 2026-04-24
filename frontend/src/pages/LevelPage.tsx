import { useEffect, useState } from 'react';
import { getDashboardSummary } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import type { DashboardSummary, LoadState } from '../types';

function LevelPage() {
  const [summary, setSummary] = useState<DashboardSummary>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getDashboardSummary()
      .then((data) => {
        if (ignore) return;
        setSummary(data);
        setLoadState('loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, []);

  const percent = summary ? Math.min(100, Math.round((summary.level.exp / summary.level.nextLevelExp) * 100)) : 0;

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="레벨/포인트" description="현재 레벨, 경험치, 장학 포인트를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="레벨 정보를 불러오지 못했습니다." message={errorMessage} /> : null}
      {summary ? (
        <div className="level-layout">
          <section className="panel">
            <h2>현재 레벨</h2>
            <div className="level-number">Lv.{summary.level.level}</div>
            <div className="progress-track" aria-label={`경험치 ${percent}%`}>
              <span style={{ width: `${percent}%` }} />
            </div>
            <p>
              {summary.level.exp.toLocaleString('ko-KR')} / {summary.level.nextLevelExp.toLocaleString('ko-KR')} EXP
            </p>
          </section>
          <section className="panel">
            <h2>장학 포인트</h2>
            <div className="level-number">{summary.level.scholarshipPoints}점</div>
            <p>현재 순위 {summary.level.rank}위</p>
          </section>
        </div>
      ) : null}
    </section>
  );
}

export default LevelPage;
