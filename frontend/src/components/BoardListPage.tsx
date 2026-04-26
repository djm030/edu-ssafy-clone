import { FormEvent, useEffect, useMemo, useState } from 'react';
import { getCategories, getPost, getPosts } from '../api/boards';
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
  const [faqAnswers, setFaqAnswers] = useState<Record<number, string>>({});

  useEffect(() => {
    setCategories([]);
    setPosts([]);
    setCategoryId(undefined);
    setKeyword('');
    setSubmittedKeyword('');
    setPageMeta({ page: 1, size: PAGE_SIZE, totalItems: 0, totalPages: 0 });
    setFaqAnswers({});
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

        const nextFaqAnswers = config.boardCode === 'faq' && postResponse.items.length > 0
          ? await loadFaqAnswers(postResponse.items)
          : {};

        if (ignore) return;
        setCategories(categoryResponse.items);
        setPosts(postResponse.items);
        setFaqAnswers(nextFaqAnswers);
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
        <label className="board-category-select">
          <span>카테고리 선택</span>
          <select onChange={(event) => selectCategory(event.target.value ? Number(event.target.value) : undefined)} value={categoryId ?? ''}>
            <option value="">전체</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}{typeof category.postCount === 'number' ? ` (${category.postCount})` : ''}
              </option>
            ))}
          </select>
        </label>
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
          <BoardResultSummary posts={posts} />
          {config.boardCode === 'faq' ? <FaqAccordion posts={posts} answers={faqAnswers} /> : <PostTable posts={posts} showEngagement={config.showEngagement} />}
          <Pagination page={pageMeta.page} totalPages={totalPages} onMove={(nextPage) => setPageMeta((current) => ({ ...current, page: nextPage }))} />
        </>
      ) : null}
    </section>
  );
}

function BoardResultSummary({ posts }: { posts: BoardPostListItem[] }) {
  const pinnedCount = posts.filter((post) => post.isPinned).length;
  const attachmentCount = posts.filter((post) => post.hasAttachment).length;
  const newCount = posts.filter((post) => post.isNew).length;
  const commentCount = posts.reduce((total, post) => total + (post.commentCount || 0), 0);

  return (
    <section className="panel board-result-summary" aria-label="게시판 목록 상태 요약">
      <div className="board-result-summary-grid">
        <SummaryMetric label="고정" value={`${pinnedCount}건`} />
        <SummaryMetric label="신규" value={`${newCount}건`} />
        <SummaryMetric label="첨부" value={`${attachmentCount}건`} />
        <SummaryMetric label="댓글" value={`${commentCount.toLocaleString('ko-KR')}건`} />
      </div>
    </section>
  );
}

function SummaryMetric({ label, value }: { label: string; value: string }) {
  return (
    <div className="board-summary-metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}


async function loadFaqAnswers(posts: BoardPostListItem[]): Promise<Record<number, string>> {
  const details = await Promise.all(posts.map(async (post) => {
    try {
      return await getPost('faq', post.id);
    } catch {
      return undefined;
    }
  }));

  return details.reduce<Record<number, string>>((answers, detail) => {
    if (detail?.content) {
      answers[detail.id] = detail.content;
    }
    return answers;
  }, {});
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


function FaqAccordion({ posts, answers }: { posts: BoardPostListItem[]; answers: Record<number, string> }) {
  return (
    <div className="faq-accordion" aria-label="FAQ 목록">
      {posts.map((post) => (
        <details className="faq-item" key={post.id}>
          <summary>
            <span className="category-cell">{post.category?.name || 'FAQ'}</span>
            <strong>{post.title}</strong>
            {post.isNew ? <span className="badge muted">NEW</span> : null}
          </summary>
          <p>{answers[post.id] || '답변을 불러오는 중입니다.'}</p>
          <a className="ghost-button" href={`/help/faq/${post.id}`}>상세 보기</a>
        </details>
      ))}
    </div>
  );
}

function PostTable({ posts, showEngagement }: { posts: BoardPostListItem[]; showEngagement: boolean }) {
  return (
    <div className={showEngagement ? 'board-table community-board-table' : 'board-table community-board-table compact'} role="table" aria-label="게시글 목록">
      <div className="table-row table-head" role="row">
        <span role="columnheader">카테고리</span>
        <span role="columnheader">제목</span>
        <span role="columnheader">작성자</span>
        <span role="columnheader">기수/반</span>
        <span role="columnheader">작성일</span>
        <span role="columnheader">댓글/조회</span>
        <span role="columnheader">{showEngagement ? '추천/첨부' : '첨부'}</span>
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
          <span role="cell">{cohortClassLabel(post)}</span>
          <span role="cell">{formatDate(post.createdAt)}</span>
          <span role="cell">댓글 {formatNumber(post.commentCount)} · 조회 {formatNumber(post.viewCount)}</span>
          <span className="metric-cell" role="cell">
            {showEngagement ? `추천 ${formatNumber(post.reactionCount)} · ` : ''}{post.hasAttachment ? '첨부 있음' : '첨부 없음'}
          </span>
        </a>
      ))}
    </div>
  );
}

function cohortClassLabel(post: BoardPostListItem): string {
  if (post.boardCode === 'anonymous') return '익명 보호';
  if (post.boardCode === 'notice' || post.boardCode === 'faq') return '운영 공지';
  return post.category?.name || '공개 범위 내';
}

function postDetailPath(post: BoardPostListItem): string {
  if (post.boardCode === 'free') return `/community/free/${post.id}`;
  if (post.boardCode === 'anonymous') return `/community/anonymous/${post.id}`;
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
