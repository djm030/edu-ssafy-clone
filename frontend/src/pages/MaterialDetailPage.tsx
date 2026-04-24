import { useEffect, useState } from 'react';
import { getLearningMaterial } from '../api/app';
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

function MaterialDetailPage({ materialId }: { materialId: number }) {
  const [material, setMaterial] = useState<LearningMaterial>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getLearningMaterial(materialId)
      .then((response) => {
        if (ignore) return;
        setMaterial(response);
        setLoadState(response ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });

    return () => {
      ignore = true;
    };
  }, [materialId]);

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="학습자료 상세" description="자료 설명과 열람 정보를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="학습자료를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="학습자료를 찾을 수 없습니다." /> : null}
      {material ? <MaterialContent material={material} /> : null}
    </section>
  );
}

function MaterialContent({ material }: { material: LearningMaterial }) {
  const canOpenViewer = material.type === 'ebook' || material.type === 'file';

  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone="blue">{typeLabels[material.type]}</StatusPill>
        <StatusPill tone="gray">조회 {material.viewCount.toLocaleString('ko-KR')}</StatusPill>
      </div>
      <h2>{material.title}</h2>
      <dl className="info-list detail-info">
        <div>
          <dt>등록자</dt>
          <dd>{material.authorName}</dd>
        </div>
        <div>
          <dt>등록일</dt>
          <dd>{material.createdAt}</dd>
        </div>
        <div>
          <dt>파일명</dt>
          <dd>{material.fileName || '-'}</dd>
        </div>
      </dl>
      <div className="detail-body">{material.description || '자료 설명이 없습니다.'}</div>
      <div className="action-row">
        <a className="ghost-button" href="/learning/materials">목록</a>
        {canOpenViewer ? <a className="primary-action" href={`/learning/materials/${material.id}/viewer`}>열기</a> : null}
      </div>
    </article>
  );
}

export default MaterialDetailPage;
