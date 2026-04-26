import { useEffect, useState } from 'react';
import { cancelDocumentSubmission, getDocumentRequests, submitDocument } from '../api/app';
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

function DocumentsPage() {
  const [items, setItems] = useState<DocumentRequestItem[]>([]);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [mutationMessage, setMutationMessage] = useState('');

  const load = () => {
    setLoadState('loading');
    getDocumentRequests({ size: 50 })
      .then((response) => {
        setItems(response.items);
        setLoadState(response.items.length ? 'loaded' : 'empty');
      })
      .catch((error) => {
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });
  };

  useEffect(load, []);

  const handleSubmit = async (requestId: number, file?: File) => {
    if (!file) {
      setMutationMessage('제출할 파일을 선택해 주세요.');
      return;
    }
    setMutationMessage('서류를 제출하는 중입니다.');
    try {
      const result = await submitDocument(requestId, await fileToDocumentDraft(file));
      setItems((current) => current.map((item) => (item.id === requestId ? result.item : item)));
      setMutationMessage('서류 제출이 완료되었습니다.');
    } catch (error) {
      setMutationMessage(getErrorMessage(error));
    }
  };

  const handleCancel = async (requestId: number, submissionId?: number) => {
    if (!submissionId) return;
    setMutationMessage('제출을 취소하는 중입니다.');
    try {
      await cancelDocumentSubmission(requestId, submissionId);
      load();
      setMutationMessage('서류 제출이 취소되었습니다.');
    } catch (error) {
      setMutationMessage(getErrorMessage(error));
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="서류제출" description="제출해야 할 교육생 서류를 확인하고 파일 제출 상태를 관리합니다." />
      {mutationMessage ? <p className="helper-text" role="status">{mutationMessage}</p> : null}
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="서류 제출 목록을 불러오지 못했습니다." message={errorMessage} /> : null}
      {loadState === 'empty' ? <DataState title="제출할 서류가 없습니다." message="현재 활성화된 서류 제출 요청이 없습니다." /> : null}
      {loadState === 'loaded' ? <DocumentList items={items} onCancel={handleCancel} onSubmit={handleSubmit} /> : null}
    </section>
  );
}

interface DocumentListProps {
  items: DocumentRequestItem[];
  onCancel: (requestId: number, submissionId?: number) => void;
  onSubmit: (requestId: number, file?: File) => void;
}

function DocumentList({ items, onCancel, onSubmit }: DocumentListProps) {
  const [selectedFiles, setSelectedFiles] = useState<Record<number, File | undefined>>({});

  return (
    <div className="card-list" aria-label="서류 제출 목록">
      {items.map((item) => {
        const status = statusLabels[item.status];
        const attachment = item.attachments[0];
        const canCancel = item.status === 'submitted' || item.status === 'rejected';
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
              <input
                aria-label={`${item.title} 파일 선택`}
                onChange={(event) => setSelectedFiles((current) => ({ ...current, [item.id]: event.target.files?.[0] }))}
                type="file"
              />
              <button className="primary-action" onClick={() => onSubmit(item.id, selectedFiles[item.id])} type="button">제출</button>
              {canCancel ? <button className="ghost-button" onClick={() => onCancel(item.id, attachment?.submissionId)} type="button">제출 취소</button> : null}
            </div>
          </article>
        );
      })}
    </div>
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

export default DocumentsPage;
