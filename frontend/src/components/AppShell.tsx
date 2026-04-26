import { useState, type ReactNode } from 'react';
import { navSections } from '../routes';
import type { NavItem, NavSection } from '../routes';
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

function isActivePath(currentPath: string, itemPath: string): boolean {
  if (itemPath === '/') return currentPath === '/';
  return currentPath === itemPath || currentPath.startsWith(`${itemPath}/`);
}

function isActiveSection(currentPath: string, section: NavSection): boolean {
  return section.items.some((item) => isActivePath(currentPath, item.path));
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
  const [openSectionTitle, setOpenSectionTitle] = useState<string | null>(null);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const sessionExpiryText = formatSessionExpiry(sessionExpiresAt);
  const sessionWarning = typeof sessionSecondsRemaining === 'number' && sessionSecondsRemaining < 300;
  const isDeniedNavItem = (path: string) => Boolean(
    roleAccess?.deniedRoutes.some((route) => path === route || path.startsWith(`${route}/`))
  );
  const visibleSections = navSections
    .map((section) => ({
      ...section,
      items: section.items.filter((item) => !isDeniedNavItem(item.path)),
    }))
    .filter((section) => section.items.length > 0);
  const openSection = visibleSections.find((section) => section.title === openSectionTitle) ?? null;

  const navigate = (path: string) => {
    onNavigate(path);
    setOpenSectionTitle(null);
    setMobileMenuOpen(false);
  };

  return (
    <div className="app-frame">
      <header className="global-header" onMouseLeave={() => setOpenSectionTitle(null)}>
        <div className="global-header__utility" aria-label="사용자 및 세션 정보">
          <button className="brand global-brand" onClick={() => navigate('/')} type="button" aria-label="EduSSAFY 홈으로 이동">
            eduSSAFY
          </button>
          <div className="global-header__identity">
            <strong>{user.name}</strong>
            <span>{user.campusName} 캠퍼스 · {user.cohortName} · {user.trackName}</span>
            <span className={sessionWarning ? 'session-chip warning' : 'session-chip'}>
              {roleAccess ? `역할 ${roleAccess.role} · 권한 ${roleAccess.permissions.length}개` : '권한 확인 중'}
              {sessionExpiryText ? ` · 세션 ${sessionExpiryText} 만료` : ''}
              {sessionWarning ? ' · 곧 만료' : ''}
              {accessError ? ` · ${accessError}` : ''}
            </span>
          </div>
          <div className="topbar-actions global-header__actions">
            <button className="ghost-button" onClick={() => navigate('/mycampus/notifications')} type="button">
              알림함
            </button>
            <button className="ghost-button" onClick={() => navigate('/external-services')} type="button">
              외부 서비스
            </button>
            <button className="ghost-button" onClick={() => navigate('/profile/check')} type="button">
              회원정보
            </button>
            <button className="ghost-button" onClick={onLogout} type="button">
              로그아웃
            </button>
            <button
              aria-controls="global-mobile-menu"
              aria-expanded={mobileMenuOpen}
              className="ghost-button global-menu-toggle"
              onClick={() => setMobileMenuOpen((value) => !value)}
              type="button"
            >
              전체메뉴
            </button>
          </div>
        </div>

        <nav className="global-nav" aria-label="EduSSAFY 상단 대메뉴">
          {visibleSections.map((section) => {
            const panelId = `mega-panel-${section.title.replace(/\s+/g, '-')}`;
            const active = isActiveSection(currentPath, section);
            const expanded = openSectionTitle === section.title;
            return (
              <button
                aria-controls={panelId}
                aria-expanded={expanded}
                className={active ? 'global-nav__item active' : 'global-nav__item'}
                key={section.title}
                onClick={() => setOpenSectionTitle(expanded ? null : section.title)}
                onFocus={() => setOpenSectionTitle(section.title)}
                onMouseEnter={() => setOpenSectionTitle(section.title)}
                type="button"
              >
                {section.title}
              </button>
            );
          })}
        </nav>

        {openSection ? (
          <MegaMenuPanel currentPath={currentPath} onNavigate={navigate} section={openSection} />
        ) : null}

        <div className={mobileMenuOpen ? 'global-mobile-menu open' : 'global-mobile-menu'} id="global-mobile-menu">
          {visibleSections.map((section) => (
            <div className="global-mobile-menu__section" key={section.title}>
              <strong>{section.title}</strong>
              <div>
                {section.items.map((item) => (
                  <MenuButton currentPath={currentPath} item={item} key={item.path} onNavigate={navigate} />
                ))}
              </div>
            </div>
          ))}
        </div>
      </header>

      <div className="app-shell edu-shell">
        <div className="workspace">
          <main>{children}</main>
        </div>
      </div>
    </div>
  );
}

function MegaMenuPanel({ currentPath, onNavigate, section }: { currentPath: string; onNavigate: (path: string) => void; section: NavSection }) {
  const panelId = `mega-panel-${section.title.replace(/\s+/g, '-')}`;
  return (
    <div className="mega-menu-panel" id={panelId} role="region" aria-label={`${section.title} 하위 메뉴`}>
      <div>
        <p className="eyebrow">{section.title}</p>
        <strong>{section.title} 바로가기</strong>
        <span>실제 EduSSAFY 대메뉴처럼 하위 화면을 한 번에 탐색합니다.</span>
      </div>
      <div className="mega-menu-panel__links">
        {section.items.map((item) => (
          <MenuButton currentPath={currentPath} item={item} key={item.path} onNavigate={onNavigate} />
        ))}
      </div>
    </div>
  );
}

function MenuButton({ currentPath, item, onNavigate }: { currentPath: string; item: NavItem; onNavigate: (path: string) => void }) {
  return (
    <button
      className={isActivePath(currentPath, item.path) ? 'mega-menu-link active' : 'mega-menu-link'}
      onClick={() => onNavigate(item.path)}
      type="button"
    >
      <span>{item.label}</span>
      <small>{item.path}</small>
    </button>
  );
}

export default AppShell;
