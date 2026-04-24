import { useEffect, useMemo, useState } from 'react';
import { getAttendanceRecords } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { AttendanceRecord, LoadState } from '../types';

const statusLabel = {
  absent: '결석',
  appeal: '소명 가능',
  late: '지각',
  present: '출석',
};

function AttendancePage() {
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getAttendanceRecords()
      .then((response) => {
        if (ignore) return;
        setRecords(response.items);
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

  const counts = useMemo(
    () => ({
      absent: records.filter((record) => record.status === 'absent').length,
      appeal: records.filter((record) => record.status === 'appeal').length,
      late: records.filter((record) => record.status === 'late').length,
      present: records.filter((record) => record.status === 'present').length,
    }),
    [records],
  );

  return (
    <section className="page">
      <PageHeader
        action={<a className="primary-action" href="/mycampus/attendance/appeals/new">소명 신청</a>}
        eyebrow="MY CAMPUS"
        title="출석현황"
        description="일자별 출결 상태와 소명 가능 여부를 확인합니다."
      />
      <div className="summary-grid">
        <Summary title="출석" value={counts.present} />
        <Summary title="지각" value={counts.late} />
        <Summary title="결석" value={counts.absent} />
        <Summary title="소명 가능" value={counts.appeal} />
      </div>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="출석현황을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="출석 기록이 없습니다." /> : null}
      {loadState === 'loaded' ? <AttendanceTable records={records} /> : null}
    </section>
  );
}

function Summary({ title, value }: { title: string; value: number }) {
  return (
    <section className="stat-card">
      <span>{title}</span>
      <strong>{value}일</strong>
    </section>
  );
}

function AttendanceTable({ records }: { records: AttendanceRecord[] }) {
  return (
    <div className="simple-table" role="table" aria-label="출석 기록">
      <div className="simple-row table-head" role="row">
        <span role="columnheader">날짜</span>
        <span role="columnheader">상태</span>
        <span role="columnheader">입실</span>
        <span role="columnheader">퇴실</span>
        <span role="columnheader">비고</span>
      </div>
      {records.map((record) => (
        <div className="simple-row" key={record.id} role="row">
          <span role="cell">{record.date}</span>
          <span role="cell">
            <StatusPill tone={record.status === 'present' ? 'green' : record.status === 'late' ? 'yellow' : 'red'}>
              {statusLabel[record.status]}
            </StatusPill>
          </span>
          <span role="cell">{record.checkIn || '-'}</span>
          <span role="cell">{record.checkOut || '-'}</span>
          <span role="cell">{record.note || '-'}</span>
        </div>
      ))}
    </div>
  );
}

export default AttendancePage;
