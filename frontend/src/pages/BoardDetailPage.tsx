import { FormEvent, useEffect, useState } from 'react';
import { attachBoardFile, createBoardComment, createBoardReaction, deleteBoardPost, updateBoardPost } from '../api/app';
import { getPost } from '../api/boards';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { BoardCode, BoardPostListItem, LoadState } from '../types';

interface BoardDetailPageProps {
  boardCode: BoardCode;
  postId: number;
  title: string;
  listPath: string;
}

function BoardDetailPage({ boardCode, postId, title, listPath }: BoardDetailPageProps) {
  const [post, setPost] = useState<BoardPostListItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getPost(boardCode, postId)
      .then((response) => {
        if (ignore) return;
        setPost(response);
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
  }, [boardCode, postId]);

  return (
    <section className="page">
      <PageHeader eyebrow="DETAIL" title={title} description="게시글 상세 내용을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="게시글을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="게시글을 찾을 수 없습니다." message="목록에서 다시 선택해 주세요." /> : null}
      {post ? <DetailContent post={post} listPath={listPath} /> : null}
    </section>
  );
}

function DetailContent({ post, listPath }: { post: BoardPostListItem; listPath: string }) {
  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone="green">{post.category?.name || '일반'}</StatusPill>
        {post.isPinned ? <StatusPill tone="yellow">고정</StatusPill> : null}
        {post.hasAttachment ? <StatusPill tone="blue">첨부 있음</StatusPill> : null}
      </div>
      <h2>{post.title}</h2>
      <dl className="info-list detail-info">
        <div>
          <dt>작성자</dt>
          <dd>{post.authorName || '-'}</dd>
        </div>
        <div>
          <dt>등록일</dt>
          <dd>{formatDate(post.createdAt)}</dd>
        </div>
        <div>
          <dt>조회</dt>
          <dd>{(post.viewCount || 0).toLocaleString('ko-KR')}</dd>
        </div>
      </dl>
      <div className="detail-body">{post.content || '등록된 본문이 없습니다.'}</div>
      <BoardActions post={post} />
      <div className="action-row">
        <a className="ghost-button" href={listPath}>목록</a>
      </div>
    </article>
  );
}

function BoardActions({ post }: { post: BoardPostListItem }) {
  const [comment, setComment] = useState('');
  const [liked, setLiked] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const [deleted, setDeleted] = useState(false);
  const [message, setMessage] = useState('댓글, 반응, 수정, 삭제, 첨부를 API로 처리합니다.');

  const submitComment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    try {
      const item = await createBoardComment(post.boardCode, post.id, comment);
      setMessage(`댓글이 등록되었습니다: ${item.content}`);
      setComment('');
    } catch (error) {
      setMessage(getErrorMessage(error));
    }
  };

  const toggleReaction = async (type: 'like' | 'bookmark') => {
    try {
      const item = await createBoardReaction(post.boardCode, post.id, type);
      if (type === 'like') setLiked(item.active);
      if (type === 'bookmark') setBookmarked(item.active);
      setMessage(`${type === 'like' ? '추천' : '찜'} 상태가 변경되었습니다.`);
    } catch (error) {
      setMessage(getErrorMessage(error));
    }
  };

  const editPost = async () => {
    try {
      const item = await updateBoardPost(post.boardCode, post.id, { categoryId: post.category?.id, title: post.title.endsWith('수정') ? post.title : `${post.title} 수정`, content: post.content || '본문' });
      setMessage(`게시글이 수정되었습니다: ${item.title}`);
    } catch (error) {
      setMessage(getErrorMessage(error));
    }
  };

  const attachFile = async () => {
    try {
      const item = await attachBoardFile(post.boardCode, post.id, 'board-attachment.txt');
      setMessage(`첨부파일이 등록되었습니다: ${item.fileName}`);
    } catch (error) {
      setMessage(getErrorMessage(error));
    }
  };

  const deletePost = async () => {
    try {
      await deleteBoardPost(post.boardCode, post.id);
      setDeleted(true);
      setMessage('게시글이 삭제되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    }
  };

  return (
    <section className="board-actions" aria-label="게시글 반응">
      {deleted ? <StatusPill tone="red">삭제됨</StatusPill> : null}
      <div className="action-row">
        <button className={liked ? 'primary-action' : 'ghost-button'} onClick={() => toggleReaction('like')} type="button">
          추천 {(post.reactionCount || 0) + (liked ? 1 : 0)}
        </button>
        <button className={bookmarked ? 'primary-action' : 'ghost-button'} onClick={() => toggleReaction('bookmark')} type="button">
          찜 {(post.bookmarkCount || 0) + (bookmarked ? 1 : 0)}
        </button>
        <button className="ghost-button" onClick={editPost} type="button">수정</button>
        <button className="ghost-button" onClick={attachFile} type="button">첨부 등록</button>
        <button className="ghost-button" onClick={deletePost} type="button">삭제</button>
      </div>
      <form className="comment-form" onSubmit={submitComment}>
        <label className="visually-hidden" htmlFor={`comment-${post.id}`}>댓글</label>
        <input id={`comment-${post.id}`} onChange={(event) => setComment(event.target.value)} placeholder="댓글을 입력하세요" required value={comment} />
        <button className="ghost-button" type="submit">댓글 등록</button>
      </form>
      <p className="form-message" aria-live="polite">{message}</p>
    </section>
  );
}

function formatDate(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default BoardDetailPage;
