import { useEffect, useState } from 'react';
import { getQuests } from '../api/app';
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

function QuestPage() {
  const [items, setItems] = useState<QuestItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getQuests()
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
  }, []);

  return (
    <section className="page">
      <PageHeader eyebrow="QUEST" title="Quest/평가" description="진행 중인 Quest와 평가 상태를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="Quest 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="진행 중인 Quest가 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <div className="card-list">
          {items.map((item) => {
            const status = statusMap[item.status];
            return (
              <article className="list-card" key={item.id}>
                <div>
                  <h2>{item.title}</h2>
                  <p>
                    {item.startsAt} ~ {item.endsAt}
                  </p>
                </div>
                <StatusPill tone={status.tone}>{status.label}</StatusPill>
                <a className="ghost-button" href={`/quest/${item.id}`}>
                  상세
                </a>
              </article>
            );
          })}
        </div>
      ) : null}
    </section>
  );
}

export default QuestPage;
