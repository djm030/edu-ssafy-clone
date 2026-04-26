import { useEffect, useMemo, useState } from 'react';
import { getCurriculumWeek, getCurriculumWeeks } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { CurriculumWeek, LoadState } from '../types';

const statusMap = {
  current: { label: '진행중', tone: 'yellow' },
  done: { label: '완료', tone: 'green' },
  planned: { label: '예정', tone: 'gray' },
} as const;

const statusOptions = [
  { value: '', label: '전체 상태' },
  { value: 'current', label: '진행중' },
  { value: 'planned', label: '예정' },
  { value: 'done', label: '완료' },
];

function unique(values: Array<string | null | undefined>) {
  return Array.from(new Set(values.filter((value): value is string => Boolean(value))));
}

interface CurriculumPageProps {
  weekId?: number;
}

function CurriculumPage({ weekId }: CurriculumPageProps) {
  const [items, setItems] = useState<CurriculumWeek[]>([]);
  const [semester, setSemester] = useState('');
  const [track, setTrack] = useState('');
  const [status, setStatus] = useState('');
  const [detail, setDetail] = useState<CurriculumWeek>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    setErrorMessage('');

    const request = weekId
      ? getCurriculumWeek(weekId).then((item) => ({ items: [item], detail: item }))
      : getCurriculumWeeks({ semester, track, status }).then((response) => ({ ...response, detail: undefined }));

    request
      .then((response) => {
        if (ignore) return;
        setItems(response.items);
        setDetail(response.detail);
        setLoadState(response.items.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setItems([]);
        setDetail(undefined);
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [semester, track, status, weekId]);

  const semesters = useMemo(() => unique(items.map((item) => item.semester)), [items]);
  const tracks = useMemo(() => unique(items.map((item) => item.track)), [items]);

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="주차별 커리큘럼" description="학기, 트랙, 주차별 수업과 진행 상태를 확인합니다." />

      {!weekId ? (
        <div className="toolbar" aria-label="커리큘럼 필터">
          <select value={semester} onChange={(event) => setSemester(event.target.value)} aria-label="학기 선택">
            <option value="">전체 학기</option>
            {semesters.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
          <select value={track} onChange={(event) => setTrack(event.target.value)} aria-label="트랙 선택">
            <option value="">전체 트랙</option>
            {tracks.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
          <select value={status} onChange={(event) => setStatus(event.target.value)} aria-label="상태 선택">
            {statusOptions.map((option) => (
              <option key={option.value || 'all'} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>
      ) : null}

      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="커리큘럼을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="조건에 맞는 커리큘럼이 없습니다." message="학기, 트랙, 상태 필터를 조정해 보세요." /> : null}
      {loadState === 'loaded' && detail ? <CurriculumDetail item={detail} /> : null}
      {loadState === 'loaded' && !detail ? (
        <>
          <CurriculumSessionDensityPanel items={items} />
          <div className="card-list">
            {items.map((item) => {
              const statusInfo = statusMap[item.status];
              return (
                <article className="list-card curriculum-card" key={item.id}>
                  <div>
                    <p className="eyebrow">{item.semester || '학기 미지정'} · {item.track || '공통'} · {item.week}주차</p>
                    <h2>{item.title}</h2>
                    <p>{item.period}</p>
                    <p className="muted">총 {item.sessionCount || item.sessions?.length || item.lessons.length}개 세션</p>
                    <CurriculumMiniSchedule item={item} />
                    <div className="action-row">
                      <a className="text-link" href={`/learning/curriculum/${item.id}`}>주차 상세 보기</a>
                    </div>
                  </div>
                  <StatusPill tone={statusInfo.tone}>{statusInfo.label}</StatusPill>
                </article>
              );
            })}
          </div>
        </>
      ) : null}
    </section>
  );
}

function CurriculumSessionDensityPanel({ items }: { items: CurriculumWeek[] }) {
  const sessions = items.flatMap((item) => normalizedSessions(item));
  const morningCount = sessions.filter((session) => sessionSlotLabel(session) === '오전').length;
  const afternoonCount = sessions.filter((session) => sessionSlotLabel(session) === '오후').length;
  const assignmentCount = sessions.filter((session) => session.sessionType?.toLowerCase().includes('assignment') || session.title.includes('과제')).length;

  return (
    <section className="panel curriculum-density-panel" aria-label="커리큘럼 오전 오후 과제 교재 Meeting 요약">
      <div className="section-heading compact-heading">
        <div>
          <p>WEEKLY SCHEDULE</p>
          <h2>시간표 밀도 요약</h2>
        </div>
        <span>{items.length}개 주차 · {sessions.length}개 세션</span>
      </div>
      <div className="curriculum-density-grid">
        <MetricCard label="오전 세션" value={`${morningCount}개`} />
        <MetricCard label="오후 세션" value={`${afternoonCount}개`} />
        <MetricCard label="과제/실습" value={`${assignmentCount}개`} />
        <MetricCard label="교재/Meeting" value="상세에서 확인" />
      </div>
    </section>
  );
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="curriculum-density-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function CurriculumMiniSchedule({ item }: { item: CurriculumWeek }) {
  const sessions = normalizedSessions(item).slice(0, 4);
  return (
    <div className="curriculum-mini-schedule" aria-label={`${item.week}주차 오전 오후 시간표`}>
      {sessions.map((session) => (
        <div className="curriculum-time-block" key={session.id}>
          <strong>{sessionSlotLabel(session)}</strong>
          <span>{[session.period, session.title].filter(Boolean).join(' · ')}</span>
          <small>{[session.instructor, session.location || 'Meeting/강의실 추후 공지'].filter(Boolean).join(' · ')}</small>
        </div>
      ))}
    </div>
  );
}


interface CurriculumDetailProps {
  item: CurriculumWeek;
}

function CurriculumDetail({ item }: CurriculumDetailProps) {
  const statusInfo = statusMap[item.status];
  const sessions = item.sessions?.length
    ? item.sessions
    : item.lessons.map((lesson, index) => ({
      id: index + 1,
      title: lesson,
      date: item.period,
      period: '',
      instructor: '',
      location: '',
      sessionType: 'LESSON',
    }));

  return (
    <article className="panel detail-panel" aria-labelledby="curriculum-detail-title">
      <div className="detail-header">
        <div>
          <p className="eyebrow">{item.semester || '학기 미지정'} · {item.track || '공통'} · {item.week}주차</p>
          <h2 id="curriculum-detail-title">{item.week}주차 · {item.title}</h2>
          <p>{item.period}</p>
        </div>
        <StatusPill tone={statusInfo.tone}>{statusInfo.label}</StatusPill>
      </div>

      <dl className="info-list detail-info" aria-label="커리큘럼 상세 정보">
        <div>
          <dt>학기</dt>
          <dd>{item.semester || '미지정'}</dd>
        </div>
        <div>
          <dt>트랙</dt>
          <dd>{item.track || '공통'}</dd>
        </div>
        <div>
          <dt>기간</dt>
          <dd>{item.period}</dd>
        </div>
        <div>
          <dt>세션 수</dt>
          <dd>{item.sessionCount || sessions.length}개</dd>
        </div>
      </dl>

      <section aria-label="일자별 시간표" className="detail-section">
        <h3>일자별 시간표</h3>
        <div className="curriculum-timetable" role="table" aria-label="커리큘럼 상세 시간표">
          <div className="curriculum-timetable-row table-head" role="row">
            <span role="columnheader">일자</span>
            <span role="columnheader">구분</span>
            <span role="columnheader">시간/제목</span>
            <span role="columnheader">강사/장소</span>
            <span role="columnheader">교재/Meeting</span>
          </div>
          {sessions.map((session) => (
            <div className="curriculum-timetable-row" key={session.id} role="row">
              <span role="cell">{session.date || item.period}</span>
              <strong role="cell">{sessionSlotLabel(session)}</strong>
              <span role="cell">{[session.period, session.title].filter(Boolean).join(' · ')}</span>
              <span role="cell">{[session.instructor, session.location].filter(Boolean).join(' · ') || '담당자/장소는 추후 안내됩니다.'}</span>
              <span role="cell">{session.sessionType || '교재/Meeting 추후 공지'}</span>
            </div>
          ))}
        </div>
      </section>

      <div className="action-row">
        <a className="secondary-action" href="/learning/curriculum">목록으로 돌아가기</a>
      </div>
    </article>
  );
}

function normalizedSessions(item: CurriculumWeek) {
  return item.sessions?.length
    ? item.sessions
    : item.lessons.map((lesson, index) => ({
      id: index + 1,
      title: lesson,
      date: item.period,
      period: index === 0 ? '09:00-12:00' : '13:00-18:00',
      instructor: '',
      location: '',
      sessionType: 'LESSON',
    }));
}

function sessionSlotLabel(session: { period?: string | null; sessionType?: string | null; title: string }) {
  const text = `${session.period || ''} ${session.sessionType || ''} ${session.title}`.toLowerCase();
  if (text.includes('assignment') || text.includes('과제')) return '과제';
  if (text.includes('pm') || text.includes('오후') || /1[3-9]:|2[0-3]:/.test(text)) return '오후';
  if (text.includes('am') || text.includes('오전') || /0[8-9]:|1[0-2]:/.test(text)) return '오전';
  return session.sessionType || '세션';
}

export default CurriculumPage;
