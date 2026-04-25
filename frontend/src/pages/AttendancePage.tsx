import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { cancelAttendanceAppeal, getAttendanceAppeals, getAttendanceRecords } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { AttendanceAppeal, AttendanceRecord, AttendanceRecordFilters, LoadState } from '../types';

const statusLabel: Record<AttendanceRecord['status'], string> = {
  absent: '결석',
  early_leave: '조퇴',
  excused: '공결',
  late: '지각',
  present: '출석',
};

function AttendancePage() {
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [appeals, setAppeals] = useState<AttendanceAppeal[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [appealMessage, setAppealMessage] = useState('');
  const [cancelingAppealId, setCancelingAppealId] = useState<number>();
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [status, setStatus] = useState<AttendanceRecordFilters['status']>('');
  const [appliedFilters, setAppliedFilters] = useState<AttendanceRecordFilters>({ dateFrom: '', dateTo: '', status: '' });

  const loadAttendance = useCallback((filters = appliedFilters) => {
    let ignore = false;
    setLoadState('loading');
    setErrorMessage('');
    Promise.all([getAttendanceRecords(filters), getAttendanceAppeals()])
      .then(([recordsResponse, appealsResponse]) => {
        if (ignore) return;
        setRecords(recordsResponse.items);
        setAppeals(appealsResponse.items);
        setLoadState(recordsResponse.items.length || appealsResponse.items.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [appliedFilters]);

  useEffect(() => loadAttendance(), [loadAttendance]);

  const applyFilters = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setAppliedFilters({ dateFrom, dateTo, status });
  };

  const resetFilters = () => {
    setDateFrom('');
    setDateTo('');
    setStatus('');
    setAppliedFilters({ dateFrom: '', dateTo: '', status: '' });
  };

  const handleCancelAppeal = async (appealId: number) => {
    setCancelingAppealId(appealId);
    setAppealMessage('');
    try {
      const canceled = await cancelAttendanceAppeal(appealId);
      setAppeals((current) => current.map((appeal) => (appeal.id === appealId ? canceled : appeal)));
      setRecords((current) => current.map((record) => (
        record.appealId === appealId ? { ...record, appealAvailable: true, appealStatus: canceled.status } : record
      )));
      setAppealMessage('이의신청이 취소되었습니다.');
    } catch (error) {
      setAppealMessage(getErrorMessage(error));
    } finally {
      setCancelingAppealId(undefined);
    }
  };

  const counts = useMemo(
    () => ({
      absent: records.filter((record) => record.status === 'absent').length,
      appeal: records.filter((record) => record.appealAvailable).length,
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
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={applyFilters}>
          <h2>출석 이력 필터</h2>
          <label htmlFor="attendance-date-from">시작일</label>
          <input id="attendance-date-from" onChange={(event) => setDateFrom(event.target.value)} type="date" value={dateFrom} />
          <label htmlFor="attendance-date-to">종료일</label>
          <input id="attendance-date-to" onChange={(event) => setDateTo(event.target.value)} type="date" value={dateTo} />
          <label htmlFor="attendance-status">출결 상태</label>
          <select id="attendance-status" onChange={(event) => setStatus(event.target.value as AttendanceRecordFilters['status'])} value={status}>
            <option value="">전체</option>
            {Object.entries(statusLabel).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
          <div className="button-row">
            <button className="primary-action" type="submit">조회</button>
            <button className="ghost-button" type="button" onClick={resetFilters}>초기화</button>
          </div>
        </form>
      </section>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="출석현황을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="출석 기록이 없습니다." /> : null}
      {loadState === 'loaded' ? (
        <>
          <AttendanceTable records={records} />
          <AttendanceAppealsPanel
            appeals={appeals}
            cancelingAppealId={cancelingAppealId}
            message={appealMessage}
            onCancel={handleCancelAppeal}
          />
        </>
      ) : null}
    </section>
  );
}

function AttendanceAppealsPanel({
  appeals,
  cancelingAppealId,
  message,
  onCancel,
}: {
  appeals: AttendanceAppeal[];
  cancelingAppealId?: number;
  message: string;
  onCancel: (appealId: number) => Promise<void>;
}) {
  return (
    <section className="panel">
      <div className="section-heading">
        <div>
          <p>소명 내역</p>
          <h2>제출한 출석 이의신청</h2>
        </div>
        <span>{appeals.length}건</span>
      </div>
      {message ? <div className="inline-alert">{message}</div> : null}
      {appeals.length === 0 ? (
        <DataState title="제출한 소명 내역이 없습니다." message="출석 기록의 신청 버튼으로 이의신청을 제출할 수 있습니다." />
      ) : (
        <div className="simple-table" role="table" aria-label="출석 이의신청 내역">
          <div className="simple-row table-head" role="row">
            <span role="columnheader">접수번호</span>
            <span role="columnheader">대상 기록</span>
            <span role="columnheader">요청 상태</span>
            <span role="columnheader">사유</span>
            <span role="columnheader">상태</span>
            <span role="columnheader">처리 결과</span>
            <span role="columnheader">관리</span>
          </div>
          {appeals.map((appeal) => (
            <div className="simple-row" key={appeal.id} role="row">
              <span role="cell">#{appeal.id}</span>
              <span role="cell">{appeal.recordDate || `기록 #${appeal.attendanceRecordId}`}</span>
              <span role="cell">{attendanceStatusLabel(appeal.requestedStatus)}</span>
              <span role="cell">{appeal.reason || '-'}</span>
              <span role="cell">
                <StatusPill tone={appealStatusTone(appeal.status)}>{appealStatusLabel(appeal.status)}</StatusPill>
              </span>
              <span role="cell">{appealResolutionSummary(appeal)}</span>
              <span role="cell">
                {appeal.status === 'requested' ? (
                  <button
                    className="ghost-button"
                    disabled={cancelingAppealId === appeal.id}
                    type="button"
                    onClick={() => {
                      void onCancel(appeal.id);
                    }}
                  >
                    {cancelingAppealId === appeal.id ? '취소 중...' : '신청 취소'}
                  </button>
                ) : (
                  '-'
                )}
              </span>
            </div>
          ))}
        </div>
      )}
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
        <span role="columnheader">소명</span>
      </div>
      {records.map((record) => (
        <div className="simple-row" key={record.id} role="row">
          <span role="cell">{record.date}</span>
          <span role="cell">
            <StatusPill tone={statusTone(record.status)}>
              {statusLabel[record.status]}
            </StatusPill>
          </span>
          <span role="cell">{record.checkIn || '-'}</span>
          <span role="cell">{record.checkOut || '-'}</span>
          <span role="cell">{record.note || '-'}</span>
          <span role="cell">
            {record.appealAvailable ? (
              <a href={`/mycampus/attendance/appeals/new?recordId=${record.id}`}>신청</a>
            ) : record.appealStatus ? (
              <StatusPill tone="gray">{appealStatusLabel(record.appealStatus)}</StatusPill>
            ) : (
              '-'
            )}
          </span>
        </div>
      ))}
    </div>
  );
}

function statusTone(status: AttendanceRecord['status']) {
  if (status === 'present' || status === 'excused') return 'green';
  if (status === 'late' || status === 'early_leave') return 'yellow';
  return 'red';
}

function appealStatusLabel(status: string) {
  if (status === 'requested') return '접수됨';
  if (status === 'approved') return '승인됨';
  if (status === 'rejected') return '반려됨';
  if (status === 'canceled') return '취소됨';
  return status;
}

function appealStatusTone(status: string) {
  if (status === 'approved') return 'green';
  if (status === 'rejected') return 'red';
  if (status === 'canceled') return 'gray';
  return 'blue';
}

function attendanceStatusLabel(status?: string | null) {
  return status ? (statusLabel[status as AttendanceRecord['status']] ?? status) : '-';
}

function appealResolutionSummary(appeal: AttendanceAppeal) {
  if (appeal.status === 'requested') return '처리 대기';
  const resolvedStatus = attendanceStatusLabel(appeal.resolvedStatus);
  const by = appeal.resolvedByName ? ` · ${appeal.resolvedByName}` : '';
  const at = appeal.resolvedAt ? ` · ${appeal.resolvedAt.slice(0, 10)}` : '';
  const comment = appeal.resolutionComment ? ` · ${appeal.resolutionComment}` : '';
  return `${resolvedStatus}${by}${at}${comment}`;
}

export default AttendancePage;
