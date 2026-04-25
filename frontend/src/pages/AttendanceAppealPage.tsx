import { FormEvent, useEffect, useMemo, useState } from 'react';
import { getAttendanceRecords, submitAttendanceAppeal } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { AttendanceRecord, LoadState } from '../types';

const statusLabel: Record<AttendanceRecord['status'], string> = {
  absent: '결석',
  early_leave: '조퇴',
  excused: '공결',
  late: '지각',
  present: '출석',
};

function AttendanceAppealPage() {
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [attendanceRecordId, setAttendanceRecordId] = useState('');
  const [recordsState, setRecordsState] = useState<LoadState>('loading');
  const [type, setType] = useState('status_change');
  const [requestedStatus, setRequestedStatus] = useState('present');
  const [reason, setReason] = useState('QR 인식 오류');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('소명할 출결 기록, 요청 상태, 사유를 입력해 주세요.');

  useEffect(() => {
    let ignore = false;
    const params = new URLSearchParams(window.location.search);
    const requestedRecordId = params.get('recordId');

    getAttendanceRecords()
      .then((response) => {
        if (ignore) return;
        const appealableRecords = response.items.filter((record) => record.appealAvailable);
        setRecords(appealableRecords);
        setRecordsState(appealableRecords.length ? 'loaded' : 'empty');
        setAttendanceRecordId(selectInitialRecordId(appealableRecords, requestedRecordId));
      })
      .catch((error) => {
        if (ignore) return;
        setMessage(getErrorMessage(error));
        setResult('error');
        setRecordsState('error');
      });

    return () => {
      ignore = true;
    };
  }, []);

  const selectedRecord = useMemo(
    () => records.find((record) => String(record.id) === attendanceRecordId),
    [attendanceRecordId, records],
  );

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedRecord) {
      setResult('error');
      setMessage('소명할 출결 기록을 먼저 선택해 주세요.');
      return;
    }

    setSubmitting(true);
    setResult('idle');

    try {
      const response = await submitAttendanceAppeal({
        attendanceRecordId: selectedRecord.id,
        type,
        reason,
        requestedStatus,
      });
      setResult('success');
      setMessage(`출결 소명이 접수되었습니다. 접수 번호: ${response.id}`);
      setRecords((current) => current.filter((record) => record.id !== selectedRecord.id));
      setAttendanceRecordId('');
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
        {recordsState === 'loading' ? <LoadingRows /> : null}
        {recordsState === 'error' ? <DataState title="출결 기록을 불러오지 못했습니다." message={message} /> : null}
        {recordsState === 'empty' ? <DataState title="소명 가능한 출결 기록이 없습니다." message="이미 접수된 소명은 처리 결과를 기다려 주세요." /> : null}
        {recordsState === 'loaded' ? (
          <form className="stack-form" onSubmit={submit}>
            <label htmlFor="appeal-record">소명할 출결 기록</label>
            <select id="appeal-record" onChange={(event) => setAttendanceRecordId(event.target.value)} required value={attendanceRecordId}>
              <option disabled value="">출결 기록 선택</option>
              {records.map((record) => (
                <option key={record.id} value={record.id}>
                  {record.date} · {statusLabel[record.status]} · 입실 {record.checkIn || '-'} / 퇴실 {record.checkOut || '-'}
                </option>
              ))}
            </select>
            <label htmlFor="appeal-type">소명 유형</label>
            <select id="appeal-type" onChange={(event) => setType(event.target.value)} required value={type}>
              <option value="status_change">출결 상태 정정</option>
              <option value="check_in">입실 기록 정정</option>
              <option value="check_out">퇴실 기록 정정</option>
              <option value="other">기타</option>
            </select>
            <label htmlFor="appeal-requested-status">요청 상태</label>
            <select id="appeal-requested-status" onChange={(event) => setRequestedStatus(event.target.value)} required value={requestedStatus}>
              <option value="present">출석</option>
              <option value="late">지각</option>
              <option value="absent">결석</option>
              <option value="early_leave">조퇴</option>
              <option value="excused">공결</option>
            </select>
            <label htmlFor="appeal-reason">사유</label>
            <textarea id="appeal-reason" onChange={(event) => setReason(event.target.value)} required rows={8} value={reason} />
            <button className="primary-action" disabled={submitting || !selectedRecord} type="submit">
              {submitting ? '신청 중' : '신청'}
            </button>
          </form>
        ) : null}
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

function selectInitialRecordId(records: AttendanceRecord[], requestedRecordId: string | null) {
  if (requestedRecordId && records.some((record) => String(record.id) === requestedRecordId)) {
    return requestedRecordId;
  }
  return records[0] ? String(records[0].id) : '';
}

export default AttendanceAppealPage;
