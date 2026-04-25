import { useEffect, useMemo, useState } from 'react';
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '../api/app';
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
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [updatingAll, setUpdatingAll] = useState(false);
  const [actionMessage, setActionMessage] = useState('');

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

  const handleMarkRead = (notification: NotificationItem) => {
    if (notification.read || updatingId !== null || updatingAll) return;
    setUpdatingId(notification.id);
    setActionMessage('');
    markNotificationRead(notification.id)
      .then((response) => {
        setItems((current) => current.map((item) => (item.id === response.item.id ? response.item : item)));
        setActionMessage(`읽음 처리되었습니다. 남은 미확인 알림 ${response.unreadCount}건`);
      })
      .catch((error) => {
        setActionMessage(getErrorMessage(error));
      })
      .finally(() => {
        setUpdatingId(null);
      });
  };

  const handleMarkAllRead = () => {
    if (unreadCount === 0 || updatingId !== null || updatingAll) return;
    setUpdatingAll(true);
    setActionMessage('');
    markAllNotificationsRead()
      .then((response) => {
        setItems((current) => {
          if (response.items.length === 0) {
            return current.map((item) => ({ ...item, read: true }));
          }
          const updates = new Map(response.items.map((item) => [item.id, item]));
          return current.map((item) => updates.get(item.id) || { ...item, read: true });
        });
        setActionMessage(`모든 알림을 읽음 처리했습니다. 남은 미확인 알림 ${response.unreadCount}건`);
      })
      .catch((error) => {
        setActionMessage(getErrorMessage(error));
      })
      .finally(() => {
        setUpdatingAll(false);
      });
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="알림함" description="공지, 학습, Quest, 설문 알림을 모아 확인합니다." />
      <div className="summary-grid">
        <section className="stat-card">
          <span>읽지 않은 알림</span>
          <strong>{unreadCount}건</strong>
          <button className="ghost-button" disabled={unreadCount === 0 || updatingAll || updatingId !== null} onClick={handleMarkAllRead} type="button">
            {updatingAll ? '전체 처리 중...' : '모두 읽음 처리'}
          </button>
        </section>
        <section className="stat-card">
          <span>전체 알림</span>
          <strong>{items.length}건</strong>
        </section>
      </div>
      {actionMessage ? <DataState title="알림 처리 결과" message={actionMessage} /> : null}
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
              <div className="action-row">
                <StatusPill tone={item.read ? 'gray' : 'blue'}>{item.read ? '읽음' : '새 알림'}</StatusPill>
                <StatusPill tone="green">{categoryLabel[item.category]}</StatusPill>
                {!item.read ? (
                  <button className="ghost-button" disabled={updatingId === item.id} onClick={() => handleMarkRead(item)} type="button">
                    {updatingId === item.id ? '처리 중...' : '읽음 처리'}
                  </button>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  );
}

export default NotificationsPage;
