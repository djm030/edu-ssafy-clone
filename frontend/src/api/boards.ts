import { buildQuery, fetchJson } from './client';
import { mockCategories, mockPosts } from '../data/mockData';
import type { BoardCategory, BoardCode, BoardCommentItem, BoardPostDraft, BoardPostListItem, BoardPostListResponse } from '../types';

const PAGE_SIZE = 20;

type BoardPostDetailResponse = { post?: BoardPostListItem; item?: BoardPostListItem };
type BoardPostCreateResponse = { item: BoardPostListItem };
type BoardCommentCreateResponse = { item: BoardCommentItem };

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
  }).then((response) => response.post ?? response.item);
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

export function createComment(boardCode: BoardCode, postId: number, content: string): Promise<BoardCommentItem> {
  return fetchJson<BoardCommentCreateResponse>(`/api/boards/${boardCode}/posts/${postId}/comments`, {
    body: JSON.stringify({ content }),
    fallback: () => ({
      item: {
        id: Date.now(),
        postId,
        content,
        authorName: '로컬 데모',
        createdAt: new Date().toISOString(),
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}
