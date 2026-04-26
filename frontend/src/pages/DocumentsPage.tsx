import { useEffect, useState } from 'react';
import { cancelDocumentSubmission, getDocumentRequest, getDocumentRequests, submitDocument } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { DocumentRequestItem, LoadState } from '../types';

const statusLabels = {
  approved: { label: '승인', tone: 'green' },
  canceled: { label: '취소', tone: 'gray' },
  not_submitted: { label: '미제출', tone: 'red' },
  rejected: { label: '반려', tone: 'red' },
  submitted: { label: '제출', tone: 'blue' },
} as const;

interface DocumentsPageProps {
  requestId?: number;
}

function DocumentsPage({ requestId }: DocumentsPageProps) {
  const [items, setItems] = useState<DocumentRequestItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [mutationMessage, setMutationMessage] = useState('');
  const [pendingDocumentAction, setPendingDocumentAction] = useState<string | null>(null);

  const load = () => {
    setLoadState('loading');
    const request = requestId
      ? getDocumentRequest(requestId).then((item) => ({ items: [item] }))
      : getDocumentRequests({ size: 50 });

    request
      .then((response) => {
        setItems(response.items);
        setLoadState(response.items.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
  };

  useEffect(load, [requestId]);

  const handleSubmit = async (requestId: number, file?: File) => {
    if (!file) {
      setMutationMessage('제출할 파일을 선택해 주세요.');
      return;
    }
    if (pendingDocumentAction !== null) return;
    setPendingDocumentAction(`submit:${requestId}`);
    setMutationMessage('서류를 제출하는 중입니다.');
    try {
      const result = await submitDocument(requestId, await fileToDocumentDraft(file));
      setItems((current) => current.map((item) => (item.id === requestId ? result.item : item)));
      setMutationMessage('서류 제출이 완료되었습니다.');
    } catch (error) {
      setMutationMessage(getErrorMessage(error));
    } finally {
      setPendingDocumentAction(null);
    }
  };

  const handleCancel = async (requestId: number, submissionId?: number) => {
    if (!submissionId || pendingDocumentAction !== null) return;
    setPendingDocumentAction(`cancel:${requestId}`);
    setMutationMessage('제출을 취소하는 중입니다.');
    try {
      await cancelDocumentSubmission(requestId, submissionId);
      load();
      setMutationMessage('서류 제출이 취소되었습니다.');
    } catch (error) {
      setMutationMessage(getErrorMessage(error));
    } finally {
      setPendingDocumentAction(null);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="서류제출" description="제출해야 할 교육생 서류를 확인하고 파일 제출 상태를 관리합니다." />
      {mutationMessage ? <p className="helper-text" role="status">{mutationMessage}</p> : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="서류 제출 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="제출할 서류가 없습니다." message="현재 활성화된 서류 제출 요청이 없습니다." /> : null}
      {loadState === 'loaded' && requestId ? <DocumentDetailPanel item={items[0]} pendingAction={pendingDocumentAction} onCancel={handleCancel} onSubmit={handleSubmit} /> : null}
      {loadState === 'loaded' && !requestId ? <DocumentList items={items} pendingAction={pendingDocumentAction} onCancel={handleCancel} onSubmit={handleSubmit} /> : null}
    </section>
  );
}

interface DocumentListProps {
  items: DocumentRequestItem[];
  pendingAction: string | null;
  onCancel: (requestId: number, submissionId?: number) => void;
  onSubmit: (requestId: number, file?: File) => void;
}

function DocumentList({ items, pendingAction, onCancel, onSubmit }: DocumentListProps) {
  const [selectedFiles, setSelectedFiles] = useState<Record<number, File | undefined>>({});

  return (
    <div className="card-list" aria-label="서류 제출 목록">
      {items.map((item) => {
        const status = statusLabels[item.status];
        const attachment = item.attachments[0];
        const canCancel = item.status === 'submitted' || item.status === 'rejected';
        const submitPending = pendingAction === `submit:${item.id}`;
        const cancelPending = pendingAction === `cancel:${item.id}`;
        const actionPending = pendingAction !== null;
        return (
          <article className="list-card" key={item.id}>
            <div>
              <p className="eyebrow">{item.category} · {item.required ? '필수' : '선택'}</p>
              <h2>{item.title}</h2>
              <p>{item.description || '서류 안내가 없습니다.'}</p>
              <dl className="detail-grid">
                <div>
                  <dt>마감</dt>
                  <dd>{formatDate(item.dueAt)}</dd>
                </div>
                <div>
                  <dt>허용 확장자</dt>
                  <dd>{item.allowedExtensions}</dd>
                </div>
                <div>
                  <dt>최대 크기</dt>
                  <dd>{formatBytes(item.maxFileSizeBytes)}</dd>
                </div>
                <div>
                  <dt>최근 제출</dt>
                  <dd>{attachment ? `${attachment.filename} (${formatBytes(attachment.fileSize)})` : '-'}</dd>
                </div>
              </dl>
              {item.reviewComment ? <p className="helper-text">검토 의견: {item.reviewComment}</p> : null}
            </div>
            <StatusPill tone={status.tone}>{status.label}</StatusPill>
            <div className="action-row">
              <a className="ghost-button" href={`/mycampus/documents/${item.id}`}>상세 이력</a>
              <input
                aria-label={`${item.title} 파일 선택`}
                onChange={(event) => setSelectedFiles((current) => ({ ...current, [item.id]: event.target.files?.[0] }))}
                type="file"
              />
              <button className="primary-action" disabled={actionPending} onClick={() => onSubmit(item.id, selectedFiles[item.id])} type="button">
                {submitPending ? '제출 중' : '제출'}
              </button>
              {canCancel ? (
                <button className="ghost-button" disabled={actionPending} onClick={() => onCancel(item.id, attachment?.submissionId)} type="button">
                  {cancelPending ? '취소 중' : '제출 취소'}
                </button>
              ) : null}
            </div>
          </article>
        );
      })}
    </div>
  );
}

interface DocumentDetailPanelProps {
  item: DocumentRequestItem;
  pendingAction: string | null;
  onCancel: (requestId: number, submissionId?: number) => void;
  onSubmit: (requestId: number, file?: File) => void;
}

function DocumentDetailPanel({ item, pendingAction, onCancel, onSubmit }: DocumentDetailPanelProps) {
  const [selectedFile, setSelectedFile] = useState<File>();
  const status = statusLabels[item.status];
  const canCancel = item.status === 'submitted' || item.status === 'rejected';
  const latestSubmissionId = item.attachments[0]?.submissionId;
  const submitPending = pendingAction === `submit:${item.id}`;
  const cancelPending = pendingAction === `cancel:${item.id}`;
  const actionPending = pendingAction !== null;

  return (
    <div className="document-detail-grid">
      <article className="list-card">
        <div>
          <a className="helper-link" href="/mycampus/documents">← 서류 목록으로</a>
          <p className="eyebrow">{item.category} · {item.required ? '필수' : '선택'}</p>
          <h2>{item.title}</h2>
          <p>{item.description || '서류 안내가 없습니다.'}</p>
          <dl className="detail-grid">
            <div>
              <dt>제출 상태</dt>
              <dd>{status.label}</dd>
            </div>
            <div>
              <dt>기간 상태</dt>
              <dd>{deadlineLabel(item)}</dd>
            </div>
            <div>
              <dt>마감</dt>
              <dd>{formatDate(item.dueAt)}</dd>
            </div>
            <div>
              <dt>제출 일시</dt>
              <dd>{formatDate(item.submittedAt)}</dd>
            </div>
            <div>
              <dt>검토 일시</dt>
              <dd>{formatDate(item.reviewedAt)}</dd>
            </div>
            <div>
              <dt>허용/크기</dt>
              <dd>{item.allowedExtensions} · {formatBytes(item.maxFileSizeBytes)}</dd>
            </div>
          </dl>
        </div>
        <StatusPill tone={status.tone}>{status.label}</StatusPill>
        {item.reviewComment ? (
          <div className="callout-card danger">
            <strong>보완 요청/검토 의견</strong>
            <p>{item.reviewComment}</p>
          </div>
        ) : (
          <div className="callout-card">
            <strong>검토 의견</strong>
            <p>아직 등록된 검토 의견이 없습니다.</p>
          </div>
        )}
        <div className="action-row">
          <input aria-label={`${item.title} 상세 파일 선택`} onChange={(event) => setSelectedFile(event.target.files?.[0])} type="file" />
          <button className="primary-action" disabled={actionPending} onClick={() => onSubmit(item.id, selectedFile)} type="button">
            {submitPending ? '제출 중' : '다시 제출'}
          </button>
          {canCancel ? (
            <button className="ghost-button" disabled={actionPending} onClick={() => onCancel(item.id, latestSubmissionId)} type="button">
              {cancelPending ? '취소 중' : '제출 취소'}
            </button>
          ) : null}
        </div>
      </article>
      <DocumentTimeline item={item} />
      <DocumentFileHistory item={item} />
    </div>
  );
}

function DocumentTimeline({ item }: { item: DocumentRequestItem }) {
  const events = [
    { label: '요청 공개', value: formatDate(item.startsAt), active: true },
    { label: '제출 완료', value: formatDate(item.submittedAt), active: Boolean(item.submittedAt) },
    { label: '검토 완료', value: formatDate(item.reviewedAt), active: Boolean(item.reviewedAt) || item.status === 'approved' || item.status === 'rejected' },
    { label: '마감', value: formatDate(item.dueAt), active: item.status !== 'not_submitted' },
  ];

  return (
    <article className="list-card">
      <p className="eyebrow">SUBMISSION TIMELINE</p>
      <h2>제출 상태 이력</h2>
      <ol className="timeline-list">
        {events.map((event) => (
          <li className={event.active ? 'active' : ''} key={event.label}>
            <strong>{event.label}</strong>
            <span>{event.value}</span>
          </li>
        ))}
      </ol>
    </article>
  );
}

function DocumentFileHistory({ item }: { item: DocumentRequestItem }) {
  return (
    <article className="list-card">
      <p className="eyebrow">FILE HISTORY</p>
      <h2>제출 파일 이력</h2>
      {item.attachments.length ? (
        <ul className="file-history-list">
          {item.attachments.map((attachment) => (
            <li key={`${attachment.submissionId}-${attachment.id}`}>
              <div>
                <strong>{attachment.filename}</strong>
                <span>제출 #{attachment.submissionId} · {formatDate(attachment.createdAt)}</span>
              </div>
              <span>{formatBytes(attachment.fileSize)}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="helper-text">아직 제출된 파일이 없습니다. 허용 확장자와 용량을 확인한 뒤 업로드해 주세요.</p>
      )}
    </article>
  );
}

function fileToDocumentDraft(file: File): Promise<{ filename: string; mimeType?: string; contentBase64: string }> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const result = String(reader.result || '');
      resolve({
        filename: file.name,
        mimeType: file.type || 'application/octet-stream',
        contentBase64: result.includes(',') ? result.split(',')[1] : result,
      });
    };
    reader.onerror = () => reject(reader.error || new Error('파일을 읽지 못했습니다.'));
    reader.readAsDataURL(file);
  });
}

function formatDate(value?: string | null): string {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

function formatBytes(value: number): string {
  if (!value) return '-';
  if (value >= 1024 * 1024) return `${(value / 1024 / 1024).toFixed(1)}MB`;
  return `${Math.ceil(value / 1024)}KB`;
}

function deadlineLabel(item: DocumentRequestItem): string {
  if (!item.dueAt) return '상시 제출';
  const due = new Date(item.dueAt).getTime();
  if (Number.isNaN(due)) return '마감일 확인 필요';
  if (due < Date.now() && item.status === 'not_submitted') return '마감 지남';
  if (due < Date.now()) return '마감 후 검토';
  return '제출 가능';
}

export default DocumentsPage;
