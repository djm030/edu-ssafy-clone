import type { ReactNode } from 'react';

interface PageHeaderProps {
  eyebrow: string;
  title: string;
  description: string;
  action?: ReactNode;
}

function PageHeader({ eyebrow, title, description, action }: PageHeaderProps) {
  return (
    <div className="page-heading">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {action}
    </div>
  );
}

export default PageHeader;
