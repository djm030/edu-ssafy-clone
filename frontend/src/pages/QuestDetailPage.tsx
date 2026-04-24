import { useEffect, useState } from 'react';
import { getQuest } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, QuestItem } from '../types';

const statusMap = {
  done: { label: '완료', tone: 'green' },
  graded: { label: '채점완료', tone: 'blue' },
  progress: { label: '진행중', tone: 'yellow' },
} as const;

function QuestDetailPage({ questId }: { questId: number }) {
  const [quest, setQuest] = useState<QuestItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getQuest(questId)
      .then((response) => {
        if (ignore) return;
        setQuest(response);
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
      {quest ? <QuestContent quest={quest} /> : null}
    </section>
  );
}

function QuestContent({ quest }: { quest: QuestItem }) {
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
      <div className="action-row">
        <a className="ghost-button" href="/quest">목록</a>
        <a className="primary-action" href={`/quest/${quest.id}/submit`}>제출하기</a>
      </div>
    </article>
  );
}

export default QuestDetailPage;
