import { useEffect, useState } from 'react';
import { getSurveys } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, SurveyItem } from '../types';

function SurveyPage() {
  const [items, setItems] = useState<SurveyItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getSurveys()
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
      <PageHeader eyebrow="SURVEY" title="설문" description="필수 여부, 기간, 응답 상태를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="설문 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="응답할 설문이 없습니다." /> : null}
      {loadState === 'loaded' ? <SurveyTable items={items} /> : null}
    </section>
  );
}

function SurveyTable({ items }: { items: SurveyItem[] }) {
  return (
    <div className="simple-table" role="table" aria-label="설문 목록">
      <div className="simple-row table-head" role="row">
        <span role="columnheader">제목</span>
        <span role="columnheader">필수</span>
        <span role="columnheader">기간</span>
        <span role="columnheader">응답</span>
        <span role="columnheader">상세</span>
      </div>
      {items.map((item) => (
        <div className="simple-row" key={item.id} role="row">
          <strong role="cell">{item.title}</strong>
          <span role="cell">{item.required ? '필수' : '선택'}</span>
          <span role="cell">
            {item.startsAt} ~ {item.endsAt}
          </span>
          <span role="cell">
            <StatusPill tone={item.answered ? 'green' : 'yellow'}>{item.answered ? '응답완료' : '미응답'}</StatusPill>
          </span>
          <span role="cell">
            <a className="ghost-button" href={`/survey/${item.id}`}>열기</a>
          </span>
        </div>
      ))}
    </div>
  );
}

export default SurveyPage;
