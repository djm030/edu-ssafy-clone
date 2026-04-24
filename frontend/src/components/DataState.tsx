interface DataStateProps {
  title: string;
  message?: string;
  actionLabel?: string;
  onAction?: () => void;
}

function DataState({ title, message, actionLabel, onAction }: DataStateProps) {
  return (
    <div className="state-message">
      <strong>{title}</strong>
      {message ? <p>{message}</p> : null}
      {onAction ? (
        <button onClick={onAction} type="button">
          {actionLabel || '다시 시도'}
        </button>
      ) : null}
    </div>
  );
}

export function LoadingRows() {
  return (
    <div className="skeleton-list" aria-label="목록 로딩 중">
      {Array.from({ length: 5 }).map((_, index) => (
        <div className="skeleton-row" key={index}>
          <span />
          <span />
          <span />
        </div>
      ))}
    </div>
  );
}

export default DataState;
