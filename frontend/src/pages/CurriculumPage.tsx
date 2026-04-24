import { useEffect, useState } from 'react';
import { getCurriculum } from '../api/app';
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

function CurriculumPage() {
  const [items, setItems] = useState<CurriculumWeek[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getCurriculum()
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
      <PageHeader eyebrow="LEARNING" title="주차별 커리큘럼" description="주차별 학습 주제와 수업 구성을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="커리큘럼을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="등록된 커리큘럼이 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <div className="card-list">
          {items.map((item) => {
            const status = statusMap[item.status];
            return (
              <article className="list-card curriculum-card" key={item.id}>
                <div>
                  <p className="eyebrow">{item.week}주차</p>
                  <h2>{item.title}</h2>
                  <p>{item.period}</p>
                  <ul>
                    {item.lessons.map((lesson) => (
                      <li key={lesson}>{lesson}</li>
                    ))}
                  </ul>
                </div>
                <StatusPill tone={status.tone}>{status.label}</StatusPill>
              </article>
            );
          })}
        </div>
      ) : null}
    </section>
  );
}

export default CurriculumPage;
