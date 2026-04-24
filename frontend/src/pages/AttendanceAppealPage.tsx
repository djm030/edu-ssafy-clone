import { FormEvent, useState } from 'react';
import { submitAttendanceAppeal } from '../api/app';
import { getErrorMessage } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';

function AttendanceAppealPage() {
  const [date, setDate] = useState('2026-04-24');
  const [reason, setReason] = useState('QR 인식 오류');
  const [detail, setDetail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('소명 사유와 상세 내용을 입력해 주세요.');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');

    try {
      const response = await submitAttendanceAppeal({ date, reason, detail });
      setResult('success');
      setMessage(`출결 소명이 접수되었습니다. 접수 번호: ${response.id}`);
      setDetail('');
    } catch (error) {
      setResult('error');
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="출결 소명 신청" description="누락되거나 정정이 필요한 출결 기록을 소명합니다." />
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submit}>
          <label htmlFor="appeal-date">소명 날짜</label>
          <input id="appeal-date" onChange={(event) => setDate(event.target.value)} required type="date" value={date} />
          <label htmlFor="appeal-reason">사유</label>
          <input id="appeal-reason" onChange={(event) => setReason(event.target.value)} required value={reason} />
          <label htmlFor="appeal-detail">상세 내용</label>
          <textarea id="appeal-detail" onChange={(event) => setDetail(event.target.value)} required rows={8} value={detail} />
          <button className="primary-action" disabled={submitting} type="submit">
            {submitting ? '신청 중' : '신청'}
          </button>
        </form>
        <div className="check-result" aria-live="polite">
          <StatusPill tone={result === 'success' ? 'green' : result === 'error' ? 'red' : 'gray'}>
            {result === 'success' ? '접수완료' : result === 'error' ? '오류' : '대기'}
          </StatusPill>
          <p>{message}</p>
        </div>
      </section>
    </section>
  );
}

export default AttendanceAppealPage;
