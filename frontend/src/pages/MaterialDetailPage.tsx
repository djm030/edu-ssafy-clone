import { useEffect, useState } from 'react';
import { getLearningMaterial, toggleMaterialReaction } from '../api/app';
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
  const [liked, setLiked] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const [favorited, setFavorited] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getLearningMaterial(materialId)
      .then((response) => {
        if (ignore) return;
        setMaterial(response);
        setLiked(Boolean(response?.liked));
        setBookmarked(Boolean(response?.bookmarked));
        setFavorited(Boolean(response?.favorited));
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

  function applyReactionState(type: 'like' | 'bookmark' | 'favorite', active: boolean) {
    if (type === 'like') setLiked(active);
    if (type === 'bookmark') setBookmarked(active);
    if (type === 'favorite') setFavorited(active);
  }

  async function handleReaction(type: 'like' | 'bookmark' | 'favorite') {
    try {
      const response = await toggleMaterialReaction(materialId, type);
      applyReactionState(type, response.active);
      const label = type === 'like' ? '좋아요' : type === 'bookmark' ? '북마크' : '즐겨찾기';
      setMessage(`${label} 상태가 변경되었습니다.`);
    } catch (error) {
      setMessage(getErrorMessage(error));
    }
  }

  return (
    <section className="page">
      <PageHeader eyebrow="LEARNING" title="학습자료 상세" description="자료 설명과 열람 정보를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="학습자료를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="학습자료를 찾을 수 없습니다." /> : null}
      {message ? <p className="form-message success">{message}</p> : null}
      {material ? (
        <MaterialContent
          bookmarked={bookmarked}
          favorited={favorited}
          liked={liked}
          material={material}
          onReaction={handleReaction}
        />
      ) : null}
    </section>
  );
}

function MaterialContent({
  material,
  liked,
  bookmarked,
  favorited,
  onReaction,
}: {
  material: LearningMaterial;
  liked: boolean;
  bookmarked: boolean;
  favorited: boolean;
  onReaction: (type: 'like' | 'bookmark' | 'favorite') => void;
}) {
  const canOpenViewer = material.type === 'ebook' || material.type === 'file';
  const likeBase = material.likeCount || 0;
  const bookmarkBase = material.bookmarkCount || 0;
  const favoriteBase = material.favoriteCount || 0;
  const likeCount = likeBase + (liked ? 1 : 0) - (material.liked ? 1 : 0);
  const bookmarkCount = bookmarkBase + (bookmarked ? 1 : 0) - (material.bookmarked ? 1 : 0);
  const favoriteCount = favoriteBase + (favorited ? 1 : 0) - (material.favorited ? 1 : 0);

  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone="blue">{typeLabels[material.type]}</StatusPill>
        <StatusPill tone="gray">조회 {material.viewCount.toLocaleString('ko-KR')}</StatusPill>
      </div>
      <div className="action-row">
        <button className={liked ? 'primary-action' : 'ghost-button'} onClick={() => onReaction('like')} type="button">
          좋아요 {likeCount}
        </button>
        <button className={bookmarked ? 'primary-action' : 'ghost-button'} onClick={() => onReaction('bookmark')} type="button">
          북마크 {bookmarkCount}
        </button>
        <button className={favorited ? 'primary-action' : 'ghost-button'} onClick={() => onReaction('favorite')} type="button">
          즐겨찾기 {favoriteCount}
        </button>
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
