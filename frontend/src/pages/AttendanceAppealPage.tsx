import { FormEvent, useState } from 'react';
import { submitAttendanceAppeal } from '../api/app';
import { getErrorMessage } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';

function AttendanceAppealPage() {
  const [type, setType] = useState('status_change');
  const [requestedStatus, setRequestedStatus] = useState('present');
  const [reason, setReason] = useState('QR 인식 오류');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('소명 유형, 요청 상태, 사유를 입력해 주세요.');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');

    try {
      const response = await submitAttendanceAppeal({ type, reason, requestedStatus });
      setResult('success');
      setMessage(`출결 소명이 접수되었습니다. 접수 번호: ${response.id}`);
      setReason('');
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
          <label htmlFor="appeal-type">소명 유형</label>
          <select id="appeal-type" onChange={(event) => setType(event.target.value)} required value={type}>
            <option value="status_change">출결 상태 정정</option>
            <option value="missing_check">입퇴실 기록 누락</option>
          </select>
          <label htmlFor="appeal-requested-status">요청 상태</label>
          <select id="appeal-requested-status" onChange={(event) => setRequestedStatus(event.target.value)} required value={requestedStatus}>
            <option value="present">출석</option>
            <option value="late">지각</option>
            <option value="absent">결석</option>
          </select>
          <label htmlFor="appeal-reason">사유</label>
          <textarea id="appeal-reason" onChange={(event) => setReason(event.target.value)} required rows={8} value={reason} />
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
