import { FormEvent, useEffect, useState } from 'react';
import { getLearningMaterials } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LearningMaterial, LoadState } from '../types';

const typeLabels = {
  ebook: 'eBook',
  file: 'PDF/파일',
  link: '링크',
  video: '영상',
};

function MaterialsPage() {
  const [items, setItems] = useState<LearningMaterial[]>([]);
  const [type, setType] = useState('');
  const [keyword, setKeyword] = useState('');
  const [submittedKeyword, setSubmittedKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getLearningMaterials({ keyword: submittedKeyword, type })
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
  }, [submittedKeyword, type]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmittedKeyword(keyword.trim());
  };

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="학습자료" description="자료 유형과 검색어로 필요한 학습 자료를 찾습니다." />
      <div className="filter-bar">
        <div className="category-strip">
          <button className={!type ? 'category-chip active' : 'category-chip'} onClick={() => setType('')} type="button">
            전체
          </button>
          {Object.entries(typeLabels).map(([value, label]) => (
            <button className={type === value ? 'category-chip active' : 'category-chip'} key={value} onClick={() => setType(value)} type="button">
              {label}
            </button>
          ))}
        </div>
        <form className="search-form" onSubmit={submit}>
          <input onChange={(event) => setKeyword(event.target.value)} placeholder="자료 제목 검색" value={keyword} />
          <button type="submit">검색</button>
        </form>
      </div>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="학습자료를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="학습자료가 없습니다." message="검색어 또는 자료 유형을 바꿔 보세요." /> : null}
      {loadState === 'loaded' ? <MaterialList items={items} /> : null}
    </section>
  );
}

function MaterialList({ items }: { items: LearningMaterial[] }) {
  return (
    <div className="simple-table" role="table" aria-label="학습자료 목록">
      <div className="simple-row table-head" role="row">
        <span role="columnheader">제목</span>
        <span role="columnheader">유형</span>
        <span role="columnheader">등록자</span>
        <span role="columnheader">조회</span>
        <span role="columnheader">반응</span>
        <span role="columnheader">열기</span>
      </div>
      {items.map((item) => (
        <div className="simple-row" key={item.id} role="row">
          <strong role="cell">{item.title}</strong>
          <span role="cell">
            <StatusPill tone="blue">{typeLabels[item.type]}</StatusPill>
          </span>
          <span role="cell">{item.authorName}</span>
          <span role="cell">{item.viewCount.toLocaleString('ko-KR')}</span>
          <span role="cell">좋아요 {item.likeCount || 0} · 북마크 {item.bookmarkCount || 0} · 즐겨찾기 {item.favoriteCount || 0}</span>
          <span role="cell">
            <a className="ghost-button" href={`/learning/materials/${item.id}`}>
              열기
            </a>
          </span>
        </div>
      ))}
    </div>
  );
}

export default MaterialsPage;
