import { useEffect, useState } from 'react';
import { getLearningMaterial } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import type { LearningMaterial, LoadState } from '../types';

function MaterialViewerPage({ materialId }: { materialId: number }) {
  const [material, setMaterial] = useState<LearningMaterial>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
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
    <section className="page viewer-page">
      <PageHeader eyebrow="VIEWER" title="자료 뷰어" description="PDF/eBook 열람 영역 placeholder입니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="뷰어를 열 수 없습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="자료를 찾을 수 없습니다." /> : null}
      {material ? (
        <section className="viewer-frame" aria-label={`${material.title} 뷰어`}>
          <div>
            <strong>{material.title}</strong>
            <p>{material.fileName || '외부 자료'} 미리보기 영역</p>
          </div>
          <a className="ghost-button" href={`/learning/materials/${material.id}`}>상세로 돌아가기</a>
        </section>
      ) : null}
    </section>
  );
}

export default MaterialViewerPage;
