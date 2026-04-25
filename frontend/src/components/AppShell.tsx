import type { ReactNode } from 'react';
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

const navSections = [
  { title: '홈', items: [{ path: '/', label: '대시보드' }] },
  { title: '관리', items: [{ path: '/admin/campus', label: '캠퍼스 관리' }] },
  {
    title: '마이캠퍼스',
    items: [
      { path: '/mycampus/attendance', label: '출석현황' },
      { path: '/mycampus/level', label: '레벨/포인트' },
      { path: '/mycampus/notifications', label: '알림함' },
    ],
  },
  {
    title: '학습',
    items: [
      { path: '/learning/curriculum', label: '커리큘럼' },
      { path: '/learning/materials', label: '학습자료' },
      { path: '/learning/replays', label: '강의 다시보기' },
      { path: '/quest', label: 'Quest' },
      { path: '/survey', label: '설문' },
    ],
  },
  {
    title: '소통',
    items: [
      { path: '/community/free', label: '자유게시판' },
      { path: '/community/classmates', label: '우리반 보기' },
    ],
  },
  {
    title: '도움말',
    items: [
      { path: '/help/notice', label: '공지사항' },
      { path: '/help/faq', label: 'FAQ' },
      { path: '/help/qna', label: '1:1 문의' },
    ],
  },
];

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
