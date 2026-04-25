import { useEffect, useState } from 'react';
import { getSupportTickets } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, SupportTicketItem } from '../types';

function QnaListPage() {
  const [tickets, setTickets] = useState<SupportTicketItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getSupportTickets({ page: 1, size: 20 })
      .then((response) => {
        if (ignore) return;
        setTickets(response.items);
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

  return (
    <section className="page">
      <PageHeader eyebrow="HELP DESK" title="1:1 문의" description="내 문의 내역과 답변 상태를 확인합니다." />
      <div className="action-row">
        <a className="primary-action" href="/help/qna/new">문의 등록</a>
      </div>
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="문의 내역을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="문의 내역이 없습니다." message="궁금한 내용을 새 문의로 등록해 주세요." /> : null}
      {tickets.length ? <TicketList tickets={tickets} /> : null}
    </section>
  );
}

function TicketList({ tickets }: { tickets: SupportTicketItem[] }) {
  return (
    <section className="panel">
      <ul className="info-list">
        {tickets.map((ticket) => (
          <li key={ticket.id}>
            <a href={`/help/qna/tickets/${ticket.id}`}>
              <strong>{ticket.title}</strong>
            </a>
            <StatusPill tone={statusTone(ticket.status)}>{supportStatusLabel(ticket.status)}</StatusPill>
            <span>
              메시지 {(ticket.messageCount || 0).toLocaleString('ko-KR')}건 · 최근 {formatDate(ticket.latestMessageAt || ticket.updatedAt)}
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}

function statusTone(status: string): 'blue' | 'green' | 'gray' | 'red' | 'yellow' {
  if (status === 'answered') return 'green';
  if (status === 'waiting_user') return 'yellow';
  if (status === 'closed') return 'gray';
  return 'blue';
}

function supportStatusLabel(status: string): string {
  if (status === 'open') return '접수';
  if (status === 'waiting_user') return '사용자 응답 대기';
  if (status === 'answered') return '답변 완료';
  if (status === 'closed') return '종료';
  return status;
}

function formatDate(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default QnaListPage;
