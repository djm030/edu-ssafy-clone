import { useEffect, useMemo, useState } from 'react';
import { getCurriculumWeeks } from '../api/app';
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

function CurriculumPage() {
  const [items, setItems] = useState<CurriculumWeek[]>([]);
  const [semester, setSemester] = useState('');
  const [track, setTrack] = useState('');
  const [status, setStatus] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getCurriculumWeeks({ semester, track, status })
      .then((response) => {
        if (ignore) return;
        setItems(response.items);
        setLoadState(response.items.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [semester, track, status]);

  const semesters = useMemo(() => unique(items.map((item) => item.semester)), [items]);
  const tracks = useMemo(() => unique(items.map((item) => item.track)), [items]);

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="주차별 커리큘럼" description="학기, 트랙, 주차별 수업과 진행 상태를 확인합니다." />

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

      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="커리큘럼을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="조건에 맞는 커리큘럼이 없습니다." message="학기, 트랙, 상태 필터를 조정해 보세요." /> : null}
      {loadState === 'loaded' ? (
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
                  <ul>
                    {(item.sessions?.length ? item.sessions : []).map((session) => (
                      <li key={session.id}>
                        {[session.date, session.period, session.title, session.instructor, session.location, session.sessionType]
                          .filter(Boolean)
                          .join(' · ')}
                      </li>
                    ))}
                    {!item.sessions?.length
                      ? item.lessons.map((lesson) => <li key={lesson}>{lesson}</li>)
                      : null}
                  </ul>
                </div>
                <StatusPill tone={statusInfo.tone}>{statusInfo.label}</StatusPill>
              </article>
            );
          })}
        </div>
      ) : null}
    </section>
  );
}

export default CurriculumPage;
