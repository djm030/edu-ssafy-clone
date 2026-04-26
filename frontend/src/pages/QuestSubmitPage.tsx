import { FormEvent, useEffect, useState } from 'react';
import { getQuest, submitQuest } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, QuestItem } from '../types';

function QuestSubmitPage({ questId }: { questId: number }) {
  const [quest, setQuest] = useState<QuestItem>();
  const [content, setContent] = useState('');
  const [repositoryUrl, setRepositoryUrl] = useState('');
  const [attachment, setAttachment] = useState<File | null>(null);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('제출 내용 또는 저장소 링크를 입력해 주세요.');
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');

  useEffect(() => {
    let ignore = false;
    getQuest(questId)
      .then((response) => {
        if (ignore) return;
        setQuest(response);
        setLoadState(response ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => {
      ignore = true;
    };
  }, [questId]);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');

    try {
      const attachmentDraft = attachment ? await fileToAttachmentDraft(attachment) : undefined;
      const response = await submitQuest({ questId, repositoryUrl, content, attachment: attachmentDraft });
      setResult('success');
      setMessage(`Quest가 제출되었습니다. 제출번호 ${response.id}, 상태: ${response.status}${attachment ? ', 첨부파일 저장 완료' : ''}`);
    } catch (error) {
      setResult('error');
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="QUEST" title="Quest 제출" description="Quest 산출물과 참고 링크를 제출합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="Quest를 불러오지 못했습니다." message={message} /> : null}
      {loadState === 'empty' ? <DataState title="Quest를 찾을 수 없습니다." /> : null}
      {quest ? <QuestSubmitForm attachment={attachment} content={content} message={message} quest={quest} repositoryUrl={repositoryUrl} result={result} setAttachment={setAttachment} setContent={setContent} setRepositoryUrl={setRepositoryUrl} submit={submit} submitting={submitting} /> : null}
    </section>
  );
}

function QuestSubmitForm(props: {
  attachment: File | null;
  content: string;
  message: string;
  quest: QuestItem;
  repositoryUrl: string;
  result: 'idle' | 'success' | 'error';
  setAttachment: (value: File | null) => void;
  setContent: (value: string) => void;
  setRepositoryUrl: (value: string) => void;
  submit: (event: FormEvent<HTMLFormElement>) => void;
  submitting: boolean;
}) {
  return (
    <section className="panel form-panel">
      <h2>{props.quest.title}</h2>
      <form className="stack-form" onSubmit={props.submit}>
        <label htmlFor="quest-repo">저장소 URL</label>
        <input id="quest-repo" onChange={(event) => props.setRepositoryUrl(event.target.value)} placeholder="https://..." value={props.repositoryUrl} />
        <label htmlFor="quest-content">제출 내용</label>
        <textarea id="quest-content" onChange={(event) => props.setContent(event.target.value)} required rows={8} value={props.content} />
        <label htmlFor="quest-attachment">첨부파일</label>
        <input
          id="quest-attachment"
          onChange={(event) => props.setAttachment(event.target.files?.[0] || null)}
          type="file"
        />
        {props.attachment ? <p className="form-message">선택됨: {props.attachment.name}</p> : null}
        <button className="primary-action" disabled={props.submitting} type="submit">{props.submitting ? '제출 중' : '제출'}</button>
      </form>
      <StatusMessage message={props.message} result={props.result} />
    </section>
  );
}

function StatusMessage({ message, result }: { message: string; result: 'idle' | 'success' | 'error' }) {
  return (
    <div className="check-result" aria-live="polite">
      <StatusPill tone={result === 'success' ? 'green' : result === 'error' ? 'red' : 'gray'}>{result === 'success' ? '완료' : result === 'error' ? '오류' : '대기'}</StatusPill>
      <p>{message}</p>
    </div>
  );
}

function fileToAttachmentDraft(file: File): Promise<{ filename: string; mimeType?: string; contentBase64: string }> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('첨부파일을 읽지 못했습니다.'));
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : '';
      const contentBase64 = result.includes(',') ? result.split(',')[1] : result;
      resolve({
        filename: file.name,
        mimeType: file.type || 'application/octet-stream',
        contentBase64,
      });
    };
    reader.readAsDataURL(file);
  });
}

export default QuestSubmitPage;
