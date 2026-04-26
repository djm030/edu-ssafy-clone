import { useEffect, useState } from 'react';
import { agreePledge, getPledge, getPledges } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, PledgeItem } from '../types';

interface PledgesPageProps {
  pledgeId?: number;
}

function PledgesPage({ pledgeId }: PledgesPageProps) {
  const [items, setItems] = useState<PledgeItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [checked, setChecked] = useState<Record<number, boolean>>({});
  const [mutationMessage, setMutationMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    const request = pledgeId ? getPledge(pledgeId).then((item) => ({ items: [item] })) : getPledges({ size: 50 });
    request
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
  }, [pledgeId]);

  const submitAgreement = (id: number) => {
    if (!checked[id]) {
      setMutationMessage('서약 내용을 확인하고 동의 체크를 해 주세요.');
      return;
    }
    setMutationMessage('서약 동의를 저장하는 중입니다.');
    agreePledge(id)
      .then((response) => {
        setItems((current) => current.map((item) => (item.id === id ? response.item : item)));
        setMutationMessage('서약 동의가 저장되었습니다.');
      })
      .catch((error) => setMutationMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="교육생 서약서" description="교육 과정 참여에 필요한 서약 내용을 확인하고 동의 상태를 저장합니다." />
      {mutationMessage ? <p className="helper-text" role="status">{mutationMessage}</p> : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="서약서를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="활성화된 서약서가 없습니다." message="현재 동의할 교육생 서약서가 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <div className="card-list" aria-label="교육생 서약서 목록">
          {items.map((item) => (
            <article className="list-card" key={item.id}>
              <div>
                <p className="eyebrow">v{item.version} · {item.required ? '필수' : '선택'}</p>
                <h2>{item.title}</h2>
                <p>{item.content}</p>
                <dl className="detail-grid">
                  <div>
                    <dt>마감</dt>
                    <dd>{formatDate(item.dueAt)}</dd>
                  </div>
                  <div>
                    <dt>동의 시각</dt>
                    <dd>{formatDate(item.agreedAt)}</dd>
                  </div>
                </dl>
              </div>
              <StatusPill tone={item.agreed ? 'green' : 'yellow'}>{item.agreed ? '동의 완료' : '동의 필요'}</StatusPill>
              <label className="helper-text">
                <input checked={Boolean(checked[item.id])} onChange={(event) => setChecked((current) => ({ ...current, [item.id]: event.target.checked }))} type="checkbox" />
                {' '}서약 내용을 확인했고 동의합니다.
              </label>
              <div className="action-row">
                <a className="ghost-button" href={`/mycampus/pledges/${item.id}`}>내용 보기</a>
                <button className="primary-action" disabled={item.agreed} onClick={() => submitAgreement(item.id)} type="button">
                  {item.agreed ? '제출 완료' : '동의 제출'}
                </button>
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  );
}

function formatDate(value?: string | null): string {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

export default PledgesPage;
