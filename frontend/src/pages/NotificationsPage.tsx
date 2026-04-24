import { useEffect, useMemo, useState } from 'react';
import { getNotifications } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, NotificationItem } from '../types';

const categoryLabel = {
  learning: '학습',
  notice: '공지',
  quest: 'Quest',
  survey: '설문',
};

function NotificationsPage() {
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getNotifications()
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

  const unreadCount = useMemo(() => items.filter((item) => !item.read).length, [items]);

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="알림함" description="공지, 학습, Quest, 설문 알림을 모아 확인합니다." />
      <div className="summary-grid">
        <section className="stat-card">
          <span>읽지 않은 알림</span>
          <strong>{unreadCount}건</strong>
        </section>
        <section className="stat-card">
          <span>전체 알림</span>
          <strong>{items.length}건</strong>
        </section>
      </div>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="알림을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="새 알림이 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <div className="card-list">
          {items.map((item) => (
            <article className={item.read ? 'list-card' : 'list-card unread'} key={item.id}>
              <div>
                <h2>{item.title}</h2>
                <p>{item.message}</p>
                <p>{item.createdAt}</p>
              </div>
              <StatusPill tone={item.read ? 'gray' : 'blue'}>{item.read ? '읽음' : '새 알림'}</StatusPill>
              <StatusPill tone="green">{categoryLabel[item.category]}</StatusPill>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  );
}

export default NotificationsPage;
