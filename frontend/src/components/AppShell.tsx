import type { ReactNode } from 'react';
import { navSections } from '../routes';
import type { RoleAccess, UserProfile } from '../types';

interface AppShellProps {
  children: ReactNode;
  currentPath: string;
  user: UserProfile;
  roleAccess?: RoleAccess;
  accessError?: string;
  sessionExpiresAt?: string;
  sessionSecondsRemaining?: number;
  onNavigate: (path: string) => void;
  onLogout: () => void;
}

function formatSessionExpiry(value?: string): string {
  if (!value) return '';
  const expiresAt = new Date(value);
  if (Number.isNaN(expiresAt.getTime())) return '';
  return expiresAt.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
}

function AppShell({
  accessError,
  children,
  currentPath,
  onLogout,
  onNavigate,
  roleAccess,
  sessionExpiresAt,
  sessionSecondsRemaining,
  user,
}: AppShellProps) {
  const sessionExpiryText = formatSessionExpiry(sessionExpiresAt);
  const isDeniedNavItem = (path: string) => Boolean(
    roleAccess?.deniedRoutes.some((route) => path === route || path.startsWith(`${route}/`))
  );
  const visibleSections = navSections
    .map((section) => ({
      ...section,
      items: section.items.filter((item) => !isDeniedNavItem(item.path)),
    }))
    .filter((section) => section.items.length > 0);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <button className="brand" onClick={() => onNavigate('/')} type="button">
          eduSSAFY
        </button>
        <nav aria-label="주요 메뉴">
          {visibleSections.map((section) => (
            <div className="nav-section" key={section.title}>
              <p>{section.title}</p>
              {section.items.map((item) => (
                <button
                  className={item.path === currentPath ? 'nav-link active' : 'nav-link'}
                  key={item.path}
                  onClick={() => onNavigate(item.path)}
                  type="button"
                >
                  {item.label}
                </button>
              ))}
            </div>
          ))}
        </nav>
      </aside>

      <div className="workspace">
        <header className="topbar">
          <div>
            <strong>{user.name}</strong>
            <span>
              {user.campusName} 캠퍼스 · {user.cohortName} · {user.trackName}
            </span>
            <span className="role-access">
              {roleAccess ? `역할 ${roleAccess.role} · 권한 ${roleAccess.permissions.length}개` : '권한 확인 중'}
              {sessionExpiryText ? ` · 세션 ${sessionExpiryText} 만료` : ''}
              {typeof sessionSecondsRemaining === 'number' && sessionSecondsRemaining < 300 ? ' · 곧 만료' : ''}
              {accessError ? ` · ${accessError}` : ''}
            </span>
          </div>
          <div className="topbar-actions">
            <button className="ghost-button" onClick={() => onNavigate('/external-services')} type="button">
              외부 서비스
            </button>
            <button className="ghost-button" onClick={() => onNavigate('/profile/check')} type="button">
              회원정보
            </button>
            <button className="ghost-button" onClick={onLogout} type="button">
              로그아웃
            </button>
          </div>
        </header>
        <main>{children}</main>
      </div>
    </div>
  );
}

export default AppShell;
