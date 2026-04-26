import { buildQuery, fetchJson } from './client';
import { mockCategories, mockPosts } from '../data/mockData';
import type {
  BoardAttachmentDraft,
  BoardAttachmentItem,
  BoardCategory,
  BoardCode,
  BoardCommentItem,
  BoardPostDraft,
  BoardPostListItem,
  BoardPostListResponse,
} from '../types';

const PAGE_SIZE = 20;

type BoardPostDetailPayload = BoardPostListItem & {
  engagement?: {
    commentCount?: number;
    reactionCount?: number;
    bookmarkCount?: number;
  };
};
type BoardPostDetailResponse = { post?: BoardPostDetailPayload; item?: BoardPostDetailPayload };
type BoardPostCreateResponse = { item: BoardPostListItem };
type BoardCommentCreateResponse = { item: BoardCommentItem };
type BoardCommentDeleteResponse = { item: { id: number; postId: number; deleted: boolean; demo?: boolean } };
type BoardReactionResponse = { item: { postId: number; type: string; active: boolean; demo?: boolean } };
type BoardPostDeleteResponse = { item: { id: number; boardCode: BoardCode; deleted: boolean; demo?: boolean } };
type BoardAttachmentCreateResponse = { item: BoardAttachmentItem };
type BoardAttachmentDeleteResponse = { item: { id: number; postId: number; deleted: boolean; demo?: boolean } };

interface PostQuery {
  categoryId?: number;
  keyword?: string;
  page: number;
  size?: number;
  sort?: string;
}

function filterPosts(boardCode: BoardCode, query: PostQuery): BoardPostListResponse {
  const keyword = query.keyword?.trim().toLowerCase();
  const page = query.page || 1;
  const size = query.size || PAGE_SIZE;

  const filtered = mockPosts.filter((post) => {
    const matchesBoard = post.boardCode === boardCode;
    const matchesCategory = !query.categoryId || post.category?.id === query.categoryId;
    const matchesKeyword = !keyword || post.title.toLowerCase().includes(keyword);
    return matchesBoard && matchesCategory && matchesKeyword;
  });

  const start = (page - 1) * size;
  const items = filtered.slice(start, start + size);

  return {
    items,
    page: {
      page,
      size,
      totalItems: filtered.length,
      totalPages: Math.ceil(filtered.length / size),
    },
  };
}

export function getCategories(boardCode: BoardCode): Promise<{ items: BoardCategory[] }> {
  return fetchJson<{ items: BoardCategory[] }>(`/api/boards/${boardCode}/categories`, {
    fallback: () => ({ items: mockCategories[boardCode] }),
  });
}

export function getPosts(boardCode: BoardCode, query: PostQuery): Promise<BoardPostListResponse> {
  const params = buildQuery({
    categoryId: query.categoryId,
    keyword: query.keyword?.trim(),
    page: query.page,
    size: query.size || PAGE_SIZE,
    sort: query.sort || 'createdAt,desc',
  });

  return fetchJson<BoardPostListResponse>(`/api/boards/${boardCode}/posts${params}`, {
    fallback: () => filterPosts(boardCode, query),
  });
}

export function getPost(boardCode: BoardCode, postId: number): Promise<BoardPostListItem | undefined> {
  return fetchJson<BoardPostDetailResponse>(`/api/boards/${boardCode}/posts/${postId}`, {
    fallback: () => ({ post: mockPosts.find((item) => item.boardCode === boardCode && item.id === postId) }),
  }).then((response) => normalizePost(response.post ?? response.item));
}

export function createPost(boardCode: BoardCode, draft: BoardPostDraft): Promise<BoardPostListItem> {
  return fetchJson<BoardPostCreateResponse>(`/api/boards/${boardCode}/posts`, {
    body: JSON.stringify(draft),
    fallback: () => ({
      item: {
        id: Date.now(),
        boardCode,
        category: undefined,
        title: draft.title,
        content: draft.content,
        authorName: '로컬 데모',
        createdAt: new Date().toISOString(),
        commentCount: 0,
        reactionCount: 0,
        bookmarkCount: 0,
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}

export function updatePost(boardCode: BoardCode, postId: number, draft: BoardPostDraft): Promise<BoardPostListItem> {
  return fetchJson<BoardPostCreateResponse>(`/api/boards/${boardCode}/posts/${postId}`, {
    body: JSON.stringify(draft),
    fallback: () => ({
      item: {
        id: postId,
        boardCode,
        category: draft.categoryId ? { id: draft.categoryId, name: '선택 카테고리' } : undefined,
        title: draft.title,
        content: draft.content,
        authorName: '로컬 데모',
        createdAt: new Date().toISOString(),
        commentCount: 0,
        reactionCount: 0,
        bookmarkCount: 0,
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'PUT',
  }).then((response) => response.item);
}

export function boardAttachmentUrl(boardCode: BoardCode, postId: number, attachmentId: number): string {
  return `/api/boards/${boardCode}/posts/${postId}/attachments/${attachmentId}`;
}

export function deletePost(boardCode: BoardCode, postId: number): Promise<BoardPostDeleteResponse['item']> {
  return fetchJson<BoardPostDeleteResponse>(`/api/boards/${boardCode}/posts/${postId}`, {
    fallback: () => ({ item: { id: postId, boardCode, deleted: true, demo: true } }),
    method: 'DELETE',
  }).then((response) => response.item);
}

export function createPostAttachment(
  boardCode: BoardCode,
  postId: number,
  draft: BoardAttachmentDraft,
): Promise<BoardAttachmentItem> {
  return fetchJson<BoardAttachmentCreateResponse>(`/api/boards/${boardCode}/posts/${postId}/attachments`, {
    body: JSON.stringify(draft),
    fallback: () => ({
      item: {
        id: Date.now(),
        postId,
        originalFilename: draft.originalFilename,
        storageKey: draft.storageKey,
        storedPath: draft.storedPath,
        mimeType: draft.mimeType,
        fileSize: draft.fileSize,
        createdAt: new Date().toISOString(),
        demo: true,
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}

export function deletePostAttachment(
  boardCode: BoardCode,
  postId: number,
  attachmentId: number,
): Promise<BoardAttachmentDeleteResponse['item']> {
  return fetchJson<BoardAttachmentDeleteResponse>(`/api/boards/${boardCode}/posts/${postId}/attachments/${attachmentId}`, {
    fallback: () => ({ item: { id: attachmentId, postId, deleted: true, demo: true } }),
    method: 'DELETE',
  }).then((response) => response.item);
}

export function createComment(
  boardCode: BoardCode,
  postId: number,
  content: string,
  parentCommentId?: number,
): Promise<BoardCommentItem> {
  return fetchJson<BoardCommentCreateResponse>(`/api/boards/${boardCode}/posts/${postId}/comments`, {
    body: JSON.stringify({ content, parentCommentId }),
    fallback: () => ({
      item: {
        id: Date.now(),
        postId,
        parentCommentId: parentCommentId ?? null,
        content,
        authorName: '로컬 데모',
        createdAt: new Date().toISOString(),
        replies: [],
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}

export function updateComment(
  boardCode: BoardCode,
  postId: number,
  commentId: number,
  content: string,
): Promise<BoardCommentItem> {
  return fetchJson<BoardCommentCreateResponse>(`/api/boards/${boardCode}/posts/${postId}/comments/${commentId}`, {
    body: JSON.stringify({ content }),
    fallback: () => ({
      item: {
        id: commentId,
        postId,
        content,
        authorName: '로컬 데모',
        createdAt: new Date().toISOString(),
        replies: [],
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'PUT',
  }).then((response) => response.item);
}

export function deleteComment(
  boardCode: BoardCode,
  postId: number,
  commentId: number,
): Promise<BoardCommentDeleteResponse['item']> {
  return fetchJson<BoardCommentDeleteResponse>(`/api/boards/${boardCode}/posts/${postId}/comments/${commentId}`, {
    fallback: () => ({ item: { id: commentId, postId, deleted: true, demo: true } }),
    method: 'DELETE',
  }).then((response) => response.item);
}

export function createReaction(
  boardCode: BoardCode,
  postId: number,
  type: 'bookmark' | 'like',
): Promise<BoardReactionResponse['item']> {
  return fetchJson<BoardReactionResponse>(`/api/boards/${boardCode}/posts/${postId}/reactions`, {
    body: JSON.stringify({ type }),
    fallback: () => ({ item: { postId, type, active: true, demo: true } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}

export function deleteReaction(
  boardCode: BoardCode,
  postId: number,
  type: 'bookmark' | 'like',
): Promise<BoardReactionResponse['item']> {
  return fetchJson<BoardReactionResponse>(`/api/boards/${boardCode}/posts/${postId}/reactions/${type}`, {
    fallback: () => ({ item: { postId, type, active: false, demo: true } }),
    method: 'DELETE',
  }).then((response) => response.item);
}

function normalizePost(post?: BoardPostDetailPayload): BoardPostListItem | undefined {
  if (!post) return undefined;

  return {
    ...post,
    commentCount: post.commentCount ?? post.engagement?.commentCount ?? 0,
    reactionCount: post.reactionCount ?? post.engagement?.reactionCount ?? 0,
    bookmarkCount: post.bookmarkCount ?? post.engagement?.bookmarkCount ?? 0,
    hasAttachment: post.hasAttachment || Boolean(post.attachments?.length),
    attachments: post.attachments ?? [],
  };
}
