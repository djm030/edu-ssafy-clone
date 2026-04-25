import { useEffect, useState } from 'react';
import { getQuest, getQuestSubmission } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, QuestItem, QuestSubmissionResult } from '../types';

const statusMap = {
  done: { label: '완료', tone: 'green' },
  graded: { label: '채점완료', tone: 'blue' },
  progress: { label: '진행중', tone: 'yellow' },
} as const;

function QuestDetailPage({ questId }: { questId: number }) {
  const [quest, setQuest] = useState<QuestItem>();
  const [submission, setSubmission] = useState<QuestSubmissionResult>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    Promise.all([
      getQuest(questId),
      getQuestSubmission(questId).catch(() => undefined),
    ])
      .then(([response, submissionResponse]) => {
        if (ignore) return;
        setQuest(response);
        setSubmission(submissionResponse);
        setLoadState(response ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [questId]);

  return (
    <section className="page">
      <PageHeader eyebrow="QUEST" title="Quest 상세" description="Quest 기간, 상태, 수행 항목을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="Quest를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="Quest를 찾을 수 없습니다." /> : null}
      {quest ? <QuestContent quest={quest} submission={submission} /> : null}
    </section>
  );
}

function QuestContent({ quest, submission }: { quest: QuestItem; submission?: QuestSubmissionResult }) {
  const status = statusMap[quest.status];

  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone={status.tone}>{status.label}</StatusPill>
        <StatusPill tone="gray">{quest.startsAt} ~ {quest.endsAt}</StatusPill>
      </div>
      <h2>{quest.title}</h2>
      <div className="detail-body">{quest.description || 'Quest 설명이 없습니다.'}</div>
      <ul className="task-list">
        {(quest.tasks || []).map((task) => (
          <li key={task}>{task}</li>
        ))}
      </ul>
      <section className="board-actions" aria-label="제출 결과">
        <h3>내 제출 결과</h3>
        {submission ? (
          <dl className="info-list detail-info">
            <div>
              <dt>제출 상태</dt>
              <dd>{submission.status}</dd>
            </div>
            <div>
              <dt>평가 상태</dt>
              <dd>{submission.resultStatus || 'pending'}</dd>
            </div>
            <div>
              <dt>점수</dt>
              <dd>{typeof submission.score === 'number' ? submission.score.toLocaleString('ko-KR') : '-'}</dd>
            </div>
            <div>
              <dt>제출일</dt>
              <dd>{formatDate(submission.submittedAt)}</dd>
            </div>
            <div>
              <dt>채점일</dt>
              <dd>{formatDate(submission.gradedAt)}</dd>
            </div>
          </dl>
        ) : (
          <p className="muted-text">아직 제출 내역이 없습니다.</p>
        )}
      </section>
      <div className="action-row">
        <a className="ghost-button" href="/quest">목록</a>
        <a className="primary-action" href={`/quest/${quest.id}/submit`}>제출하기</a>
      </div>
    </article>
  );
}

function formatDate(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default QuestDetailPage;
