import { FormEvent, useEffect, useState } from 'react';
import {
  boardAttachmentUrl,
  createComment,
  createPostAttachment,
  createReaction,
  deleteComment,
  deletePost,
  deletePostAttachment,
  deleteReaction,
  getPost,
  updateComment,
  updatePost,
} from '../api/boards';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { BoardAttachmentDraft, BoardAttachmentItem, BoardCode, BoardCommentItem, BoardPostListItem, LoadState } from '../types';

interface BoardDetailPageProps {
  boardCode: BoardCode;
  readOnly?: boolean;
  postId: number;
  title: string;
  listPath: string;
}

function BoardDetailPage({ boardCode, readOnly = false, postId, title, listPath }: BoardDetailPageProps) {
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
      {post ? <DetailContent boardCode={boardCode} post={post} readOnly={readOnly} listPath={listPath} /> : null}
    </section>
  );
}

function DetailContent({ boardCode, post, readOnly, listPath }: { boardCode: BoardCode; post: BoardPostListItem; readOnly: boolean; listPath: string }) {
  const [currentPost, setCurrentPost] = useState(post);
  const [editing, setEditing] = useState(false);
  const [draftTitle, setDraftTitle] = useState(post.title);
  const [draftContent, setDraftContent] = useState(post.content ?? '');
  const [submittingPost, setSubmittingPost] = useState(false);
  const readonlyMessage = '공지/FAQ는 운영자가 관리하는 읽기 전용 콘텐츠입니다.';
  const [postMessage, setPostMessage] = useState(readOnly ? readonlyMessage : '게시글 수정 또는 삭제가 가능합니다.');

  useEffect(() => {
    setCurrentPost(post);
    setDraftTitle(post.title);
    setDraftContent(post.content ?? '');
    setEditing(false);
    setSubmittingPost(false);
    setPostMessage(readOnly ? readonlyMessage : '게시글 수정 또는 삭제가 가능합니다.');
  }, [post, readOnly, readonlyMessage]);

  const submitUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const title = draftTitle.trim();
    const content = draftContent.trim();
    if (!title || !content) return;

    setSubmittingPost(true);
    setPostMessage('게시글을 저장하는 중입니다.');
    try {
      const updated = await updatePost(boardCode, currentPost.id, {
        categoryId: currentPost.category?.id,
        title,
        content,
      });
      setCurrentPost((previous) => ({ ...previous, ...updated, comments: previous.comments }));
      setEditing(false);
      setPostMessage('게시글이 저장되었습니다.');
    } catch (error) {
      setPostMessage(getErrorMessage(error));
    } finally {
      setSubmittingPost(false);
    }
  };

  const requestDelete = async () => {
    if (!window.confirm('게시글을 삭제할까요? 삭제 후에는 목록으로 이동합니다.')) return;

    setSubmittingPost(true);
    setPostMessage('게시글을 삭제하는 중입니다.');
    try {
      const result = await deletePost(boardCode, currentPost.id);
      if (result.deleted) {
        window.location.href = listPath;
        return;
      }
      setPostMessage('게시글 삭제 결과를 확인하지 못했습니다.');
    } catch (error) {
      setPostMessage(getErrorMessage(error));
    } finally {
      setSubmittingPost(false);
    }
  };

  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone="green">{currentPost.category?.name || '일반'}</StatusPill>
        {currentPost.isPinned ? <StatusPill tone="yellow">고정</StatusPill> : null}
        {currentPost.hasAttachment ? <StatusPill tone="blue">첨부 있음</StatusPill> : null}
      </div>
      <h2>{currentPost.title}</h2>
      <dl className="info-list detail-info">
        <div>
          <dt>작성자</dt>
          <dd>{currentPost.authorName || '-'}</dd>
        </div>
        <div>
          <dt>등록일</dt>
          <dd>{formatDate(currentPost.createdAt)}</dd>
        </div>
        <div>
          <dt>조회</dt>
          <dd>{(currentPost.viewCount || 0).toLocaleString('ko-KR')}</dd>
        </div>
      </dl>
      <div className="detail-body">{currentPost.content || '등록된 본문이 없습니다.'}</div>
      {readOnly ? (
        <section className="board-actions readonly-board-actions" aria-label="읽기 전용 안내">
          <StatusPill tone="blue">읽기 전용</StatusPill>
          <p className="form-message" aria-live="polite">{postMessage}</p>
        </section>
      ) : (
        <>
          <section className="board-actions" aria-label="게시글 관리">
            <div className="action-row">
              <button className="ghost-button" disabled={submittingPost} onClick={() => setEditing((value) => !value)} type="button">
                {editing ? '수정 취소' : '수정'}
              </button>
              <button className="ghost-button danger" disabled={submittingPost} onClick={() => { void requestDelete(); }} type="button">
                삭제
              </button>
            </div>
            {editing ? (
              <form className="stack-form" onSubmit={submitUpdate}>
                <label htmlFor={`post-title-${currentPost.id}`}>제목</label>
                <input
                  disabled={submittingPost}
                  id={`post-title-${currentPost.id}`}
                  onChange={(event) => setDraftTitle(event.target.value)}
                  required
                  value={draftTitle}
                />
                <label htmlFor={`post-content-${currentPost.id}`}>내용</label>
                <textarea
                  disabled={submittingPost}
                  id={`post-content-${currentPost.id}`}
                  onChange={(event) => setDraftContent(event.target.value)}
                  required
                  rows={8}
                  value={draftContent}
                />
                <button className="primary-action" disabled={submittingPost} type="submit">
                  {submittingPost ? '저장 중' : '저장'}
                </button>
              </form>
            ) : null}
            <p className="form-message" aria-live="polite">{postMessage}</p>
          </section>
          <AttachmentManager boardCode={boardCode} post={currentPost} onChange={(attachments) => {
            setCurrentPost((previous) => ({ ...previous, attachments, hasAttachment: attachments.length > 0 }));
          }} />
          <BoardActions boardCode={boardCode} post={currentPost} />
        </>
      )}
      <div className="action-row">
        <a className="ghost-button" href={listPath}>목록</a>
      </div>
    </article>
  );
}

function AttachmentManager({
  boardCode,
  onChange,
  post,
}: {
  boardCode: BoardCode;
  onChange: (attachments: BoardAttachmentItem[]) => void;
  post: BoardPostListItem;
}) {
  const [attachments, setAttachments] = useState<BoardAttachmentItem[]>(post.attachments ?? []);
  const [originalFilename, setOriginalFilename] = useState('');
  const [storedPath, setStoredPath] = useState('');
  const [mimeType, setMimeType] = useState('');
  const [fileSize, setFileSize] = useState('');
  const [selectedFile, setSelectedFile] = useState<File>();
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('첨부파일 파일을 선택하면 서버에 저장되고, 기존 메타데이터도 등록할 수 있습니다.');

  useEffect(() => {
    setAttachments(post.attachments ?? []);
    setOriginalFilename('');
    setStoredPath('');
    setMimeType('');
    setFileSize('');
    setSelectedFile(undefined);
    setMessage('첨부파일 파일을 선택하면 서버에 저장되고, 기존 메타데이터도 등록할 수 있습니다.');
  }, [post.id, post.attachments]);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const filename = (originalFilename.trim() || selectedFile?.name || '').trim();
    if (!filename) return;

    setSubmitting(true);
    setMessage('첨부파일을 저장하는 중입니다.');
    try {
      const contentBase64 = selectedFile ? await fileToBase64(selectedFile) : undefined;
      const draft: BoardAttachmentDraft = {
        originalFilename: filename,
        storedPath: storedPath.trim() || undefined,
        mimeType: mimeType.trim() || selectedFile?.type || undefined,
        fileSize: selectedFile ? selectedFile.size : fileSize ? Number(fileSize) : undefined,
        contentBase64,
      };
      const created = await createPostAttachment(boardCode, post.id, draft);
      const nextAttachments = [...attachments, created];
      setAttachments(nextAttachments);
      onChange(nextAttachments);
      setOriginalFilename('');
      setStoredPath('');
      setMimeType('');
      setFileSize('');
      setSelectedFile(undefined);
      setMessage('첨부파일이 저장되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  const remove = async (attachmentId: number) => {
    if (!window.confirm('첨부파일 연결을 삭제할까요?')) return;

    setSubmitting(true);
    setMessage('첨부파일을 삭제하는 중입니다.');
    try {
      const result = await deletePostAttachment(boardCode, post.id, attachmentId);
      if (result.deleted) {
        const nextAttachments = attachments.filter((item) => item.id !== attachmentId);
        setAttachments(nextAttachments);
        onChange(nextAttachments);
        setMessage('첨부파일 연결이 삭제되었습니다.');
      } else {
        setMessage('첨부파일 삭제 결과를 확인하지 못했습니다.');
      }
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="board-actions" aria-label="첨부파일">
      <h3>첨부파일 {attachments.length}</h3>
      {attachments.length === 0 ? (
        <p className="muted-text">등록된 첨부파일이 없습니다.</p>
      ) : (
        <ul className="info-list">
          {attachments.map((attachment) => (
            <li key={attachment.id}>
              <strong>{attachment.originalFilename}</strong>
              <span>{attachment.mimeType || 'unknown'} · {formatFileSize(attachment.fileSize)}</span>
              {!attachment.demo ? (
                <a className="ghost-button" href={boardAttachmentUrl(boardCode, post.id, attachment.id)}>다운로드</a>
              ) : attachment.storedPath ? (
                <a className="ghost-button" href={attachment.storedPath}>열기</a>
              ) : null}
              <button className="ghost-button danger" disabled={submitting} onClick={() => { void remove(attachment.id); }} type="button">
                삭제
              </button>
            </li>
          ))}
        </ul>
      )}
      <form className="stack-form" onSubmit={submit}>
        <label htmlFor={`attachment-file-${post.id}`}>파일 선택</label>
        <input
          disabled={submitting}
          id={`attachment-file-${post.id}`}
          onChange={(event) => {
            const file = event.target.files?.[0];
            setSelectedFile(file);
            if (file) {
              setOriginalFilename(file.name);
              setMimeType(file.type || 'application/octet-stream');
              setFileSize(String(file.size));
            }
          }}
          type="file"
        />
        <label htmlFor={`attachment-name-${post.id}`}>파일명</label>
        <input
          disabled={submitting}
          id={`attachment-name-${post.id}`}
          onChange={(event) => setOriginalFilename(event.target.value)}
          placeholder="예: project-guide.pdf"
          required
          value={originalFilename}
        />
        <label htmlFor={`attachment-path-${post.id}`}>저장 경로</label>
        <input
          disabled={submitting}
          id={`attachment-path-${post.id}`}
          onChange={(event) => setStoredPath(event.target.value)}
          placeholder="/uploads/board/project-guide.pdf"
          value={storedPath}
        />
        <div className="inline-fields">
          <label>
            MIME
            <input disabled={submitting} onChange={(event) => setMimeType(event.target.value)} placeholder="application/pdf" value={mimeType} />
          </label>
          <label>
            크기(byte)
            <input disabled={submitting} min="0" onChange={(event) => setFileSize(event.target.value)} type="number" value={fileSize} />
          </label>
        </div>
        <button className="ghost-button" disabled={submitting} type="submit">{submitting ? '저장 중' : '첨부 추가'}</button>
      </form>
      <p className="form-message" aria-live="polite">{message}</p>
    </section>
  );
}

function BoardActions({ boardCode, post }: { boardCode: BoardCode; post: BoardPostListItem }) {
  const [comment, setComment] = useState('');
  const [comments, setComments] = useState<BoardCommentItem[]>(post.comments ?? []);
  const [replyContent, setReplyContent] = useState('');
  const [replyParentId, setReplyParentId] = useState<number>();
  const [editingCommentId, setEditingCommentId] = useState<number>();
  const [editCommentContent, setEditCommentContent] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [submittingReply, setSubmittingReply] = useState(false);
  const [submittingCommentMutation, setSubmittingCommentMutation] = useState(false);
  const [submittingReaction, setSubmittingReaction] = useState<'bookmark' | 'like'>();
  const [liked, setLiked] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const [message, setMessage] = useState('댓글을 등록하면 서버에 저장됩니다.');

  useEffect(() => {
    setComments(post.comments ?? []);
    setComment('');
    setReplyContent('');
    setReplyParentId(undefined);
    setEditingCommentId(undefined);
    setEditCommentContent('');
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
      setComments((items) => [...items, { ...created, replies: created.replies ?? [] }]);
      setComment('');
      setMessage('댓글이 등록되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmittingComment(false);
    }
  };

  const submitReply = async (event: FormEvent<HTMLFormElement>, parentId: number) => {
    event.preventDefault();
    const content = replyContent.trim();
    if (!content) return;

    setSubmittingReply(true);
    setMessage('답글을 등록하는 중입니다.');
    try {
      const created = await createComment(boardCode, post.id, content, parentId);
      setComments((items) => appendReply(items, parentId, { ...created, replies: created.replies ?? [] }));
      setReplyContent('');
      setReplyParentId(undefined);
      setMessage('답글이 등록되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmittingReply(false);
    }
  };

  const startCommentEdit = (item: BoardCommentItem) => {
    setEditingCommentId(item.id);
    setEditCommentContent(item.content);
    setReplyParentId(undefined);
    setMessage('댓글을 수정할 수 있습니다.');
  };

  const submitCommentEdit = async (event: FormEvent<HTMLFormElement>, commentId: number) => {
    event.preventDefault();
    const content = editCommentContent.trim();
    if (!content) return;

    setSubmittingCommentMutation(true);
    setMessage('댓글을 저장하는 중입니다.');
    try {
      const updated = await updateComment(boardCode, post.id, commentId, content);
      setComments((items) => replaceComment(items, { ...updated, replies: updated.replies ?? [] }));
      setEditingCommentId(undefined);
      setEditCommentContent('');
      setMessage('댓글이 저장되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmittingCommentMutation(false);
    }
  };

  const deleteCommentItem = async (commentId: number) => {
    if (!window.confirm('댓글을 삭제할까요?')) return;

    setSubmittingCommentMutation(true);
    setMessage('댓글을 삭제하는 중입니다.');
    try {
      const result = await deleteComment(boardCode, post.id, commentId);
      if (result.deleted) {
        setComments((items) => removeComment(items, commentId));
        setEditingCommentId(undefined);
        setEditCommentContent('');
        setMessage('댓글이 삭제되었습니다.');
      } else {
        setMessage('댓글 삭제 결과를 확인하지 못했습니다.');
      }
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmittingCommentMutation(false);
    }
  };

  const toggleReaction = async (type: 'bookmark' | 'like', active: boolean) => {
    setSubmittingReaction(type);
    setMessage(active ? '반응을 해제하는 중입니다.' : '반응을 저장하는 중입니다.');
    try {
      const result = active
        ? await deleteReaction(boardCode, post.id, type)
        : await createReaction(boardCode, post.id, type);
      if (type === 'like') {
        setLiked(result.active);
      } else {
        setBookmarked(result.active);
      }
      setMessage(result.active ? '반응이 저장되었습니다.' : '반응이 해제되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmittingReaction(undefined);
    }
  };

  return (
    <section className="board-actions" aria-label="게시글 반응">
      <div className="action-row">
        <button
          className={liked ? 'primary-action' : 'ghost-button'}
          disabled={submittingReaction === 'like'}
          onClick={() => { void toggleReaction('like', liked); }}
          type="button"
        >
          추천 {(post.reactionCount || 0) + (liked ? 1 : 0)}
        </button>
        <button
          className={bookmarked ? 'primary-action' : 'ghost-button'}
          disabled={submittingReaction === 'bookmark'}
          onClick={() => { void toggleReaction('bookmark', bookmarked); }}
          type="button"
        >
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
        <h3>댓글 {totalCommentCount(comments) || post.commentCount || 0}</h3>
        {comments.length === 0 ? (
          <p className="muted-text">등록된 댓글이 없습니다.</p>
        ) : (
          <CommentList
            comments={comments}
            replyContent={replyContent}
            editCommentContent={editCommentContent}
            editingCommentId={editingCommentId}
            submittingCommentMutation={submittingCommentMutation}
            replyParentId={replyParentId}
            submittingReply={submittingReply}
            onCommentDelete={(commentId) => { void deleteCommentItem(commentId); }}
            onCommentEdit={startCommentEdit}
            onCommentEditCancel={() => {
              setEditingCommentId(undefined);
              setEditCommentContent('');
            }}
            onCommentEditContentChange={setEditCommentContent}
            onCommentEditSubmit={submitCommentEdit}
            onReplyContentChange={setReplyContent}
            onReplySubmit={submitReply}
            onReplyToggle={(commentId) => {
              setReplyParentId((current) => (current === commentId ? undefined : commentId));
              setReplyContent('');
            }}
          />
        )}
      </section>
    </section>
  );
}

function CommentList({
  comments,
  editCommentContent,
  editingCommentId,
  replyContent,
  replyParentId,
  submittingCommentMutation,
  submittingReply,
  onCommentDelete,
  onCommentEdit,
  onCommentEditCancel,
  onCommentEditContentChange,
  onCommentEditSubmit,
  onReplyContentChange,
  onReplySubmit,
  onReplyToggle,
}: {
  comments: BoardCommentItem[];
  editCommentContent: string;
  editingCommentId?: number;
  replyContent: string;
  replyParentId?: number;
  submittingCommentMutation: boolean;
  submittingReply: boolean;
  onCommentDelete: (commentId: number) => void;
  onCommentEdit: (comment: BoardCommentItem) => void;
  onCommentEditCancel: () => void;
  onCommentEditContentChange: (value: string) => void;
  onCommentEditSubmit: (event: FormEvent<HTMLFormElement>, commentId: number) => Promise<void>;
  onReplyContentChange: (value: string) => void;
  onReplySubmit: (event: FormEvent<HTMLFormElement>, parentId: number) => Promise<void>;
  onReplyToggle: (commentId: number) => void;
}) {
  return (
    <ul className="info-list">
      {comments.map((item) => (
        <li key={item.id}>
          <strong>{item.authorName || '익명'}</strong>
          {editingCommentId === item.id ? (
            <form className="comment-form" onSubmit={(event) => { void onCommentEditSubmit(event, item.id); }}>
              <label className="visually-hidden" htmlFor={`comment-edit-${item.id}`}>댓글 수정</label>
              <input
                disabled={submittingCommentMutation}
                id={`comment-edit-${item.id}`}
                onChange={(event) => onCommentEditContentChange(event.target.value)}
                required
                value={editCommentContent}
              />
              <button className="ghost-button" disabled={submittingCommentMutation} type="submit">
                {submittingCommentMutation ? '저장 중' : '저장'}
              </button>
              <button className="ghost-button" disabled={submittingCommentMutation} onClick={onCommentEditCancel} type="button">
                취소
              </button>
            </form>
          ) : (
            <span>{item.content}</span>
          )}
          <small>{formatDate(item.createdAt)}</small>
          <div className="action-row">
            <button className="ghost-button" type="button" onClick={() => onReplyToggle(item.id)}>
              {replyParentId === item.id ? '답글 취소' : '답글'}
            </button>
            <button className="ghost-button" disabled={submittingCommentMutation} onClick={() => onCommentEdit(item)} type="button">
              수정
            </button>
            <button className="ghost-button danger" disabled={submittingCommentMutation} onClick={() => onCommentDelete(item.id)} type="button">
              삭제
            </button>
          </div>
          {replyParentId === item.id ? (
            <form className="comment-form" onSubmit={(event) => { void onReplySubmit(event, item.id); }}>
              <label className="visually-hidden" htmlFor={`reply-${item.id}`}>답글</label>
              <input
                disabled={submittingReply}
                id={`reply-${item.id}`}
                onChange={(event) => onReplyContentChange(event.target.value)}
                placeholder="답글을 입력하세요"
                required
                value={replyContent}
              />
              <button className="ghost-button" disabled={submittingReply} type="submit">
                {submittingReply ? '등록 중' : '답글 등록'}
              </button>
            </form>
          ) : null}
          {item.replies?.length ? (
            <div className="detail-info">
              <CommentList
                comments={item.replies}
                editCommentContent={editCommentContent}
                editingCommentId={editingCommentId}
                replyContent={replyContent}
                replyParentId={replyParentId}
                submittingCommentMutation={submittingCommentMutation}
                submittingReply={submittingReply}
                onCommentDelete={onCommentDelete}
                onCommentEdit={onCommentEdit}
                onCommentEditCancel={onCommentEditCancel}
                onCommentEditContentChange={onCommentEditContentChange}
                onCommentEditSubmit={onCommentEditSubmit}
                onReplyContentChange={onReplyContentChange}
                onReplySubmit={onReplySubmit}
                onReplyToggle={onReplyToggle}
              />
            </div>
          ) : null}
        </li>
      ))}
    </ul>
  );
}

function appendReply(comments: BoardCommentItem[], parentId: number, reply: BoardCommentItem): BoardCommentItem[] {
  return comments.map((item) => {
    if (item.id === parentId) {
      return { ...item, replies: [...(item.replies ?? []), reply] };
    }
    if (item.replies?.length) {
      return { ...item, replies: appendReply(item.replies, parentId, reply) };
    }
    return item;
  });
}

function replaceComment(comments: BoardCommentItem[], updated: BoardCommentItem): BoardCommentItem[] {
  return comments.map((item) => {
    if (item.id === updated.id) {
      return { ...item, ...updated, replies: item.replies };
    }
    if (item.replies?.length) {
      return { ...item, replies: replaceComment(item.replies, updated) };
    }
    return item;
  });
}

function removeComment(comments: BoardCommentItem[], commentId: number): BoardCommentItem[] {
  return comments
    .filter((item) => item.id !== commentId)
    .map((item) => (item.replies?.length ? { ...item, replies: removeComment(item.replies, commentId) } : item));
}

function totalCommentCount(comments: BoardCommentItem[]): number {
  return comments.reduce((total, item) => total + 1 + totalCommentCount(item.replies ?? []), 0);
}

function formatDate(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}


function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('첨부파일을 읽지 못했습니다.'));
    reader.onload = () => {
      const result = String(reader.result || '');
      const [, base64 = ''] = result.split(',');
      resolve(base64);
    };
    reader.readAsDataURL(file);
  });
}

function formatFileSize(value?: number | null): string {
  if (typeof value !== 'number') return '크기 미상';
  if (value < 1024) return `${value.toLocaleString('ko-KR')} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / (1024 * 1024)).toFixed(1)} MB`;
}

export default BoardDetailPage;
