import type { ReactNode } from 'react';

interface StatusPillProps {
  tone?: 'blue' | 'green' | 'red' | 'yellow' | 'gray';
  children: ReactNode;
}

function StatusPill({ tone = 'gray', children }: StatusPillProps) {
  return <span className={`status-pill ${tone}`}>{children}</span>;
}

export default StatusPill;
