import { FormEvent, useEffect, useState } from 'react';
import { applyMentoringMeeting, cancelMentoringMeetingApplication, getMentoringMeeting, getMentoringMeetings, getMyMentoringMeetingApplications } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, MentoringMeetingApplicationItem, MentoringMeetingItem, PageMeta } from '../types';

function MentoringMeetingsPage({ meetingId, mode }: { meetingId?: number; mode?: 'my-applications' }) {
  if (mode === 'my-applications') return <MyMeetingApplications />;
  if (meetingId) return <MentoringMeetingDetail meetingId={meetingId} />;
  return <MentoringMeetingList />;
}

function MentoringMeetingList() {
  const [meetings, setMeetings] = useState<MentoringMeetingItem[]>([]);
  const [page, setPage] = useState<PageMeta>({ page: 1, size: 20, totalItems: 0, totalPages: 0 });
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringMeetings({ keyword, page: page.page, size: page.size })
      .then((response) => {
        if (ignore) return;
        setMeetings(response.items);
        setPage(response.page);
        setLoadState(response.items.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => { ignore = true; };
  }, [keyword, page.page, page.size]);

  const submitSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setPage((current) => ({ ...current, page: 1 }));
    setKeyword(keywordInput.trim());
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="간담회 신청" description="모집 중인 멘토링 간담회를 확인하고 로그인 사용자 기준으로 신청합니다." />
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submitSearch}>
          <label htmlFor="mentoring-meeting-keyword">간담회 검색</label>
          <input id="mentoring-meeting-keyword" onChange={(event) => setKeywordInput(event.target.value)} placeholder="주제, 제목, 설명 검색" value={keywordInput} />
          <button className="primary-action" type="submit">검색</button>
          <a className="ghost-button" href="/mentoring/meetings/my-applications">내 신청 보기</a>
        </form>
      </section>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="간담회를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="간담회가 없습니다." message="다른 검색어를 입력해 주세요." /> : null}
      {loadState === 'loaded' ? <MeetingCards meetings={meetings} /> : null}
    </section>
  );
}

function MeetingCards({ meetings }: { meetings: MentoringMeetingItem[] }) {
  return (
    <section className="grid-list">
      {meetings.map((meeting) => (
        <article className="panel card" key={meeting.id}>
          <StatusPill tone={meeting.status === 'RECRUITING' ? 'green' : 'gray'}>{meetingStatusLabel(meeting.status)}</StatusPill>
          <h2><a href={`/mentoring/meetings/${meeting.id}`}>{meeting.title}</a></h2>
          <p>{meeting.description}</p>
          <span className="muted">{meeting.topic} · {meeting.meetingType} · {meeting.appliedCount}/{meeting.capacity}명 · {formatDate(meeting.startsAt)}</span>
        </article>
      ))}
    </section>
  );
}

function MentoringMeetingDetail({ meetingId }: { meetingId: number }) {
  const [meeting, setMeeting] = useState<MentoringMeetingItem>();
  const [motivation, setMotivation] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringMeeting(meetingId)
      .then((response) => {
        if (ignore) return;
        setMeeting(response);
        setMotivation(response.myMotivation || '');
        setLoadState('loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => { ignore = true; };
  }, [meetingId]);

  const apply = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setActionMessage('');
    applyMentoringMeeting(meetingId, motivation)
      .then((application) => {
        setMeeting((current) => current ? { ...current, myApplicationStatus: application.status, myMotivation: application.motivation, appliedCount: current.appliedCount + 1 } : current);
        setActionMessage('간담회 신청이 완료되었습니다.');
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  const cancel = () => {
    setActionMessage('');
    cancelMentoringMeetingApplication(meetingId)
      .then(() => {
        setMeeting((current) => current ? { ...current, myApplicationStatus: null, myMotivation: null, appliedCount: Math.max(0, current.appliedCount - 1) } : current);
        setActionMessage('간담회 신청을 취소했습니다.');
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="간담회 상세" description="모집 기간, 정원, 장소를 확인하고 신청 또는 취소합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="간담회를 불러오지 못했습니다." message={errorMessage} /> : null}
      {meeting ? (
        <article className="panel article-detail">
          <StatusPill tone={meeting.status === 'RECRUITING' ? 'green' : 'gray'}>{meetingStatusLabel(meeting.status)}</StatusPill>
          <h2>{meeting.title}</h2>
          <p>{meeting.description}</p>
          <p className="muted">{meeting.topic} · {meeting.meetingType} · {meeting.location || '장소 미정'} · 신청 {meeting.appliedCount}/{meeting.capacity}명</p>
          <p className="muted">행사: {formatDateTime(meeting.startsAt)} ~ {formatDateTime(meeting.endsAt)}</p>
          <p className="muted">신청: {formatDateTime(meeting.applicationStartsAt)} ~ {formatDateTime(meeting.applicationEndsAt)}</p>
          {meeting.myApplicationStatus ? (
            <section className="stack-form">
              <StatusPill tone="blue">내 신청 상태: {applicationStatusLabel(meeting.myApplicationStatus)}</StatusPill>
              <p>{meeting.myMotivation}</p>
              <button className="ghost-button" onClick={cancel} type="button">신청 취소</button>
            </section>
          ) : (
            <form className="stack-form" onSubmit={apply}>
              <label htmlFor="meeting-motivation">신청 동기</label>
              <textarea id="meeting-motivation" maxLength={1000} required rows={4} value={motivation} onChange={(event) => setMotivation(event.target.value)} />
              <button className="primary-action" disabled={meeting.status !== 'RECRUITING'} type="submit">간담회 신청</button>
            </form>
          )}
          {actionMessage ? <p className="muted">{actionMessage}</p> : null}
          <a className="ghost-button" href="/mentoring/meetings">목록으로</a>
        </article>
      ) : null}
    </section>
  );
}

function MyMeetingApplications() {
  const [items, setItems] = useState<MentoringMeetingApplicationItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMyMentoringMeetingApplications()
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

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="내 간담회 신청" description="로그인 사용자 기준 간담회 신청 상태를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="신청 내역을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="신청한 간담회가 없습니다." message="모집 중인 간담회를 확인해 주세요." /> : null}
      {loadState === 'loaded' ? (
        <section className="grid-list">
          {items.map((item) => (
            <article className="panel card" key={item.meetingId}>
              <StatusPill tone="blue">{applicationStatusLabel(item.status)}</StatusPill>
              <h2><a href={`/mentoring/meetings/${item.meetingId}`}>{item.meetingTitle}</a></h2>
              <p>{item.motivation}</p>
              <span className="muted">신청일 {formatDateTime(item.appliedAt)}</span>
            </article>
          ))}
        </section>
      ) : null}
    </section>
  );
}

function meetingStatusLabel(status: MentoringMeetingItem['status']): string {
  if (status === 'RECRUITING') return '모집 중';
  if (status === 'DONE') return '종료';
  return '모집 마감';
}

function applicationStatusLabel(status: MentoringMeetingApplicationItem['status']): string {
  if (status === 'CANCELLED') return '취소';
  if (status === 'SELECTED') return '선정';
  if (status === 'REJECTED') return '미선정';
  return '신청 완료';
}

function formatDate(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(date);
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default MentoringMeetingsPage;
