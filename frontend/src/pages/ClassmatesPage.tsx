import { useEffect, useState } from 'react';
import { getClassmates, sendClassmateNotification } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { Classmate, LoadState } from '../types';

function ClassmatesPage() {
  const [items, setItems] = useState<Classmate[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [notifyingId, setNotifyingId] = useState<number | null>(null);
  const [notificationMessage, setNotificationMessage] = useState('');

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

  const notifyClassmate = async (classmate: Classmate) => {
    setNotifyingId(classmate.id);
    setNotificationMessage('');

    try {
      const response = await sendClassmateNotification(classmate.id);
      setNotificationMessage(`${classmate.name}님에게 알림을 보냈습니다.${response.id ? ` 알림번호: ${response.id}` : ''}`);
    } catch (error) {
      setNotificationMessage(getErrorMessage(error));
    } finally {
      setNotifyingId(null);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="COMMUNITY" title="우리반 보기" description="같은 캠퍼스와 트랙에서 함께 학습하는 교육생을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="우리반 정보를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="표시할 교육생이 없습니다." /> : null}
      {notificationMessage ? (
        <div className="check-result" aria-live="polite">
          <StatusPill tone={notificationMessage.includes('보냈습니다') ? 'green' : 'red'}>
            {notificationMessage.includes('보냈습니다') ? '발송완료' : '오류'}
          </StatusPill>
          <p>{notificationMessage}</p>
        </div>
      ) : null}
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
                <button className="ghost-button" disabled={notifyingId === item.id} onClick={() => notifyClassmate(item)} type="button">
                  {notifyingId === item.id ? '알림 발송 중' : '알림 보내기'}
                </button>
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  );
}

export default ClassmatesPage;
