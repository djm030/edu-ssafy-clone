import { useEffect, useState } from 'react';
import { getClassmates } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { Classmate, LoadState } from '../types';

function ClassmatesPage() {
  const [items, setItems] = useState<Classmate[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getClassmates()
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
      <PageHeader eyebrow="COMMUNITY" title="우리반 보기" description="같은 캠퍼스와 트랙에서 함께 학습하는 교육생을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="우리반 정보를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="표시할 교육생이 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <div className="classmate-grid">
          {items.map((item) => (
            <article className="panel classmate-card" key={item.id}>
              <div className="avatar" aria-hidden="true">
                {item.name.slice(0, 1)}
              </div>
              <div>
                <h2>{item.name}</h2>
                <p>
                  {item.campusName} 캠퍼스 · {item.trackName}
                </p>
                <StatusPill tone="blue">{item.teamName || '팀 미배정'}</StatusPill>
                <p>{item.statusMessage || '상태 메시지가 없습니다.'}</p>
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  );
}

export default ClassmatesPage;
