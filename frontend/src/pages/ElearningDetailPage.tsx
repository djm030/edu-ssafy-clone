import { useEffect, useState } from 'react';
import { getElearningProgressDetail, resumeElearning } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { ElearningProgressDetail, LoadState } from '../types';

const statusLabels = {
  completed: { label: '완료', tone: 'green' },
  in_progress: { label: '학습중', tone: 'yellow' },
  not_started: { label: '미시작', tone: 'gray' },
} as const;

function ElearningDetailPage({ courseId }: { courseId: number }) {
  const [item, setItem] = useState<ElearningProgressDetail>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [resumeMessage, setResumeMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');
    getElearningProgressDetail(courseId)
      .then((response) => {
        if (ignore) return;
        setItem(response);
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
  }, [courseId]);

  const handleResume = () => {
    setResumeMessage('이어보기 이력을 저장하는 중입니다.');
    resumeElearning(courseId)
      .then((response) => setResumeMessage(`이어보기 준비 완료: ${response.item.resumeUrl || `/mycampus/elearning/${courseId}`}`))
      .catch((error) => setResumeMessage(getErrorMessage(error)));
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="학습중 이러닝 상세" description="차시별 학습 완료 상태와 이어보기 위치를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="이러닝 상세를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="이러닝 진행 정보를 찾을 수 없습니다." message="본인에게 배정된 과정인지 확인해 주세요." /> : null}
      {loadState === 'loaded' && item ? <ElearningDetail item={item} onResume={handleResume} resumeMessage={resumeMessage} /> : null}
    </section>
  );
}

function ElearningDetail({ item, onResume, resumeMessage }: { item: ElearningProgressDetail; onResume: () => void; resumeMessage: string }) {
  const status = statusLabels[item.status];

  return (
    <div className="content-card">
      <div className="detail-header">
        <div>
          <p className="eyebrow">{[item.category, item.provider].filter(Boolean).join(' · ') || 'SSAFY e-Learning'}</p>
          <h2>{item.title}</h2>
          <p>{item.description || '등록된 설명이 없습니다.'}</p>
        </div>
        <StatusPill tone={status.tone}>{status.label}</StatusPill>
      </div>
      <div className="progress-track" aria-label={`${item.title} 진행률 ${item.progressPercent}%`}>
        <span style={{ width: `${item.progressPercent}%` }} />
      </div>
      <p>{item.completedLessons}/{item.totalLessons}차시 완료 · 총 {Math.round(item.totalDurationSeconds / 60).toLocaleString('ko-KR')}분</p>
      <button className="primary-action" onClick={onResume} type="button">이어보기 이력 저장</button>
      {resumeMessage ? <p className="helper-text" role="status">{resumeMessage}</p> : null}
      <div className="simple-table" role="table" aria-label="이러닝 차시 목록">
        <div className="simple-row table-head" role="row">
          <span role="columnheader">차시</span>
          <span role="columnheader">제목</span>
          <span role="columnheader">시간</span>
          <span role="columnheader">상태</span>
        </div>
        {item.lessons.map((lesson) => (
          <div className="simple-row" key={lesson.lessonId} role="row">
            <span role="cell">{lesson.lessonNo}</span>
            <strong role="cell">{lesson.title}</strong>
            <span role="cell">{Math.round(lesson.durationSeconds / 60)}분</span>
            <span role="cell"><StatusPill tone={lesson.completed ? 'green' : 'gray'}>{lesson.completed ? '완료' : '미완료'}</StatusPill></span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default ElearningDetailPage;
