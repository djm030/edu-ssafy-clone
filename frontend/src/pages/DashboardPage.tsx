import { useEffect, useState, type ReactNode } from 'react';
import { getDashboardSummary } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import type {
  DashboardBoardPost,
  DashboardCurriculumSession,
  DashboardEbookCard,
  DashboardLearningCard,
  DashboardQuestCard,
  DashboardSummary,
  LoadState,
} from '../types';

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
      <PageHeader eyebrow="MAIN" title="대시보드" description="출석, 학습, 커뮤니티, 공지 정보를 EduSSAFY 홈처럼 한 화면에서 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? (
        <DataState title="대시보드를 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} />
      ) : null}
      {summary ? <DashboardContent summary={summary} /> : null}
    </section>
  );
}

function DashboardContent({ summary }: { summary: DashboardSummary }) {
  const expPercent = summary.level.nextLevelExp > 0 ? Math.min(100, Math.round((summary.level.exp / summary.level.nextLevelExp) * 100)) : 0;
  const rankText = summary.level.rank == null ? '집계 대기' : `${summary.level.rank}위`;

  return (
    <>
      <div className="summary-grid">
        <StatCard title="출석" value={`${summary.attendance.present}일`} detail={`지각 ${summary.attendance.late} · 결석 ${summary.attendance.absent}`} />
        <StatCard title="장학포인트" value={`${summary.level.scholarshipPoints}점`} detail={`현재 순위 ${rankText}`} />
        <StatCard title="레벨&경험치" value={`Lv.${summary.level.level}`} detail={`경험치 ${expPercent}% · 다음 ${summary.level.nextLevelExp}EXP`} />
        <StatCard title="읽지 않은 알림" value={`${summary.notifications.unreadCount}건`} detail="필독/학습 알림 확인 필요" />
      </div>

      <div className="content-grid dashboard-home-grid">
        <section className="panel dashboard-attendance-panel">
          <PanelHeader title="출석체크 & 현황" moreHref={summary.home.attendanceCheck.detailPath} />
          <div className="dashboard-attendance-card">
            <span className={`status-pill ${summary.home.attendanceCheck.checkInAvailable ? 'green' : 'gray'}`}>{summary.home.attendanceCheck.statusText}</span>
            <strong>{summary.home.attendanceCheck.todayLabel}</strong>
            <p>{summary.home.attendanceCheck.message}</p>
            <a className="primary-action" href={summary.home.attendanceCheck.detailPath}>출석현황 보기</a>
          </div>
        </section>

        <section className="panel">
          <PanelHeader title="알림" moreHref="/mycampus/notifications" />
          {summary.notifications.latest.length === 0 ? (
            <DataState title="새 알림이 없습니다." message="필독 알림과 학습 알림이 도착하면 여기에 표시됩니다." />
          ) : (
            <ul className="dashboard-mini-list">
              {summary.notifications.latest.map((item) => <li key={item}>{item}</li>)}
            </ul>
          )}
        </section>

        <section className="panel dashboard-wide-panel">
          <PanelHeader title="주차별 커리큘럼" moreHref="/learning/curriculum" />
          {summary.home.curriculumOverview ? (
            <a className="dashboard-curriculum-overview" href={summary.home.curriculumOverview.detailPath}>
              <span className={`status-pill ${summary.home.curriculumOverview.status === 'current' ? 'green' : 'blue'}`}>
                {formatCurriculumStatus(summary.home.curriculumOverview.status)}
              </span>
              <strong>{summary.home.curriculumOverview.weekNumber ? `${summary.home.curriculumOverview.weekNumber}주차` : '주차 미정'} · {summary.home.curriculumOverview.track || '공통'}</strong>
              <p>{summary.home.curriculumOverview.semester || '학기 미정'} · {formatDateRange(summary.home.curriculumOverview.startsAt, summary.home.curriculumOverview.endsAt)} · {summary.home.curriculumOverview.sessionCount}개 세션</p>
            </a>
          ) : null}
          {summary.home.curriculumSessions.length === 0 ? (
            <DataState title="표시할 커리큘럼이 없습니다." message="학기/트랙 시간표가 등록되면 홈에 노출됩니다." />
          ) : (
            <div className="simple-table dashboard-table" role="table" aria-label="홈 주차별 커리큘럼">
              <div role="row">
                <span role="columnheader">주차</span>
                <span role="columnheader">일자/시간</span>
                <span role="columnheader">강의</span>
                <span role="columnheader">장소</span>
              </div>
              {summary.home.curriculumSessions.map((item) => <CurriculumRow key={item.id} item={item} />)}
            </div>
          )}
        </section>

        <DashboardCardList title="Quest/평가" moreHref="/quest" items={summary.home.quests} emptyTitle="진행 중인 Quest/평가가 없습니다." renderItem={(item) => <QuestCard key={item.id} item={item} />} />
        <DashboardCardList title="학습자료" moreHref="/learning/materials" items={summary.home.materials} emptyTitle="등록된 학습자료가 없습니다." listClassName="dashboard-material-carousel" renderItem={(item) => <LearningCard key={item.id} item={item} showResourceMetric />} />
        <DashboardCardList title="학습중 이러닝" moreHref="/mycampus/elearning" items={summary.home.elearnings} emptyTitle="학습중인 이러닝이 없습니다." renderItem={(item) => <LearningCard key={item.id} item={item} showProgress />} />
        <DashboardCardList title="자유게시판" moreHref="/community/free" items={summary.home.freePosts} emptyTitle="최근 자유게시글이 없습니다." renderItem={(item) => <BoardPostCard key={item.id} item={item} />} />
        <DashboardCardList title="e-book" moreHref="/mycampus/ebooks" items={summary.home.ebooks} emptyTitle="표시할 e-book이 없습니다." renderItem={(item) => <EbookCard key={item.id} item={item} />} />
        <DashboardCardList title="공지사항" moreHref="/help/notice" items={summary.home.notices} emptyTitle="등록된 공지사항이 없습니다." renderItem={(item) => <BoardPostCard key={item.id} item={item} />} />
      </div>
    </>
  );
}

function PanelHeader({ title, moreHref }: { title: string; moreHref: string }) {
  return (
    <div className="section-heading compact-heading">
      <h2>{title}</h2>
      <a className="text-link" href={moreHref}>More</a>
    </div>
  );
}

function DashboardCardList<T>({
  title,
  moreHref,
  items,
  emptyTitle,
  renderItem,
  listClassName = 'card-list compact-list',
}: {
  title: string;
  moreHref: string;
  items: T[];
  emptyTitle: string;
  renderItem: (item: T) => ReactNode;
  listClassName?: string;
}) {
  return (
    <section className="panel">
      <PanelHeader title={title} moreHref={moreHref} />
      {items.length === 0 ? (
        <DataState title={emptyTitle} message="데이터가 등록되면 홈 위젯에서 바로 확인할 수 있습니다." />
      ) : (
        <div className={listClassName}>{items.map(renderItem)}</div>
      )}
    </section>
  );
}

function CurriculumRow({ item }: { item: DashboardCurriculumSession }) {
  return (
    <a role="row" href={item.detailPath}>
      <span role="cell">{item.weekNumber ? `${item.weekNumber}주차` : '-'}</span>
      <span role="cell">{[item.date, item.period].filter(Boolean).join(' · ') || '-'}</span>
      <span role="cell"><strong>{item.title}</strong><small>{item.instructor || '강사 미정'}</small></span>
      <span role="cell">{item.location || '-'}</span>
    </a>
  );
}

function QuestCard({ item }: { item: DashboardQuestCard }) {
  return (
    <a className="dashboard-widget-card" href={item.detailPath}>
      <strong>{item.title}</strong>
      <p>{item.type || 'Quest'} · {formatStatus(item.status)}</p>
      <small>{formatPeriod(item.startAt, item.endAt)}</small>
    </a>
  );
}

function LearningCard({ item, showProgress = false, showResourceMetric = false }: { item: DashboardLearningCard; showProgress?: boolean; showResourceMetric?: boolean }) {
  return (
    <a className="dashboard-widget-card" href={item.detailPath}>
      <strong>{item.title}</strong>
      <p>{item.category || '학습'}{item.description ? ` · ${item.description}` : ''}</p>
      {showProgress ? (
        <div className="progress-track" aria-label={`${item.title} 진행률 ${item.progressPercent}%`}><span style={{ width: `${item.progressPercent}%` }} /></div>
      ) : showResourceMetric ? (
        <small>자료 {item.resourceCount ?? 0}개 · 조회 {item.viewCount} · 좋아요 {item.likeCount} · 찜 {item.bookmarkCount} · 상세보기</small>
      ) : (
        <small>조회 {item.viewCount} · 좋아요 {item.likeCount} · 찜 {item.bookmarkCount}</small>
      )}
    </a>
  );
}

function BoardPostCard({ item }: { item: DashboardBoardPost }) {
  return (
    <a className="dashboard-widget-card" href={item.detailPath}>
      <strong>{item.pinned ? '[필독] ' : ''}{item.title}</strong>
      <p>{item.authorLabel || '운영자'} · {item.createdAt ? item.createdAt.slice(0, 10) : '-'}</p>
    </a>
  );
}

function EbookCard({ item }: { item: DashboardEbookCard }) {
  return (
    <a className="dashboard-widget-card" href={item.detailPath}>
      <strong>{item.title}</strong>
      <p>{item.category || 'e-book'}{item.description ? ` · ${item.description}` : ''}</p>
    </a>
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

function formatStatus(value?: string | null) {
  if (!value) return '예정';
  const normalized = value.toLowerCase();
  if (normalized === 'submitted' || normalized === 'done') return '제출 완료';
  if (normalized === 'graded') return '채점 완료';
  if (normalized === 'progress' || normalized === 'in_progress') return '진행 중';
  if (normalized === 'overdue') return '마감';
  return value;
}

function formatCurriculumStatus(value?: string | null) {
  if (value === 'current') return '진행 중';
  if (value === 'done') return '완료';
  if (value === 'planned') return '예정';
  return value || '예정';
}

function formatDateRange(startAt?: string | null, endAt?: string | null) {
  const start = startAt?.slice(0, 10);
  const end = endAt?.slice(0, 10);
  if (start && end && start !== end) return `${start} ~ ${end}`;
  return start || end || '일정 미정';
}

function formatPeriod(startAt?: string | null, endAt?: string | null) {
  const start = startAt?.slice(0, 10);
  const end = endAt?.slice(0, 10);
  if (start && end) return `${start} ~ ${end}`;
  return start || end || '기간 미정';
}

export default DashboardPage;
