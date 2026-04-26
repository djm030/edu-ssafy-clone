import { FormEvent, useEffect, useState } from 'react';
import { getQuests } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, QuestItem, QuestListSummary } from '../types';

const statusOptions = [
  { label: '전체', value: 'all', countKey: 'totalCount' },
  { label: '진행중', value: 'progress', countKey: 'progressCount' },
  { label: '제출완료', value: 'submitted', countKey: 'submittedCount' },
  { label: '채점완료', value: 'graded', countKey: 'gradedCount' },
  { label: '마감초과', value: 'overdue', countKey: 'overdueCount' },
] as const;

const emptySummary: QuestListSummary = {
  totalCount: 0,
  progressCount: 0,
  submittedCount: 0,
  gradedCount: 0,
  overdueCount: 0,
};

const statusMap = {
  done: { label: '제출완료', tone: 'green' },
  submitted: { label: '제출완료', tone: 'green' },
  graded: { label: '채점완료', tone: 'blue' },
  progress: { label: '진행중', tone: 'yellow' },
  overdue: { label: '마감초과', tone: 'red' },
} as const;

function QuestPage() {
  const [items, setItems] = useState<QuestItem[]>([]);
  const [summary, setSummary] = useState<QuestListSummary>(emptySummary);
  const [status, setStatus] = useState<(typeof statusOptions)[number]['value']>('all');
  const [keyword, setKeyword] = useState('');
  const [submittedKeyword, setSubmittedKeyword] = useState('');
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getQuests({ status, keyword: submittedKeyword, size: 20 })
      .then((response) => {
        if (ignore) return;
        setItems(response.items);
        setSummary(response.summary);
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
  }, [status, submittedKeyword]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmittedKeyword(keyword.trim());
  };

  return (
    <section className="page">
      <PageHeader eyebrow="QUEST" title="Quest/평가" description="진행 기간, 제출 상태, 채점 결과를 필터링해 확인합니다." />
      <div className="summary-grid" aria-label="Quest 상태 요약">
        <SummaryCard title="전체" value={summary.totalCount} description="배정된 Quest" />
        <SummaryCard title="진행중" value={summary.progressCount} description="제출 전" />
        <SummaryCard title="제출완료" value={summary.submittedCount} description="채점 대기" />
        <SummaryCard title="채점완료" value={summary.gradedCount} description={`마감초과 ${summary.overdueCount}건`} />
      </div>
      <div className="filter-bar">
        <div className="category-strip">
          {statusOptions.map((option) => (
            <button className={status === option.value ? 'category-chip active' : 'category-chip'} key={option.value} onClick={() => setStatus(option.value)} type="button">
              {option.label}
              <span className="chip-count">{summary[option.countKey]}</span>
            </button>
          ))}
        </div>
        <form className="search-form" onSubmit={submit}>
          <input onChange={(event) => setKeyword(event.target.value)} placeholder="Quest 제목, 유형, 분류 검색" value={keyword} />
          <button type="submit">검색</button>
        </form>
      </div>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="Quest 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="조건에 맞는 Quest가 없습니다." message="상태 필터 또는 검색어를 바꿔 보세요." /> : null}
      {loadState === 'loaded' ? (
        <>
          <QuestTypeSummary items={items} />
          <QuestList items={items} />
        </>
      ) : null}
    </section>
  );
}

function SummaryCard({ title, value, description }: { title: string; value: number; description: string }) {
  return (
    <article className="stat-card">
      <p>{title}</p>
      <strong>{value}</strong>
      <span>{description}</span>
    </article>
  );
}

function QuestTypeSummary({ items }: { items: QuestItem[] }) {
  const questCount = items.filter((item) => questTypeLabel(item) === 'Quest').length;
  const evaluationCount = items.length - questCount;
  const resultOpenCount = items.filter((item) => item.resultStatus === 'graded' || item.status === 'graded').length;

  return (
    <section className="panel quest-type-summary" aria-label="Quest 평가 타입과 결과 공개 요약">
      <div className="section-heading compact-heading">
        <div>
          <p>QUEST / EVALUATION</p>
          <h2>유형별 제출 현황</h2>
        </div>
        <span>{resultOpenCount}건 결과 공개</span>
      </div>
      <div className="quest-type-summary-grid">
        <SummaryCard title="Quest" value={questCount} description="실습/과제형 제출" />
        <SummaryCard title="평가" value={evaluationCount} description="평가/시험형 항목" />
        <SummaryCard title="결과 공개" value={resultOpenCount} description="채점 결과 확인 가능" />
      </div>
    </section>
  );
}

function QuestList({ items }: { items: QuestItem[] }) {
  return (
    <div className="card-list" aria-label="Quest 목록">
      {items.map((item) => {
        const status = statusMap[item.status];
        return (
          <article className="list-card" key={item.id}>
            <div>
              <p className="eyebrow">{questTypeLabel(item)} · {item.description || 'SSAFY Quest'}</p>
              <h2>{item.title}</h2>
              <p>
                {item.startsAt || '-'} ~ {item.endsAt || '-'}
              </p>
              <p>
                제출: {formatCode(item.submitStatus)} · 결과: {formatCode(item.resultStatus)}{item.maxExp ? ` · 최대 EXP ${item.maxExp}` : ''}
              </p>
              <p className="muted">{questActionPolicy(item)}</p>
              {item.tasks?.length ? <p>{item.tasks.join(' · ')}</p> : null}
            </div>
            <StatusPill tone={status.tone}>{status.label}</StatusPill>
            <a className="ghost-button" href={`/quest/${item.id}`}>
              상세
            </a>
          </article>
        );
      })}
    </div>
  );
}

function questTypeLabel(item: QuestItem): 'Quest' | '평가' {
  const text = `${item.title} ${item.description || ''}`.toLowerCase();
  return text.includes('평가') || text.includes('evaluation') || text.includes('exam') ? '평가' : 'Quest';
}

function questActionPolicy(item: QuestItem): string {
  if (item.status === 'graded' || item.resultStatus === 'graded') return '결과가 공개되어 점수와 피드백 확인이 가능합니다.';
  if (item.submitStatus === 'submitted' || item.status === 'submitted') return '제출 완료 상태이며 채점 결과 공개를 기다리고 있습니다.';
  if (item.status === 'overdue') return '마감이 지나 신규 제출 또는 재제출이 제한됩니다.';
  return '제출 전 evidence를 확인하고 기간 내 제출할 수 있습니다.';
}

function formatCode(value?: string | null): string {
  if (!value) return '미제출';
  const labels: Record<string, string> = {
    submitted: '제출완료',
    done: '제출완료',
    pending: '채점대기',
    graded: '채점완료',
    rejected: '반려',
  };
  return labels[value] || value;
}

export default QuestPage;
