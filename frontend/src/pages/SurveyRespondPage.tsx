import { FormEvent, useEffect, useState } from 'react';
import { getSurvey, getSurveyResponse, respondSurvey } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, SurveyAnswerDraft, SurveyItem, SurveySavedResponse } from '../types';

function SurveyRespondPage({ surveyId }: { surveyId: number }) {
  const [survey, setSurvey] = useState<SurveyItem>();
  const [answers, setAnswers] = useState<SurveyAnswerDraft[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('각 문항에 대한 응답을 입력해 주세요.');
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');

  useEffect(() => {
    let ignore = false;
    Promise.all([getSurvey(surveyId), getSurveyResponse(surveyId)])
      .then(([response, savedResponse]) => {
        if (ignore) return;
        setSurvey(response);
        setAnswers(response ? answersFromSurvey(response, savedResponse) : []);
        setMessage(savedResponse ? '저장된 응답을 불러왔습니다. 수정 후 다시 제출할 수 있습니다.' : '각 문항에 대한 응답을 입력해 주세요.');
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
      const savedResponse = survey ? await getSurveyResponse(surveyId) : undefined;
      if (survey && savedResponse) {
        setAnswers(answersFromSurvey(survey, savedResponse));
      }
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
    setAnswers((current) => current.map((answer, answerIndex) => (
      answerIndex === index ? { ...answer, answerText: value, optionIds: undefined } : answer
    )));
  };

  const updateSingleChoice = (index: number, optionId: number) => {
    setAnswers((current) => current.map((answer, answerIndex) => (
      answerIndex === index ? { ...answer, answerText: '', optionIds: [optionId] } : answer
    )));
  };

  const toggleMultiChoice = (index: number, optionId: number) => {
    setAnswers((current) => current.map((answer, answerIndex) => {
      if (answerIndex !== index) return answer;
      const currentOptions = answer.optionIds || [];
      const optionIds = currentOptions.includes(optionId)
        ? currentOptions.filter((id) => id !== optionId)
        : [...currentOptions, optionId];
      return { ...answer, answerText: '', optionIds };
    }));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="SURVEY" title="설문 응답" description="설문 문항에 응답하고 제출합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="설문을 불러오지 못했습니다." message={message} /> : null}
      {loadState === 'empty' ? <DataState title="설문을 찾을 수 없습니다." /> : null}
      {survey ? (
        <SurveyForm
          answers={answers}
          message={message}
          result={result}
          submit={submit}
          submitting={submitting}
          survey={survey}
          toggleMultiChoice={toggleMultiChoice}
          updateAnswer={updateAnswer}
          updateSingleChoice={updateSingleChoice}
        />
      ) : null}
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
  toggleMultiChoice: (index: number, optionId: number) => void;
  updateAnswer: (index: number, value: string) => void;
  updateSingleChoice: (index: number, optionId: number) => void;
}) {
  return (
    <section className="panel form-panel">
      <h2>{props.survey.title}</h2>
      <form className="stack-form" onSubmit={props.submit}>
        {props.survey.questions.map((question, index) => (
          <div className="question-field" key={question.id}>
            <span>{question.text}</span>
            <QuestionInput
              answer={props.answers[index]}
              index={index}
              question={question}
              submitting={props.submitting}
              toggleMultiChoice={props.toggleMultiChoice}
              updateAnswer={props.updateAnswer}
              updateSingleChoice={props.updateSingleChoice}
            />
          </div>
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

function QuestionInput(props: {
  answer?: SurveyAnswerDraft;
  index: number;
  question: SurveyItem['questions'][number];
  submitting: boolean;
  toggleMultiChoice: (index: number, optionId: number) => void;
  updateAnswer: (index: number, value: string) => void;
  updateSingleChoice: (index: number, optionId: number) => void;
}) {
  const { answer, index, question, submitting } = props;

  if (question.type === 'single_choice') {
    return (
      <span className="option-stack">
        {(question.options || []).map((option) => (
          <label key={option.id}>
            <input
              checked={answer?.optionIds?.includes(option.id) || false}
              disabled={submitting}
              name={`survey-question-${question.id}`}
              onChange={() => props.updateSingleChoice(index, option.id)}
              required
              type="radio"
            />
            {option.text}
          </label>
        ))}
      </span>
    );
  }

  if (question.type === 'multiple_choice') {
    return (
      <span className="option-stack">
        {(question.options || []).map((option) => (
          <label key={option.id}>
            <input
              checked={answer?.optionIds?.includes(option.id) || false}
              disabled={submitting}
              onChange={() => props.toggleMultiChoice(index, option.id)}
              type="checkbox"
            />
            {option.text}
          </label>
        ))}
      </span>
    );
  }

  return (
    <textarea
      disabled={submitting}
      onChange={(event) => props.updateAnswer(index, event.target.value)}
      required
      rows={question.type === 'short_text' || question.type === 'score' ? 2 : 4}
      value={answer?.answerText || ''}
    />
  );
}

function isChoiceQuestion(type?: string): boolean {
  return type === 'single_choice' || type === 'multiple_choice';
}

function answersFromSurvey(survey: SurveyItem, savedResponse?: SurveySavedResponse): SurveyAnswerDraft[] {
  const savedAnswers = new Map((savedResponse?.answers || []).map((answer) => [answer.questionId, answer]));
  return survey.questions.map((question) => {
    const saved = savedAnswers.get(question.id);
    return {
      questionId: question.id,
      answerText: saved?.answerText || '',
      optionIds: isChoiceQuestion(question.type) ? (saved?.optionIds || []) : undefined,
    };
  });
}

export default SurveyRespondPage;
