import { FormEvent, useEffect, useState } from 'react';
import { getSurvey, respondSurvey } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, SurveyAnswerDraft, SurveyItem } from '../types';

function SurveyRespondPage({ surveyId }: { surveyId: number }) {
  const [survey, setSurvey] = useState<SurveyItem>();
  const [answers, setAnswers] = useState<SurveyAnswerDraft[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('각 문항에 대한 응답을 입력해 주세요.');
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');

  useEffect(() => {
    let ignore = false;
    getSurvey(surveyId)
      .then((response) => {
        if (ignore) return;
        setSurvey(response);
        setAnswers((response?.questions || []).map((question) => ({ questionId: question.id, answerText: '' })));
        setLoadState(response ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [surveyId]);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');

    try {
      const response = await respondSurvey({ surveyId, answers });
      setResult('success');
      setMessage(`설문 응답이 저장되었습니다. 응답 수: ${response.answerCount}`);
    } catch (error) {
      setResult('error');
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  const updateAnswer = (index: number, value: string) => {
    setAnswers((current) => current.map((answer, answerIndex) => (answerIndex === index ? { ...answer, answerText: value } : answer)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="SURVEY" title="설문 응답" description="설문 문항에 응답하고 제출합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="설문을 불러오지 못했습니다." message={message} /> : null}
      {loadState === 'empty' ? <DataState title="설문을 찾을 수 없습니다." /> : null}
      {survey ? <SurveyForm answers={answers} message={message} result={result} submit={submit} submitting={submitting} survey={survey} updateAnswer={updateAnswer} /> : null}
    </section>
  );
}

function SurveyForm(props: {
  answers: SurveyAnswerDraft[];
  message: string;
  result: 'idle' | 'success' | 'error';
  submit: (event: FormEvent<HTMLFormElement>) => void;
  submitting: boolean;
  survey: SurveyItem;
  updateAnswer: (index: number, value: string) => void;
}) {
  return (
    <section className="panel form-panel">
      <h2>{props.survey.title}</h2>
      <form className="stack-form" onSubmit={props.submit}>
        {props.survey.questions.map((question, index) => (
          <label className="question-field" key={question.id}>
            <span>{question.text}</span>
            <textarea onChange={(event) => props.updateAnswer(index, event.target.value)} required rows={4} value={props.answers[index]?.answerText || ''} />
          </label>
        ))}
        <button className="primary-action" disabled={props.submitting} type="submit">{props.submitting ? '제출 중' : '제출'}</button>
      </form>
      <div className="check-result" aria-live="polite">
        <StatusPill tone={props.result === 'success' ? 'green' : props.result === 'error' ? 'red' : 'gray'}>{props.result === 'success' ? '완료' : props.result === 'error' ? '오류' : '대기'}</StatusPill>
        <p>{props.message}</p>
      </div>
    </section>
  );
}

export default SurveyRespondPage;
