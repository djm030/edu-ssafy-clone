import { FormEvent, useEffect, useState } from 'react';
import { createSupportTicketMessage, getSupportTicket } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, SupportTicketDetail, SupportTicketMessageItem } from '../types';

function QnaDetailPage({ ticketId }: { ticketId: number }) {
  const [ticket, setTicket] = useState<SupportTicketDetail>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getSupportTicket(ticketId)
      .then((response) => {
        if (ignore) return;
        setTicket(response);
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
  }, [ticketId]);

  return (
    <section className="page">
      <PageHeader eyebrow="HELP DESK" title="1:1 문의 상세" description="문의 메시지와 답변 스레드를 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="문의 상세를 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="문의 내역을 찾을 수 없습니다." message="목록에서 다시 선택해 주세요." /> : null}
      {ticket ? <TicketContent ticket={ticket} onTicketChange={setTicket} /> : null}
    </section>
  );
}

function TicketContent({
  onTicketChange,
  ticket,
}: {
  onTicketChange: (ticket: SupportTicketDetail) => void;
  ticket: SupportTicketDetail;
}) {
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('추가 문의를 남기면 스레드에 바로 저장됩니다.');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const trimmed = content.trim();
    if (!trimmed) return;

    setSubmitting(true);
    setMessage('메시지를 저장하는 중입니다.');
    try {
      const response = await createSupportTicketMessage(ticket.id, { content: trimmed });
      onTicketChange({
        ...ticket,
        ...response.ticket,
        messages: [...ticket.messages, response.item],
      });
      setContent('');
      setMessage('메시지가 등록되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone={statusTone(ticket.status)}>{supportStatusLabel(ticket.status)}</StatusPill>
        <span>문의 #{ticket.id}</span>
      </div>
      <h2>{ticket.title}</h2>
      <dl className="info-list detail-info">
        <div>
          <dt>등록일</dt>
          <dd>{formatDate(ticket.createdAt)}</dd>
        </div>
        <div>
          <dt>최근 메시지</dt>
          <dd>{formatDate(ticket.latestMessageAt || ticket.updatedAt)}</dd>
        </div>
        <div>
          <dt>메시지</dt>
          <dd>{(ticket.messages.length || ticket.messageCount || 0).toLocaleString('ko-KR')}건</dd>
        </div>
      </dl>
      <MessageThread messages={ticket.messages} />
      {ticket.status === 'closed' ? (
        <p className="form-message">종료된 문의에는 메시지를 추가할 수 없습니다.</p>
      ) : (
        <form className="comment-form" onSubmit={submit}>
          <label className="visually-hidden" htmlFor={`support-message-${ticket.id}`}>추가 문의</label>
          <input
            disabled={submitting}
            id={`support-message-${ticket.id}`}
            onChange={(event) => setContent(event.target.value)}
            placeholder="추가 문의 내용을 입력하세요"
            required
            value={content}
          />
          <button className="ghost-button" disabled={submitting} type="submit">
            {submitting ? '저장 중' : '메시지 등록'}
          </button>
        </form>
      )}
      <p className="form-message" aria-live="polite">{message}</p>
      <div className="action-row">
        <a className="ghost-button" href="/help/qna">목록</a>
      </div>
    </article>
  );
}

function MessageThread({ messages }: { messages: SupportTicketMessageItem[] }) {
  if (messages.length === 0) {
    return <p className="muted-text">등록된 메시지가 없습니다.</p>;
  }

  return (
    <section aria-label="문의 메시지 스레드">
      <h3>메시지 스레드</h3>
      <ul className="info-list">
        {messages.map((item) => (
          <li key={item.id}>
            <strong>{item.senderName || messageTypeLabel(item.type)}</strong>
            <StatusPill tone={item.type === 'admin_reply' ? 'green' : 'blue'}>{messageTypeLabel(item.type)}</StatusPill>
            <span>{item.content}</span>
            <small>{formatDate(item.createdAt)}</small>
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

function messageTypeLabel(type: string): string {
  if (type === 'admin_reply') return '운영 답변';
  if (type === 'internal_note') return '내부 메모';
  return '사용자 문의';
}

function formatDate(value?: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default QnaDetailPage;
