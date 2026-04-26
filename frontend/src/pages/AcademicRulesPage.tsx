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
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getAcademicRules({ categoryId: selectedCategoryId, keyword })
      .then((response) => {
        if (ignore) return;
        setCategories(response.categories);
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
    () => categories.reduce((total, category) => total + category.rules.length, 0),
    [categories],
  );

  const submitSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setKeyword(keywordInput.trim());
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
        </form>
        <div className="action-row" role="list" aria-label="학사규정 카테고리">
          <button className="ghost-button" onClick={() => setSelectedCategoryId(undefined)} type="button">
            전체
          </button>
          {categories.map((category) => (
            <button className="ghost-button" key={category.id} onClick={() => setSelectedCategoryId(category.id)} type="button">
              {category.name} ({category.ruleCount})
            </button>
          ))}
        </div>
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
      {loadState === 'loaded' ? <RuleCategoryList categories={categories} totalRuleCount={totalRuleCount} /> : null}
    </section>
  );
}

function RuleCategoryList({ categories, totalRuleCount }: { categories: AcademicRuleCategory[]; totalRuleCount: number }) {
  return (
    <section className="panel">
      <div className="section-heading">
        <div>
          <h2>규정 {totalRuleCount.toLocaleString('ko-KR')}건</h2>
          <p>각 항목을 열어 세부 기준을 확인하세요.</p>
        </div>
        <a className="ghost-button" href="/help/qna/new">추가 문의</a>
      </div>
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

function RuleList({ rules }: { rules: AcademicRuleItem[] }) {
  return (
    <div className="accordion-list">
      {rules.map((rule) => (
        <details key={rule.id}>
          <summary>{rule.question}</summary>
          <p>{rule.answer}</p>
          <span className="muted">최종 수정 {formatDate(rule.updatedAt)}</span>
        </details>
      ))}
    </div>
  );
}

function formatDate(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(date);
}

export default AcademicRulesPage;
