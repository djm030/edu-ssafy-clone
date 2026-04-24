import { useEffect, useState } from 'react';
import { getDashboardSummary } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import type { DashboardSummary, LoadState } from '../types';

function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

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
  }, [retryToken]);

  return (
    <section className="page">
      <PageHeader eyebrow="MAIN" title="대시보드" description="오늘 학습과 캠퍼스 상태를 한 화면에서 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? (
        <DataState title="대시보드를 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} />
      ) : null}
      {summary ? <DashboardContent summary={summary} /> : null}
    </section>
  );
}

function DashboardContent({ summary }: { summary: DashboardSummary }) {
  const expPercent = Math.min(100, Math.round((summary.level.exp / summary.level.nextLevelExp) * 100));

  return (
    <>
      <div className="summary-grid">
        <StatCard title="출석" value={`${summary.attendance.present}일`} detail={`지각 ${summary.attendance.late} · 결석 ${summary.attendance.absent}`} />
        <StatCard title="레벨" value={`Lv.${summary.level.level}`} detail={`경험치 ${expPercent}% · 순위 ${summary.level.rank}위`} />
        <StatCard title="포인트" value={`${summary.level.scholarshipPoints}점`} detail="장학 포인트" />
        <StatCard title="읽지 않은 알림" value={`${summary.notifications.unreadCount}건`} detail="최근 알림 확인 필요" />
      </div>
      <div className="content-grid">
        <section className="panel">
          <h2>오늘의 학습</h2>
          <dl className="info-list">
            <div>
              <dt>커리큘럼</dt>
              <dd>{summary.today.curriculumTitle}</dd>
            </div>
            <div>
              <dt>Quest</dt>
              <dd>{summary.today.questTitle}</dd>
            </div>
            <div>
              <dt>설문</dt>
              <dd>{summary.today.surveyTitle}</dd>
            </div>
          </dl>
        </section>
        <section className="panel">
          <h2>주요 서비스</h2>
          <div className="quick-links">
            <a href="/mycampus/attendance">출석현황</a>
            <a href="/learning/materials">학습자료</a>
            <a href="/community/free">자유게시판</a>
          </div>
        </section>
      </div>
    </>
  );
}

function StatCard({ title, value, detail }: { title: string; value: string; detail: string }) {
  return (
    <section className="stat-card">
      <span>{title}</span>
      <strong>{value}</strong>
      <p>{detail}</p>
    </section>
  );
}

export default DashboardPage;
