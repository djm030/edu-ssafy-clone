import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { createSurvey, getSurveys } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, SurveyCreateDraft, SurveyItem } from '../types';

interface SurveyPageProps {
  canManageSurveys?: boolean;
}

type CreateState = 'idle' | LoadState;

const initialDraft: SurveyCreateDraft = {
  title: '',
  category: 'satisfaction',
  required: true,
  status: 'in_progress',
  questions: [
    {
      type: 'single_choice',
      text: '',
      options: ['매우 만족', '보통', '개선 필요'],
    },
  ],
};

function SurveyPage({ canManageSurveys = false }: SurveyPageProps) {
  const [items, setItems] = useState<SurveyItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [draft, setDraft] = useState<SurveyCreateDraft>(initialDraft);
  const [createState, setCreateState] = useState<CreateState>('idle');
  const [createMessage, setCreateMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getSurveys()
      .then((response) => {
        if (ignore) return;
        setItems(response.items);
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
  }, []);

  const submitSurvey = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setCreateState('loading');
    setCreateMessage('');

    createSurvey({
      ...draft,
      title: draft.title.trim(),
      questions: draft.questions.map((question) => ({
        ...question,
        text: question.text.trim(),
        options: question.options?.map((option) => option.trim()).filter(Boolean),
      })),
    })
      .then((item) => {
        setItems((current) => [item, ...current]);
        setLoadState('loaded');
        setDraft(initialDraft);
        setCreateMessage('설문이 생성되었습니다.');
        setCreateState('loaded');
      })
      .catch((error) => {
        setCreateMessage(getErrorMessage(error));
        setCreateState('error');
      });
  };

  return (
    <section className="page">
      <PageHeader eyebrow="SURVEY" title="설문" description="필수 여부, 기간, 응답 상태를 확인합니다." />
      {canManageSurveys ? (
        <SurveyCreateForm
          draft={draft}
          message={createMessage}
          state={createState}
          onChange={setDraft}
          onSubmit={submitSurvey}
        />
      ) : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="설문 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="응답할 설문이 없습니다." /> : null}
      {loadState === 'loaded' ? <SurveyTable items={items} /> : null}
    </section>
  );
}

interface SurveyCreateFormProps {
  draft: SurveyCreateDraft;
  message: string;
  state: CreateState;
  onChange: (draft: SurveyCreateDraft) => void;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
}

function SurveyCreateForm({ draft, message, state, onChange, onSubmit }: SurveyCreateFormProps) {
  const question = draft.questions[0];
  const optionText = question.options?.join('\n') || '';
  const choiceType = question.type === 'single_choice' || question.type === 'multiple_choice';

  return (
    <form className="content-card form-grid" onSubmit={onSubmit}>
      <div className="form-field">
        <label htmlFor="survey-title">새 설문 제목</label>
        <input
          id="survey-title"
          onChange={(event) => onChange({ ...draft, title: event.target.value })}
          placeholder="예: 4월 과정 만족도 설문"
          required
          value={draft.title}
        />
      </div>
      <div className="form-field">
        <label htmlFor="survey-category">카테고리</label>
        <select id="survey-category" onChange={(event) => onChange({ ...draft, category: event.target.value })} value={draft.category}>
          <option value="satisfaction">만족도</option>
          <option value="course">과정</option>
          <option value="lecture">강의</option>
          <option value="etc">기타</option>
        </select>
      </div>
      <div className="form-field">
        <label htmlFor="survey-question-type">문항 유형</label>
        <select
          id="survey-question-type"
          onChange={(event) => {
            const type = event.target.value;
            onChange({
              ...draft,
              questions: [{
                ...question,
                type,
                options: type === 'single_choice' || type === 'multiple_choice' ? question.options || ['좋음', '보통'] : [],
              }],
            });
          }}
          value={question.type}
        >
          <option value="single_choice">단일 선택</option>
          <option value="multiple_choice">다중 선택</option>
          <option value="short_text">단답형</option>
          <option value="long_text">장문형</option>
        </select>
      </div>
      <div className="form-field full-width">
        <label htmlFor="survey-question">첫 문항</label>
        <textarea
          id="survey-question"
          onChange={(event) => onChange({ ...draft, questions: [{ ...question, text: event.target.value }] })}
          placeholder="문항 내용을 입력하세요."
          required
          value={question.text}
        />
      </div>
      {choiceType ? (
        <div className="form-field full-width">
          <label htmlFor="survey-options">선택지(줄바꿈으로 구분)</label>
          <textarea
            id="survey-options"
            onChange={(event) => onChange({
              ...draft,
              questions: [{ ...question, options: event.target.value.split('\n') }],
            })}
            required
            value={optionText}
          />
        </div>
      ) : null}
      <label className="inline-check">
        <input
          checked={draft.required}
          onChange={(event) => onChange({ ...draft, required: event.target.checked })}
          type="checkbox"
        />
        필수 설문
      </label>
      <button className="primary-action" disabled={state === 'loading'} type="submit">
        {state === 'loading' ? '생성 중...' : '설문 생성'}
      </button>
      {message ? <p className={state === 'error' ? 'form-error' : 'form-success'}>{message}</p> : null}
    </form>
  );
}

function SurveyTable({ items }: { items: SurveyItem[] }) {
  return (
    <div className="simple-table" role="table" aria-label="설문 목록">
      <div className="simple-row table-head" role="row">
        <span role="columnheader">제목</span>
        <span role="columnheader">필수</span>
        <span role="columnheader">기간</span>
        <span role="columnheader">응답</span>
        <span role="columnheader">상세</span>
      </div>
      {items.map((item) => (
        <div className="simple-row" key={item.id} role="row">
          <strong role="cell">{item.title}</strong>
          <span role="cell">{item.required ? '필수' : '선택'}</span>
          <span role="cell">
            {item.startsAt} ~ {item.endsAt}
          </span>
          <span role="cell">
            <StatusPill tone={item.answered ? 'green' : 'yellow'}>{item.answered ? '응답완료' : '미응답'}</StatusPill>
          </span>
          <span role="cell">
            <a className="ghost-button" href={`/survey/${item.id}`}>열기</a>
          </span>
        </div>
      ))}
    </div>
  );
}

export default SurveyPage;
