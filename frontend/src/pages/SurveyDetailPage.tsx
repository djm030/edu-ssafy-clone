import { useEffect, useState } from 'react';
import { getSurvey } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, SurveyItem } from '../types';

function SurveyDetailPage({ surveyId }: { surveyId: number }) {
  const [survey, setSurvey] = useState<SurveyItem>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getSurvey(surveyId)
      .then((response) => {
        if (ignore) return;
        setSurvey(response);
        setLoadState(response ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [surveyId]);

  return (
    <section className="page">
      <PageHeader eyebrow="SURVEY" title="설문 상세" description="설문 기간과 문항을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="설문을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="설문을 찾을 수 없습니다." /> : null}
      {survey ? <SurveyContent survey={survey} /> : null}
    </section>
  );
}

function SurveyContent({ survey }: { survey: SurveyItem }) {
  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone={survey.required ? 'yellow' : 'gray'}>{survey.required ? '필수' : '선택'}</StatusPill>
        <StatusPill tone={survey.answered ? 'green' : 'blue'}>{survey.answered ? '응답완료' : '미응답'}</StatusPill>
      </div>
      <h2>{survey.title}</h2>
      <p>{survey.startsAt} ~ {survey.endsAt}</p>
      <div className="detail-body">{survey.description || '설문 설명이 없습니다.'}</div>
      <ol className="question-list">
        {survey.questions.map((question) => (
          <li key={question.id}>
            <strong>{question.text}</strong>
            <p className="muted-text">{questionTypeLabel(question.type)}</p>
            {question.options?.length ? (
              <ul>
                {question.options.map((option) => (
                  <li key={option.id}>{option.text}</li>
                ))}
              </ul>
            ) : null}
          </li>
        ))}
      </ol>
      <div className="action-row">
        <a className="ghost-button" href="/survey">목록</a>
        <a className="primary-action" href={`/survey/${survey.id}/respond`}>{survey.answered ? '응답 수정' : '응답하기'}</a>
      </div>
    </article>
  );
}

function questionTypeLabel(type?: string): string {
  if (type === 'single_choice') return '단일 선택';
  if (type === 'multiple_choice') return '복수 선택';
  if (type === 'score') return '점수 입력';
  if (type === 'short_text') return '단답형';
  return '장문형';
}

export default SurveyDetailPage;
