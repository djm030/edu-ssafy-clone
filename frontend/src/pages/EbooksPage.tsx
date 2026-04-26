import { useEffect, useState } from 'react';
import { getEbook, getEbooks, recordEbookAccess } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import type { EbookItem, LoadState } from '../types';

interface EbooksPageProps {
  ebookId?: number;
}

function EbooksPage({ ebookId }: EbooksPageProps) {
  const [items, setItems] = useState<EbookItem[]>([]);
  const [selected, setSelected] = useState<EbookItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    setActionMessage('');

    const request = ebookId ? getEbook(ebookId).then((item) => ({ items: [item], selected: item })) : getEbooks({ page: 1, size: 50 }).then((response) => ({ items: response.items, selected: response.items[0] }));

    request
      .then((response) => {
        if (ignore) return;
        setItems(response.items);
        setSelected(response.selected);
        setLoadState(response.items.length === 0 ? 'empty' : 'loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });

    return () => {
      ignore = true;
    };
  }, [ebookId, retryToken]);

  const openEbook = (item: EbookItem) => {
    setActionMessage('');
    recordEbookAccess(item.id)
      .then((response) => {
        setSelected(response.item);
        setItems((current) => current.map((ebook) => (ebook.id === response.item.id ? response.item : ebook)));
        setActionMessage(`${response.item.title} 열람 기록을 저장했습니다.`);
        if (response.item.externalUrl && response.item.externalUrl !== '#') {
          window.open(response.item.externalUrl, '_blank', 'noopener,noreferrer');
        }
      })
      .catch((error) => setActionMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="SSAFY e-book" description="학습자료와 별도로 제공되는 e-book을 확인하고 열람 기록을 남깁니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="e-book을 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} /> : null}
      {loadState === 'empty' ? <DataState title="등록된 e-book이 없습니다." message="활성화된 e-book이 등록되면 이곳에 표시됩니다." /> : null}
      {loadState === 'loaded' ? (
        <div className="content-grid">
          <section className="panel">
            <h2>e-book 목록</h2>
            <div className="list-stack">
              {items.map((item) => (
                <article className="list-card" key={item.id}>
                  <div>
                    <p className="eyebrow">{item.category || 'E-BOOK'}</p>
                    <h3>{item.title}</h3>
                    <p>{item.description || '설명이 등록되지 않았습니다.'}</p>
                    <p className="muted">열람 {item.accessCount}회 · 최근 열람 {item.lastAccessedAt ? item.lastAccessedAt.slice(0, 16).replace('T', ' ') : '-'}</p>
                  </div>
                  <a className="text-link" href={`/mycampus/ebooks/${item.id}`}>상세</a>
                </article>
              ))}
            </div>
          </section>
          <section className="panel">
            <h2>상세 보기</h2>
            {selected ? (
              <div className="detail-stack">
                <p className="eyebrow">{selected.category || 'SSAFY e-book'}</p>
                <h3>{selected.title}</h3>
                <p>{selected.description || '설명이 등록되지 않았습니다.'}</p>
                <dl className="info-list">
                  <InfoRow label="등록일" value={selected.createdAt ? selected.createdAt.slice(0, 10) : '-'} />
                  <InfoRow label="열람 횟수" value={`${selected.accessCount}회`} />
                  <InfoRow label="최근 열람" value={selected.lastAccessedAt ? selected.lastAccessedAt.slice(0, 16).replace('T', ' ') : '-'} />
                </dl>
                <button className="primary-action" onClick={() => openEbook(selected)} type="button">e-book 열람</button>
                {actionMessage ? <p className="form-message">{actionMessage}</p> : null}
              </div>
            ) : (
              <DataState title="선택된 e-book이 없습니다." message="목록에서 e-book을 선택해 주세요." />
            )}
          </section>
        </div>
      ) : null}
    </section>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

export default EbooksPage;
