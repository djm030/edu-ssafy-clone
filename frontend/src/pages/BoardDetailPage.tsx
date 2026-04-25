import { FormEvent, useEffect, useState } from 'react';
import { createComment, getPost } from '../api/boards';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { BoardCode, BoardCommentItem, BoardPostListItem, LoadState } from '../types';

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
      {post ? <DetailContent boardCode={boardCode} post={post} listPath={listPath} /> : null}
    </section>
  );
}

function DetailContent({ boardCode, post, listPath }: { boardCode: BoardCode; post: BoardPostListItem; listPath: string }) {
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
      <BoardActions boardCode={boardCode} post={post} />
      <div className="action-row">
        <a className="ghost-button" href={listPath}>목록</a>
      </div>
    </article>
  );
}

function BoardActions({ boardCode, post }: { boardCode: BoardCode; post: BoardPostListItem }) {
  const [comment, setComment] = useState('');
  const [comments, setComments] = useState<BoardCommentItem[]>(post.comments ?? []);
  const [submittingComment, setSubmittingComment] = useState(false);
  const [liked, setLiked] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const [message, setMessage] = useState('댓글을 등록하면 서버에 저장됩니다.');

  useEffect(() => {
    setComments(post.comments ?? []);
    setComment('');
    setMessage('댓글을 등록하면 서버에 저장됩니다.');
  }, [post.id, post.comments]);

  const submitComment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const content = comment.trim();
    if (!content) return;

    setSubmittingComment(true);
    setMessage('댓글을 등록하는 중입니다.');
    try {
      const created = await createComment(boardCode, post.id, content);
      setComments((items) => [...items, created]);
      setComment('');
      setMessage('댓글이 등록되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmittingComment(false);
    }
  };

  return (
    <section className="board-actions" aria-label="게시글 반응">
      <div className="action-row">
        <button className={liked ? 'primary-action' : 'ghost-button'} onClick={() => { setLiked((value) => !value); setMessage('추천 상태가 변경되었습니다.'); }} type="button">
          추천 {(post.reactionCount || 0) + (liked ? 1 : 0)}
        </button>
        <button className={bookmarked ? 'primary-action' : 'ghost-button'} onClick={() => { setBookmarked((value) => !value); setMessage('찜 상태가 변경되었습니다.'); }} type="button">
          찜 {(post.bookmarkCount || 0) + (bookmarked ? 1 : 0)}
        </button>
      </div>
      <form className="comment-form" onSubmit={submitComment}>
        <label className="visually-hidden" htmlFor={`comment-${post.id}`}>댓글</label>
        <input disabled={submittingComment} id={`comment-${post.id}`} onChange={(event) => setComment(event.target.value)} placeholder="댓글을 입력하세요" required value={comment} />
        <button className="ghost-button" disabled={submittingComment} type="submit">{submittingComment ? '등록 중' : '댓글 등록'}</button>
      </form>
      <p className="form-message" aria-live="polite">{message}</p>
      <section aria-label="댓글 목록">
        <h3>댓글 {comments.length || post.commentCount || 0}</h3>
        {comments.length === 0 ? (
          <p className="muted-text">등록된 댓글이 없습니다.</p>
        ) : (
          <ul className="info-list">
            {comments.map((item) => (
              <li key={item.id}>
                <strong>{item.authorName || '익명'}</strong>
                <span>{item.content}</span>
                <small>{formatDate(item.createdAt)}</small>
              </li>
            ))}
          </ul>
        )}
      </section>
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
