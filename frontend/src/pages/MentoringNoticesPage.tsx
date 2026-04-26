import { FormEvent, useEffect, useState } from 'react';
import { getMentoringNotice, getMentoringNotices } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, MentoringNoticeItem, PageMeta } from '../types';

function MentoringNoticesPage({ noticeId }: { noticeId?: number }) {
  if (noticeId) return <MentoringNoticeDetail noticeId={noticeId} />;
  return <MentoringNoticeList />;
}

function MentoringNoticeList() {
  const [notices, setNotices] = useState<MentoringNoticeItem[]>([]);
  const [page, setPage] = useState<PageMeta>({ page: 1, size: 20, totalItems: 0, totalPages: 0 });
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringNotices({ keyword, page: page.page, size: page.size })
      .then((response) => {
        if (ignore) return;
        setNotices(response.items);
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
      <PageHeader eyebrow="MENTORING" title="멘토링 공지사항" description="멘토링 프로그램 운영 공지, 특강 안내, Q&A 답변 일정을 확인합니다." />
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submitSearch}>
          <label htmlFor="mentoring-notice-keyword">공지 검색</label>
          <input id="mentoring-notice-keyword" onChange={(event) => setKeywordInput(event.target.value)} placeholder="제목 또는 내용 검색" value={keywordInput} />
          <button className="primary-action" type="submit">검색</button>
        </form>
      </section>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="멘토링 공지를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="멘토링 공지가 없습니다." message="다른 검색어를 입력해 주세요." /> : null}
      {loadState === 'loaded' ? <NoticeCards notices={notices} /> : null}
    </section>
  );
}

function NoticeCards({ notices }: { notices: MentoringNoticeItem[] }) {
  return (
    <section className="grid-list">
      {notices.map((notice) => (
        <article className="panel card" key={notice.id}>
          <StatusPill tone={notice.pinned ? 'yellow' : 'blue'}>{notice.pinned ? '고정 공지' : notice.categoryName}</StatusPill>
          <h2><a href={`/mentoring/notices/${notice.id}`}>{notice.title}</a></h2>
          <p>{notice.summary || '멘토링 운영 안내를 확인하세요.'}</p>
          <span className="muted">{notice.categoryName} · 조회 {notice.viewCount.toLocaleString('ko-KR')} · {formatDate(notice.publishedAt)}</span>
        </article>
      ))}
    </section>
  );
}

function MentoringNoticeDetail({ noticeId }: { noticeId: number }) {
  const [notice, setNotice] = useState<MentoringNoticeItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getMentoringNotice(noticeId)
      .then((response) => {
        if (ignore) return;
        setNotice(response);
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
  }, [noticeId]);

  return (
    <section className="page">
      <PageHeader eyebrow="MENTORING" title="멘토링 공지 상세" description="멘토링 관련 세부 안내를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="멘토링 공지를 불러오지 못했습니다." message={errorMessage} /> : null}
      {notice ? (
        <article className="panel article-detail">
          <StatusPill tone={notice.pinned ? 'yellow' : 'blue'}>{notice.pinned ? '고정 공지' : notice.categoryName}</StatusPill>
          <h2>{notice.title}</h2>
          <p className="muted">{notice.categoryName} · 조회 {notice.viewCount.toLocaleString('ko-KR')} · {formatDate(notice.publishedAt)}</p>
          <p>{notice.content || notice.summary}</p>
          <a className="ghost-button" href="/mentoring/notices">목록으로</a>
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

export default MentoringNoticesPage;
