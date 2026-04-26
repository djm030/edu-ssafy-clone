import { FormEvent, useEffect, useState } from 'react';
import { getMentorStories, getMentorStory } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, MentorStoryItem, PageMeta } from '../types';

function MentorStoriesPage({ storyId }: { storyId?: number }) {
  if (storyId) return <MentorStoryDetail storyId={storyId} />;
  return <MentorStoryList />;
}

function MentorStoryList() {
  const [stories, setStories] = useState<MentorStoryItem[]>([]);
  const [page, setPage] = useState<PageMeta>({ page: 1, size: 20, totalItems: 0, totalPages: 0 });
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentorStories({ keyword, page: page.page, size: page.size })
      .then((response) => {
        if (ignore) return;
        setStories(response.items);
        setPage(response.page);
        setLoadState(response.items.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [keyword, page.page, page.size]);

  const submitSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setPage((current) => ({ ...current, page: 1 }));
    setKeyword(keywordInput.trim());
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="멘토 스토리" description="현업 멘토가 전하는 학습 전략과 커리어 성장 사례를 확인합니다." />
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submitSearch}>
          <label htmlFor="mentor-story-keyword">멘토 스토리 검색</label>
          <input id="mentor-story-keyword" onChange={(event) => setKeywordInput(event.target.value)} placeholder="회사, 직무, 제목 검색" value={keywordInput} />
          <button className="primary-action" type="submit">검색</button>
        </form>
      </section>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="멘토 스토리를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="멘토 스토리가 없습니다." message="다른 검색어를 입력해 주세요." /> : null}
      {loadState === 'loaded' ? <StoryCards stories={stories} /> : null}
    </section>
  );
}

function StoryCards({ stories }: { stories: MentorStoryItem[] }) {
  return (
    <section className="grid-list">
      {stories.map((story) => (
        <article className="panel card" key={story.id}>
          <StatusPill tone="blue">{story.mentorCompany} · {story.mentorRole}</StatusPill>
          <h2><a href={`/mentoring/stories/${story.id}`}>{story.title}</a></h2>
          <p>{story.summary || '멘토의 학습과 커리어 경험을 확인하세요.'}</p>
          <span className="muted">{story.mentorName} · 조회 {story.viewCount.toLocaleString('ko-KR')} · {formatDate(story.publishedAt)}</span>
        </article>
      ))}
    </section>
  );
}

function MentorStoryDetail({ storyId }: { storyId: number }) {
  const [story, setStory] = useState<MentorStoryItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentorStory(storyId)
      .then((response) => {
        if (ignore) return;
        setStory(response);
        setLoadState('loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [storyId]);

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="멘토 스토리 상세" description="멘토의 경험을 읽고 나의 학습 계획에 반영합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="멘토 스토리를 불러오지 못했습니다." message={errorMessage} /> : null}
      {story ? (
        <article className="panel article-detail">
          <StatusPill tone="green">{story.mentorCompany} · {story.mentorRole}</StatusPill>
          <h2>{story.title}</h2>
          <p className="muted">{story.mentorName} · 조회 {story.viewCount.toLocaleString('ko-KR')} · {formatDate(story.publishedAt)}</p>
          <p>{story.content || story.summary}</p>
          <a className="ghost-button" href="/mentoring/stories">목록으로</a>
        </article>
      ) : null}
    </section>
  );
}

function formatDate(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(date);
}

export default MentorStoriesPage;
