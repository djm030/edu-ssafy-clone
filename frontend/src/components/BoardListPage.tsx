import { FormEvent, useEffect, useMemo, useState } from 'react';
import { getCategories, getPosts } from '../api/boards';
import { getErrorMessage } from '../api/client';
import type { BoardCategory, BoardPostListItem, BoardScreenConfig, LoadState, PageMeta } from '../types';
import DataState, { LoadingRows } from './DataState';
import PageHeader from './PageHeader';

const PAGE_SIZE = 20;

interface BoardListPageProps {
  config: BoardScreenConfig;
}

function BoardListPage({ config }: BoardListPageProps) {
  const [categories, setCategories] = useState<BoardCategory[]>([]);
  const [posts, setPosts] = useState<BoardPostListItem[]>([]);
  const [pageMeta, setPageMeta] = useState<PageMeta>({ page: 1, size: PAGE_SIZE, totalItems: 0, totalPages: 0 });
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [keyword, setKeyword] = useState('');
  const [submittedKeyword, setSubmittedKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    setCategories([]);
    setPosts([]);
    setCategoryId(undefined);
    setKeyword('');
    setSubmittedKeyword('');
    setPageMeta({ page: 1, size: PAGE_SIZE, totalItems: 0, totalPages: 0 });
  }, [config.boardCode]);

  useEffect(() => {
    let ignore = false;

    async function loadBoard() {
      setLoadState((current) => (current === 'loaded' || current === 'empty' ? 'refreshing' : 'loading'));
      setErrorMessage('');

      try {
        const [categoryResponse, postResponse] = await Promise.all([
          getCategories(config.boardCode),
          getPosts(config.boardCode, { categoryId, keyword: submittedKeyword, page: pageMeta.page, size: PAGE_SIZE }),
        ]);

        if (ignore) return;
        setCategories(categoryResponse.items);
        setPosts(postResponse.items);
        setPageMeta(postResponse.page);
        setLoadState(postResponse.items.length > 0 ? 'loaded' : 'empty');
      } catch (error) {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      }
    }

    void loadBoard();
    return () => {
      ignore = true;
    };
  }, [categoryId, config.boardCode, pageMeta.page, retryToken, submittedKeyword]);

  const currentCategoryName = useMemo(() => {
    if (!categoryId) return '전체';
    return categories.find((category) => category.id === categoryId)?.name || '선택한 카테고리';
  }, [categories, categoryId]);

  const submitSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setPageMeta((current) => ({ ...current, page: 1 }));
    setSubmittedKeyword(keyword.trim());
  };

  const selectCategory = (nextCategoryId?: number) => {
    setCategoryId(nextCategoryId);
    setPageMeta((current) => ({ ...current, page: 1 }));
  };

  const totalPages = Math.max(pageMeta.totalPages, posts.length ? 1 : 0);

  return (
    <section className="page">
      <PageHeader action={renderWriteAction(config)} description={config.description} eyebrow={config.eyebrow} title={config.title} />

      <div className="filter-bar" aria-label={`${config.title} 검색 조건`}>
        <CategoryFilter categories={categories} selectedId={categoryId} onSelect={selectCategory} />
        <form className="search-form" onSubmit={submitSearch}>
          <label className="visually-hidden" htmlFor={`${config.boardCode}-keyword`}>
            검색어
          </label>
          <input
            id={`${config.boardCode}-keyword`}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder={config.searchPlaceholder}
            value={keyword}
          />
          <button type="submit">검색</button>
        </form>
      </div>

      <div className="list-summary" aria-live="polite">
        <span>{currentCategoryName}</span>
        <span>총 {pageMeta.totalItems.toLocaleString('ko-KR')}건</span>
        {loadState === 'refreshing' ? <span>새로고침 중</span> : null}
      </div>

      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? (
        <DataState title="목록을 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} />
      ) : null}
      {loadState === 'empty' ? <DataState title={config.emptyMessage} message="검색어 또는 카테고리를 바꿔 보세요." /> : null}
      {(loadState === 'loaded' || loadState === 'refreshing') && posts.length > 0 ? (
        <>
          <PostTable posts={posts} showEngagement={config.showEngagement} />
          <Pagination page={pageMeta.page} totalPages={totalPages} onMove={(nextPage) => setPageMeta((current) => ({ ...current, page: nextPage }))} />
        </>
      ) : null}
    </section>
  );
}

function renderWriteAction(config: BoardScreenConfig) {
  if (!config.showWriteAction) return null;
  const className = config.writeDisabled ? 'primary-action disabled' : 'primary-action';

  return (
    <a className={className} href={config.writePath || '#'} aria-disabled={config.writeDisabled ? 'true' : undefined}>
      글쓰기
    </a>
  );
}

function CategoryFilter({
  categories,
  selectedId,
  onSelect,
}: {
  categories: BoardCategory[];
  selectedId?: number;
  onSelect: (id?: number) => void;
}) {
  return (
    <div className="category-strip" aria-label="카테고리">
      <button className={!selectedId ? 'category-chip active' : 'category-chip'} onClick={() => onSelect(undefined)} type="button">
        전체
      </button>
      {categories.map((category) => (
        <button
          className={category.id === selectedId ? 'category-chip active' : 'category-chip'}
          key={category.id}
          onClick={() => onSelect(category.id)}
          type="button"
        >
          {category.name}
          {typeof category.postCount === 'number' ? <span className="chip-count">{category.postCount}</span> : null}
        </button>
      ))}
    </div>
  );
}

function PostTable({ posts, showEngagement }: { posts: BoardPostListItem[]; showEngagement: boolean }) {
  return (
    <div className={showEngagement ? 'board-table' : 'board-table compact'} role="table" aria-label="게시글 목록">
      <div className="table-row table-head" role="row">
        <span role="columnheader">카테고리</span>
        <span role="columnheader">제목</span>
        <span role="columnheader">작성자</span>
        <span role="columnheader">등록일</span>
        <span role="columnheader">조회</span>
        {showEngagement ? <span role="columnheader">반응</span> : null}
      </div>
      {posts.map((post) => (
        <a className={post.isPinned ? 'table-row pinned' : 'table-row'} href={postDetailPath(post)} key={post.id} role="row">
          <span className="category-cell" role="cell">{post.category?.name || '-'}</span>
          <span className="title-cell" role="cell">
            {post.isPinned ? <span className="badge">고정</span> : null}
            <strong>{post.title}</strong>
            {post.isNew ? <span className="badge muted">NEW</span> : null}
            {post.hasAttachment ? <span className="attachment">첨부</span> : null}
          </span>
          <span role="cell">{post.authorName || '-'}</span>
          <span role="cell">{formatDate(post.createdAt)}</span>
          <span role="cell">{formatNumber(post.viewCount)}</span>
          {showEngagement ? (
            <span className="metric-cell" role="cell">
              댓글 {formatNumber(post.commentCount)} · 추천 {formatNumber(post.reactionCount)} · 찜 {formatNumber(post.bookmarkCount)}
            </span>
          ) : null}
        </a>
      ))}
    </div>
  );
}

function postDetailPath(post: BoardPostListItem): string {
  if (post.boardCode === 'free') return `/community/free/${post.id}`;
  if (post.boardCode === 'notice') return `/help/notice/${post.id}`;
  if (post.boardCode === 'faq') return `/help/faq/${post.id}`;
  return '/help/qna';
}

function Pagination({ page, totalPages, onMove }: { page: number; totalPages: number; onMove: (page: number) => void }) {
  if (totalPages <= 1) return null;

  return (
    <div className="pagination" aria-label="페이지 이동">
      <button disabled={page <= 1} onClick={() => onMove(page - 1)} type="button">이전</button>
      <span>{page} / {totalPages}</span>
      <button disabled={page >= totalPages} onClick={() => onMove(page + 1)} type="button">다음</button>
    </div>
  );
}

function formatDate(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat('ko-KR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(date);
}

function formatNumber(value?: number): string {
  return (value || 0).toLocaleString('ko-KR');
}

export default BoardListPage;
