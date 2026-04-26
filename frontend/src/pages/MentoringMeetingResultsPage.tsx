import { FormEvent, useEffect, useState } from 'react';
import {
  createMentoringMeetingReview,
  deleteMentoringMeetingReview,
  getMentoringMeetingResult,
  getMentoringMeetingResults,
  getMentoringMeetingReview,
  getMentoringMeetingReviews,
  updateMentoringMeetingReview,
} from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, MentoringMeetingResultDetail, MentoringMeetingResultItem, MentoringMeetingReviewDetail, MentoringMeetingReviewItem } from '../types';

type Mode = 'results' | 'result-detail' | 'reviews' | 'review-write' | 'review-detail';

function MentoringMeetingResultsPage({ mode = 'results', meetingId, reviewId }: { mode?: Mode; meetingId?: number; reviewId?: number }) {
  if (mode === 'result-detail' && meetingId) return <ResultDetail meetingId={meetingId} />;
  if (mode === 'reviews') return <ReviewList />;
  if (mode === 'review-write') return <ReviewEditor />;
  if (mode === 'review-detail' && reviewId) return <ReviewDetail reviewId={reviewId} />;
  return <ResultList />;
}

function ResultList() {
  const [items, setItems] = useState<MentoringMeetingResultItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringMeetingResults()
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
    return () => { ignore = true; };
  }, []);

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="간담회 정보" description="종료된 간담회의 요약, 참여 인원, 후기 평점을 확인합니다." />
      <a className="ghost-button" href="/mentoring/meeting-reviews">간담회 후기 보기</a>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="간담회 정보를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="등록된 간담회 정보가 없습니다." message="종료된 간담회 결과가 등록되면 표시됩니다." /> : null}
      {loadState === 'loaded' ? (
        <section className="grid-list">
          {items.map((item) => <ResultCard item={item} key={item.resultId} />)}
        </section>
      ) : null}
    </section>
  );
}

function ResultCard({ item }: { item: MentoringMeetingResultItem }) {
  return (
    <article className="panel card">
      <StatusPill tone="blue">후기 {item.reviewCount}개 · ★ {item.averageRating.toFixed(1)}</StatusPill>
      <h2><a href={`/mentoring/meeting-results/${item.meetingId}`}>{item.title}</a></h2>
      <p>{item.summary}</p>
      <span className="muted">참여 {item.participantCount}명 · 종료 {formatDateTime(item.endedAt)}</span>
    </article>
  );
}

function ResultDetail({ meetingId }: { meetingId: number }) {
  const [result, setResult] = useState<MentoringMeetingResultDetail>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringMeetingResult(meetingId)
      .then((response) => {
        if (ignore) return;
        setResult(response);
        setLoadState('loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => { ignore = true; };
  }, [meetingId]);

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="간담회 정보 상세" description="간담회 결과와 교육생 후기를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="간담회 정보를 불러오지 못했습니다." message={errorMessage} /> : null}
      {result ? (
        <article className="panel article-detail">
          <StatusPill tone="blue">참여 {result.participantCount}명 · 평균 ★ {result.averageRating.toFixed(1)}</StatusPill>
          <h2>{result.title}</h2>
          <p>{result.content}</p>
          <p className="muted">일시: {formatDateTime(result.startsAt)} ~ {formatDateTime(result.endedAt)}</p>
          <a className="primary-action" href={`/mentoring/meeting-reviews/write?meetingId=${result.meetingId}`}>후기 작성</a>
          <section className="grid-list">
            {result.reviews.length ? result.reviews.map((review) => <ReviewCard item={review} key={review.id} />) : <DataState title="아직 후기가 없습니다." message="간담회에 참여했다면 첫 후기를 남겨 보세요." />}
          </section>
        </article>
      ) : null}
    </section>
  );
}

function ReviewList() {
  const [items, setItems] = useState<MentoringMeetingReviewItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringMeetingReviews()
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
    return () => { ignore = true; };
  }, []);

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="간담회 후기" description="참여 교육생이 남긴 간담회 후기를 모아봅니다." />
      <a className="primary-action" href="/mentoring/meeting-reviews/write">후기 작성</a>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="후기를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="등록된 후기가 없습니다." message="종료된 간담회 상세에서 후기를 작성해 주세요." /> : null}
      {loadState === 'loaded' ? <section className="grid-list">{items.map((item) => <ReviewCard item={item} key={item.id} />)}</section> : null}
    </section>
  );
}

function ReviewCard({ item }: { item: MentoringMeetingReviewItem }) {
  return (
    <article className="panel card">
      <StatusPill tone="green">★ {item.rating}</StatusPill>
      <h2><a href={`/mentoring/meeting-reviews/${item.id}`}>{item.title}</a></h2>
      <p>{item.excerpt}</p>
      <span className="muted">{item.meetingTitle} · {item.authorName} · {formatDateTime(item.createdAt)}</span>
    </article>
  );
}

function ReviewEditor({ review }: { review?: MentoringMeetingReviewDetail }) {
  const queryMeetingId = Number(new URLSearchParams(window.location.search).get('meetingId') || review?.meetingId || 0);
  const [meetingId, setMeetingId] = useState(queryMeetingId || review?.meetingId || 0);
  const [title, setTitle] = useState(review?.title || '');
  const [content, setContent] = useState(review?.content || '');
  const [rating, setRating] = useState(review?.rating || 5);
  const [message, setMessage] = useState('');

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setMessage('');
    const action = review
      ? updateMentoringMeetingReview(review.id, { title, content, rating })
      : createMentoringMeetingReview({ meetingId, title, content, rating });
    action
      .then((saved) => { window.location.href = `/mentoring/meeting-reviews/${saved.id}`; })
      .catch((error) => setMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title={review ? '간담회 후기 수정' : '간담회 후기 작성'} description="참여한 종료 간담회에 대해 평점과 후기를 남깁니다." />
      <form className="panel stack-form" onSubmit={submit}>
        <label htmlFor="meeting-review-meeting-id">간담회 ID</label>
        <input id="meeting-review-meeting-id" min={1} onChange={(event) => setMeetingId(Number(event.target.value))} required type="number" value={meetingId || ''} />
        <label htmlFor="meeting-review-title">제목</label>
        <input id="meeting-review-title" maxLength={120} onChange={(event) => setTitle(event.target.value)} required value={title} />
        <label htmlFor="meeting-review-rating">평점</label>
        <select id="meeting-review-rating" onChange={(event) => setRating(Number(event.target.value))} value={rating}>
          {[5, 4, 3, 2, 1].map((score) => <option key={score} value={score}>{score}점</option>)}
        </select>
        <label htmlFor="meeting-review-content">후기</label>
        <textarea id="meeting-review-content" maxLength={4000} onChange={(event) => setContent(event.target.value)} required rows={8} value={content} />
        <button className="primary-action" type="submit">저장</button>
        {message ? <p className="muted">{message}</p> : null}
      </form>
    </section>
  );
}

function ReviewDetail({ reviewId }: { reviewId: number }) {
  const [review, setReview] = useState<MentoringMeetingReviewDetail>();
  const [editing, setEditing] = useState(false);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringMeetingReview(reviewId)
      .then((response) => {
        if (ignore) return;
        setReview(response);
        setLoadState('loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => { ignore = true; };
  }, [reviewId]);

  const remove = () => {
    setActionMessage('');
    deleteMentoringMeetingReview(reviewId)
      .then(() => { window.location.href = '/mentoring/meeting-reviews'; })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  if (editing && review) return <ReviewEditor review={review} />;

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="간담회 후기 상세" description="작성자의 후기와 평점을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="후기를 불러오지 못했습니다." message={errorMessage} /> : null}
      {review ? (
        <article className="panel article-detail">
          <StatusPill tone="green">★ {review.rating}</StatusPill>
          <h2>{review.title}</h2>
          <p>{review.content}</p>
          <p className="muted">{review.meetingTitle} · {review.authorName} · {formatDateTime(review.createdAt)}</p>
          {review.editable ? <button className="ghost-button" onClick={() => setEditing(true)} type="button">수정</button> : null}
          {review.editable ? <button className="ghost-button" onClick={remove} type="button">삭제</button> : null}
          {actionMessage ? <p className="muted">{actionMessage}</p> : null}
        </article>
      ) : null}
    </section>
  );
}

function formatDateTime(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default MentoringMeetingResultsPage;
