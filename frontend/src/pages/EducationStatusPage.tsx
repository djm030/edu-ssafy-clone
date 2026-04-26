import { useEffect, useState } from 'react';
import { getEducationStatus } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import type { EducationStatusSummary, LoadState } from '../types';

function EducationStatusPage() {
  const [summary, setSummary] = useState<EducationStatusSummary>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getEducationStatus()
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

  const hasAnyData = Boolean(summary && (
    summary.attendance.presentDays > 0
    || summary.attendance.lateDays > 0
    || summary.attendance.absentDays > 0
    || summary.learning.totalRequiredStudyCount > 0
    || summary.quests.openCount > 0
    || summary.quests.submittedCount > 0
    || summary.points.experiencePoint > 0
  ));

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="교육현황" description="출결, 학습, Quest, 포인트를 한 화면에서 통합 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? (
        <DataState title="교육현황을 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} />
      ) : null}
      {summary && !hasAnyData ? (
        <DataState title="아직 집계할 교육현황이 없습니다." message="출석, 학습, Quest 데이터가 쌓이면 이 화면에서 통합 요약됩니다." />
      ) : null}
      {summary && hasAnyData ? <EducationStatusContent summary={summary} /> : null}
    </section>
  );
}

function EducationStatusContent({ summary }: { summary: EducationStatusSummary }) {
  const requiredStudyPercent = summary.learning.totalRequiredStudyCount > 0
    ? Math.round((summary.learning.completedRequiredStudyCount / summary.learning.totalRequiredStudyCount) * 100)
    : 0;

  return (
    <>
      <div className="summary-grid">
        <StatusCard title="이번 달 출석" value={`${summary.attendance.presentDays}일`} detail={`${summary.attendance.month} · 지각 ${summary.attendance.lateDays} · 결석 ${summary.attendance.absentDays}`} href="/mycampus/attendance" />
        <StatusCard title="학습 진행" value={`${summary.learning.inProgressElearningCount}개`} detail={`이수 ${requiredStudyPercent}% · 다시보기 ${summary.learning.replayWatchMinutes}분`} href="/mycampus/elearning" />
        <StatusCard title="Quest" value={`${summary.quests.openCount}개 진행`} detail={`제출 ${summary.quests.submittedCount} · 지연 ${summary.quests.lateCount}`} href="/quest" />
        <StatusCard title="포인트" value={summary.points.levelName} detail={`EXP ${summary.points.experiencePoint.toLocaleString('ko-KR')} · 장학 ${summary.points.scholarshipPoint}점`} href="/mycampus/level" />
      </div>
      <div className="content-grid">
        <section className="panel">
          <h2>출결 요약</h2>
          <dl className="info-list">
            <InfoRow label="정상 출석" value={`${summary.attendance.presentDays}일`} />
            <InfoRow label="지각" value={`${summary.attendance.lateDays}일`} />
            <InfoRow label="결석" value={`${summary.attendance.absentDays}일`} />
            <InfoRow label="처리 대기 이의신청" value={`${summary.attendance.appealPendingCount}건`} />
          </dl>
        </section>
        <section className="panel">
          <h2>학습/평가 요약</h2>
          <dl className="info-list">
            <InfoRow label="학습중 이러닝" value={`${summary.learning.inProgressElearningCount}개`} />
            <InfoRow label="이수 콘텐츠" value={`${summary.learning.completedRequiredStudyCount}/${summary.learning.totalRequiredStudyCount}`} />
            <InfoRow label="누적 학습 추정" value={`${summary.learning.replayWatchMinutes}분`} />
            <InfoRow label="미제출 지연 Quest" value={`${summary.quests.lateCount}개`} />
          </dl>
        </section>
      </div>
    </>
  );
}

function StatusCard({ title, value, detail, href }: { title: string; value: string; detail: string; href: string }) {
  return (
    <section className="stat-card">
      <span>{title}</span>
      <strong>{value}</strong>
      <p>{detail}</p>
      <a className="text-link" href={href}>상세 보기</a>
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

export default EducationStatusPage;
