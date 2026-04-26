import { useEffect, useState } from 'react';
import { getLearningMaterial, toggleLearningMaterialReaction } from '../api/app';
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
  const [currentMaterial, setCurrentMaterial] = useState(material);
  const [updatingReaction, setUpdatingReaction] = useState<'bookmark' | 'like'>();
  const [message, setMessage] = useState('좋아요와 북마크는 서버에 저장됩니다.');
  const canOpenViewer = currentMaterial.type === 'ebook' || currentMaterial.type === 'file';

  useEffect(() => {
    setCurrentMaterial(material);
    setMessage('좋아요와 북마크는 서버에 저장됩니다.');
    setUpdatingReaction(undefined);
  }, [material]);

  const toggleReaction = async (type: 'bookmark' | 'like') => {
    const active = type === 'like' ? Boolean(currentMaterial.liked) : Boolean(currentMaterial.bookmarked);
    setUpdatingReaction(type);
    setMessage(active ? '반응을 해제하는 중입니다.' : '반응을 저장하는 중입니다.');
    try {
      const response = await toggleLearningMaterialReaction(currentMaterial.id, type, active);
      setCurrentMaterial(response.item);
      setMessage(active ? '반응이 해제되었습니다.' : '반응이 저장되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setUpdatingReaction(undefined);
    }
  };

  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone="blue">{typeLabels[currentMaterial.type]}</StatusPill>
        <StatusPill tone="gray">조회 {currentMaterial.viewCount.toLocaleString('ko-KR')}</StatusPill>
      </div>
      <h2>{currentMaterial.title}</h2>
      <dl className="info-list detail-info">
        <div>
          <dt>등록자</dt>
          <dd>{currentMaterial.authorName}</dd>
        </div>
        <div>
          <dt>등록일</dt>
          <dd>{currentMaterial.createdAt}</dd>
        </div>
        <div>
          <dt>파일명</dt>
          <dd>{currentMaterial.fileName || '-'}</dd>
        </div>
      </dl>
      <div className="detail-body">{currentMaterial.description || '자료 설명이 없습니다.'}</div>
      <section className="resource-list" aria-label="학습자료 리소스">
        <h3>자료 리소스</h3>
        {currentMaterial.resources && currentMaterial.resources.length > 0 ? (
          <ul>
            {currentMaterial.resources.map((resource) => (
              <li key={resource.id || resource.title}>
                <div>
                  <strong>{resource.title}</strong>
                  <span>{resource.type}{resource.launchMode ? ` · ${resource.launchMode}` : ''}</span>
                </div>
                {resource.targetUrl ? (
                  <a className="ghost-button" href={resource.targetUrl} rel="noreferrer" target="_blank">
                    열기
                  </a>
                ) : null}
              </li>
            ))}
          </ul>
        ) : (
          <p>등록된 리소스가 없습니다.</p>
        )}
      </section>
      <div className="action-row" aria-label="학습자료 반응">
        <button
          className={currentMaterial.liked ? 'primary-action' : 'ghost-button'}
          disabled={updatingReaction === 'like'}
          onClick={() => { void toggleReaction('like'); }}
          type="button"
        >
          좋아요 {(currentMaterial.likeCount || 0).toLocaleString('ko-KR')}
        </button>
        <button
          className={currentMaterial.bookmarked ? 'primary-action' : 'ghost-button'}
          disabled={updatingReaction === 'bookmark'}
          onClick={() => { void toggleReaction('bookmark'); }}
          type="button"
        >
          북마크 {(currentMaterial.bookmarkCount || 0).toLocaleString('ko-KR')}
        </button>
      </div>
      <p className="form-message" aria-live="polite">{message}</p>
      <div className="action-row">
        <a className="ghost-button" href="/learning/materials">목록</a>
        {canOpenViewer ? <a className="primary-action" href={`/learning/materials/${currentMaterial.id}/viewer`}>열기</a> : null}
      </div>
    </article>
  );
}

export default MaterialDetailPage;
