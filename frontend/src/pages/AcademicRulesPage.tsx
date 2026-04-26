import { FormEvent, useEffect, useMemo, useState } from 'react';
import { getAcademicRules } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { AcademicRuleCategory, AcademicRuleItem, LoadState } from '../types';

function AcademicRulesPage() {
  const [categories, setCategories] = useState<AcademicRuleCategory[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | undefined>();
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [serverTotalRuleCount, setServerTotalRuleCount] = useState(0);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getAcademicRules({ categoryId: selectedCategoryId, keyword })
      .then((response) => {
        if (ignore) return;
        setCategories(response.categories);
        setServerTotalRuleCount(response.totalRuleCount ?? response.categories.reduce((total, category) => total + category.rules.length, 0));
        setLoadState(response.categories.some((category) => category.rules.length > 0) ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });

    return () => {
      ignore = true;
    };
  }, [keyword, selectedCategoryId]);

  const totalRuleCount = useMemo(
    () => serverTotalRuleCount || categories.reduce((total, category) => total + category.rules.length, 0),
    [categories, serverTotalRuleCount],
  );

  const submitSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setKeyword(keywordInput.trim());
  };

  const resetFilters = () => {
    setKeywordInput('');
    setKeyword('');
    setSelectedCategoryId(undefined);
  };

  return (
    <section className="page">
      <PageHeader
        eyebrow="HELP DESK"
        title="학사규정"
        description="출결, 평가, 수료, 포인트 등 교육 생활에 필요한 학사 기준을 카테고리별로 확인합니다."
      />

      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submitSearch}>
          <label htmlFor="academic-rule-keyword">규정 검색</label>
          <input
            id="academic-rule-keyword"
            onChange={(event) => setKeywordInput(event.target.value)}
            placeholder="예: 출결, 재평가, 포인트"
            value={keywordInput}
          />
          <button className="primary-action" type="submit">검색</button>
          {(keyword || selectedCategoryId) ? (
            <button className="ghost-button" onClick={resetFilters} type="button">검색 초기화</button>
          ) : null}
        </form>
        <div className="action-row" role="list" aria-label="학사규정 카테고리">
          <button className={selectedCategoryId === undefined ? 'primary-action' : 'ghost-button'} onClick={() => setSelectedCategoryId(undefined)} type="button">
            전체
          </button>
          {categories.map((category) => (
            <button
              aria-pressed={selectedCategoryId === category.id}
              className={selectedCategoryId === category.id ? 'primary-action' : 'ghost-button'}
              key={category.id}
              onClick={() => setSelectedCategoryId(category.id)}
              type="button"
            >
              {category.name} ({category.ruleCount})
            </button>
          ))}
        </div>
        {keyword ? <p className="form-message" aria-live="polite">“{keyword}” 검색 결과 {totalRuleCount.toLocaleString('ko-KR')}건</p> : null}
      </section>

      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="학사규정을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? (
        <div className="panel">
          <DataState
            title="검색된 학사규정이 없습니다."
            message="다른 검색어를 입력하거나 1:1 문의로 운영팀에 질문해 주세요."
          />
          <a className="ghost-button" href="/help/qna/new">1:1 문의하기</a>
        </div>
      ) : null}
      {loadState === 'loaded' ? <RuleCategoryList categories={categories} keyword={keyword} totalRuleCount={totalRuleCount} /> : null}
    </section>
  );
}

function RuleCategoryList({
  categories,
  keyword,
  totalRuleCount,
}: {
  categories: AcademicRuleCategory[];
  keyword: string;
  totalRuleCount: number;
}) {
  const rules = categories.flatMap((category) => category.rules);

  return (
    <section className="panel">
      <div className="section-heading">
        <div>
          <h2>규정 {totalRuleCount.toLocaleString('ko-KR')}건</h2>
          <p>{keyword ? `“${keyword}”와 관련된 규정의 위치로 바로 이동할 수 있습니다.` : '각 항목을 열어 세부 기준을 확인하세요.'}</p>
        </div>
        <a className="ghost-button" href="/help/qna/new">추가 문의</a>
      </div>
      {rules.length ? (
        <nav className="action-row" aria-label="학사규정 바로가기">
          {rules.slice(0, 8).map((rule) => (
            <a className="ghost-button" href={`#${rule.anchorId || `rule-${rule.id}`}`} key={rule.id}>
              {rule.question}
            </a>
          ))}
        </nav>
      ) : null}
      <div className="stack-list">
        {categories.map((category) => (
          <article className="card" key={category.id}>
            <div className="card-header">
              <h3>{category.name}</h3>
              <StatusPill tone="blue">{category.ruleCount.toLocaleString('ko-KR')}건</StatusPill>
            </div>
            {category.rules.length ? <RuleList rules={category.rules} /> : <p className="muted">이 카테고리에 표시할 규정이 없습니다.</p>}
          </article>
        ))}
      </div>
    </section>
  );
}

function RuleAnchorSummary({ rules }: { rules: AcademicRuleItem[] }) {
  return (
    <section className="academic-rule-anchor-summary" aria-label="학사규정 상세 앵커와 다운로드 상태">
      <div>
        <strong>상세 앵커</strong>
        <span>{rules.filter((rule) => rule.anchorId || rule.detailPath).length}/{rules.length}개</span>
      </div>
      <div>
        <strong>파일 다운로드</strong>
        <span>운영 등록 파일 없음</span>
      </div>
      <p>규정별 상세 링크는 페이지 앵커로 제공하고, 별도 파일이 없는 규정은 다운로드 버튼을 비활성 상태로 표시합니다.</p>
    </section>
  );
}

function RuleList({ rules }: { rules: AcademicRuleItem[] }) {
  return (
    <>
      <RuleAnchorSummary rules={rules} />
      <div className="accordion-list">
        {rules.map((rule) => (
          <details id={rule.anchorId || `rule-${rule.id}`} key={rule.id} open={Boolean(rule.searchMatched)}>
            <summary>{rule.question}</summary>
            <p>{rule.answer}</p>
            <div className="action-row">
              {rule.searchMatched ? <StatusPill tone="yellow">검색 일치</StatusPill> : null}
              <span className="muted">최종 수정 {formatDate(rule.updatedAt)}</span>
              <a className="ghost-button" href={rule.detailPath || `/help/academic-rules#rule-${rule.id}`}>규정 링크</a>
              <button className="ghost-button" disabled title="등록된 규정 첨부파일이 없습니다." type="button">파일 다운로드 없음</button>
            </div>
          </details>
        ))}
      </div>
    </>
  );
}

function formatDate(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(date);
}

export default AcademicRulesPage;
