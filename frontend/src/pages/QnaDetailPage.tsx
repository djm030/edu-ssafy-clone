import { FormEvent, useEffect, useState } from 'react';
import {
  createSupportTicketAnswer,
  createSupportTicketMessage,
  createSupportTicketMessageAttachment,
  getSupportTicket,
  supportTicketAttachmentDownloadUrl,
} from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, SupportTicketAttachmentItem, SupportTicketDetail, SupportTicketMessageItem } from '../types';

function QnaDetailPage({ canAnswerSupport = false, ticketId }: { canAnswerSupport?: boolean; ticketId: number }) {
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
      {ticket ? <TicketContent canAnswerSupport={canAnswerSupport} ticket={ticket} onTicketChange={setTicket} /> : null}
    </section>
  );
}

function TicketContent({
  canAnswerSupport,
  onTicketChange,
  ticket,
}: {
  canAnswerSupport: boolean;
  onTicketChange: (ticket: SupportTicketDetail) => void;
  ticket: SupportTicketDetail;
}) {
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState(canAnswerSupport ? '운영 답변을 등록하면 문의 상태가 답변 완료로 변경됩니다.' : '추가 문의를 남기면 스레드에 바로 저장됩니다.');

  useEffect(() => {
    setMessage(canAnswerSupport ? '운영 답변을 등록하면 문의 상태가 답변 완료로 변경됩니다.' : '추가 문의를 남기면 스레드에 바로 저장됩니다.');
  }, [canAnswerSupport, ticket.id]);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const trimmed = content.trim();
    if (!trimmed) return;

    setSubmitting(true);
    setMessage('메시지를 저장하는 중입니다.');
    try {
      const response = canAnswerSupport
        ? await createSupportTicketAnswer(ticket.id, { content: trimmed })
        : await createSupportTicketMessage(ticket.id, { content: trimmed });
      onTicketChange({
        ...ticket,
        ...response.ticket,
        messages: [...ticket.messages, response.item],
      });
      setContent('');
      setMessage(canAnswerSupport ? '운영 답변이 등록되었습니다.' : '메시지가 등록되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  const updateMessage = (updatedMessage: SupportTicketMessageItem) => {
    onTicketChange({
      ...ticket,
      messages: ticket.messages.map((item) => (
        item.id === updatedMessage.id
          ? { ...item, ...updatedMessage, attachments: updatedMessage.attachments ?? item.attachments ?? [] }
          : item
      )),
    });
  };

  return (
    <article className="panel detail-panel">
      <div className="detail-meta">
        <StatusPill tone={statusTone(ticket.status)}>{supportStatusLabel(ticket.status)}</StatusPill>
        <span>문의 #{ticket.id}</span>
      </div>
      <h2>{ticket.title}</h2>
      <SupportTicketStatusPanel ticket={ticket} canAnswerSupport={canAnswerSupport} />
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
      <MessageThread
        canAnswerSupport={canAnswerSupport}
        messages={ticket.messages}
        onMessageChange={updateMessage}
        ticketId={ticket.id}
        ticketStatus={ticket.status}
      />
      {ticket.status === 'closed' ? (
        <p className="form-message">종료된 문의에는 메시지를 추가할 수 없습니다.</p>
      ) : (
        <form className="comment-form" onSubmit={submit}>
          <label className="visually-hidden" htmlFor={`support-message-${ticket.id}`}>추가 문의</label>
          <input
            disabled={submitting}
            id={`support-message-${ticket.id}`}
            onChange={(event) => setContent(event.target.value)}
            placeholder={canAnswerSupport ? '운영 답변을 입력하세요' : '추가 문의 내용을 입력하세요'}
            required
            value={content}
          />
          <button className="ghost-button" disabled={submitting} type="submit">
            {submitting ? '저장 중' : canAnswerSupport ? '답변 등록' : '메시지 등록'}
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

function SupportTicketStatusPanel({ canAnswerSupport, ticket }: { canAnswerSupport: boolean; ticket: SupportTicketDetail }) {
  const attachmentCount = ticket.messages.reduce((total, item) => total + (item.attachments?.length || 0), 0);
  const adminReplyCount = ticket.messages.filter((item) => item.type === 'admin_reply').length;

  return (
    <section className="support-ticket-status-panel" aria-label="1:1 문의 답변 및 첨부 상태">
      <div>
        <StatusPill tone={statusTone(ticket.status)}>{supportStatusLabel(ticket.status)}</StatusPill>
        <strong>{ticket.status === 'closed' ? '종료된 문의' : canAnswerSupport ? '운영 답변 가능' : '추가 문의 가능'}</strong>
      </div>
      <p>{supportTicketPolicyText(ticket.status, canAnswerSupport)}</p>
      <div className="support-ticket-status-grid">
        <span>메시지 {ticket.messages.length || ticket.messageCount || 0}건</span>
        <span>운영 답변 {adminReplyCount}건</span>
        <span>첨부 {attachmentCount}개</span>
      </div>
    </section>
  );
}

function MessageThread({
  canAnswerSupport,
  messages,
  onMessageChange,
  ticketId,
  ticketStatus,
}: {
  canAnswerSupport: boolean;
  messages: SupportTicketMessageItem[];
  onMessageChange: (message: SupportTicketMessageItem) => void;
  ticketId: number;
  ticketStatus: string;
}) {
  if (messages.length === 0) {
    return <p className="muted-text">등록된 메시지가 없습니다.</p>;
  }

  return (
    <section aria-label="문의 메시지 스레드">
      <h3>메시지 스레드</h3>
      <ol className="support-thread-list">
        {messages.map((item) => (
          <li className={item.type === 'admin_reply' ? 'support-thread-item admin' : 'support-thread-item'} key={item.id}>
            <div className="support-thread-heading">
              <strong>{item.senderName || messageTypeLabel(item.type)}</strong>
              <StatusPill tone={item.type === 'admin_reply' ? 'green' : 'blue'}>{messageTypeLabel(item.type)}</StatusPill>
              <small>{formatDate(item.createdAt)}</small>
            </div>
            <p>{item.content}</p>
            <AttachmentList attachments={item.attachments ?? []} messageId={item.id} ticketId={ticketId} />
            {ticketStatus === 'closed' || (!canAnswerSupport && item.type !== 'user_message') ? null : (
              <AttachmentUploader message={item} onUploaded={onMessageChange} ticketId={ticketId} />
            )}
          </li>
        ))}
      </ol>
      {ticketStatus === 'closed' ? <p className="form-message">닫힘 상태에서는 답변, 추가 문의, 첨부 저장이 비활성화됩니다.</p> : null}
    </section>
  );
}

function AttachmentList({
  attachments,
  messageId,
  ticketId,
}: {
  attachments: SupportTicketAttachmentItem[];
  messageId: number;
  ticketId: number;
}) {
  if (attachments.length === 0) {
    return <span className="muted-text">첨부파일 없음</span>;
  }

  return (
    <ul className="info-list" aria-label="첨부파일">
      {attachments.map((attachment) => (
        <li key={attachment.id}>
          <strong>
            <a download href={supportTicketAttachmentDownloadUrl(ticketId, messageId, attachment.id)}>
              {attachment.filename}
            </a>
          </strong>
          <span>{attachment.mimeType || 'application/octet-stream'} · {formatFileSize(attachment.fileSize)}</span>
        </li>
      ))}
    </ul>
  );
}

function AttachmentUploader({
  message,
  onUploaded,
  ticketId,
}: {
  message: SupportTicketMessageItem;
  onUploaded: (message: SupportTicketMessageItem) => void;
  ticketId: number;
}) {
  const [file, setFile] = useState<File>();
  const [uploading, setUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState('');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!file) return;

    setUploading(true);
    setUploadMessage('첨부파일을 저장하는 중입니다.');
    try {
      const contentBase64 = await readFileAsBase64(file);
      const response = await createSupportTicketMessageAttachment(ticketId, message.id, {
        filename: file.name,
        mimeType: file.type || undefined,
        contentBase64,
      });
      onUploaded({
        ...message,
        ...response.message,
        attachments: response.message.attachments ?? [...(message.attachments ?? []), response.item],
      });
      setFile(undefined);
      setUploadMessage('첨부파일이 저장되었습니다.');
    } catch (error) {
      setUploadMessage(getErrorMessage(error));
    } finally {
      setUploading(false);
    }
  };

  return (
    <form className="comment-form" onSubmit={submit}>
      <label className="visually-hidden" htmlFor={`support-attachment-${message.id}`}>첨부파일</label>
      <input
        disabled={uploading}
        id={`support-attachment-${message.id}`}
        onChange={(event) => setFile(event.target.files?.[0])}
        type="file"
      />
      <button className="ghost-button" disabled={!file || uploading} type="submit">
        {uploading ? '첨부 중' : '첨부 저장'}
      </button>
      {uploadMessage ? <span className="form-message" aria-live="polite">{uploadMessage}</span> : null}
    </form>
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

function supportTicketPolicyText(status: string, canAnswerSupport: boolean): string {
  if (status === 'closed') return '문의가 종료되어 새 메시지와 첨부파일을 추가할 수 없습니다.';
  if (status === 'answered') return canAnswerSupport ? '답변 완료 상태이며 필요 시 후속 운영 답변을 남길 수 있습니다.' : '운영 답변을 확인하고 추가 문의가 필요하면 메시지를 남길 수 있습니다.';
  if (status === 'waiting_user') return '사용자 추가 확인이 필요한 상태입니다.';
  return canAnswerSupport ? '접수된 문의에 운영 답변을 등록할 수 있습니다.' : '접수된 문의는 운영 답변을 기다리는 상태입니다.';
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

function formatFileSize(value?: number): string {
  if (!value) return '0 B';
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / (1024 * 1024)).toFixed(1)} MB`;
}

function readFileAsBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('첨부파일을 읽지 못했습니다.'));
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : '';
      resolve(result.includes(',') ? result.split(',')[1] : result);
    };
    reader.readAsDataURL(file);
  });
}

export default QnaDetailPage;
