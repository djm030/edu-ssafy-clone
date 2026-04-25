import { useEffect, useState } from 'react';
import { AUTH_REQUIRED_EVENT, FORBIDDEN_EVENT } from './api/client';
import AppShell from './components/AppShell';
import BoardListPage from './components/BoardListPage';
import { getCurrentRoleAccess, getMe, logout } from './api/app';
import { getErrorMessage } from './api/client';
import { mockUser } from './data/mockData';
import AdminCampusPage from './pages/AdminCampusPage';
import AttendanceAppealPage from './pages/AttendanceAppealPage';
import AttendancePage from './pages/AttendancePage';
import BoardDetailPage from './pages/BoardDetailPage';
import BoardPostWritePage from './pages/BoardPostWritePage';
import ClassmatesPage from './pages/ClassmatesPage';
import CurriculumPage from './pages/CurriculumPage';
import DashboardPage from './pages/DashboardPage';
import LevelPage from './pages/LevelPage';
import LoginPage from './pages/LoginPage';
import MaterialDetailPage from './pages/MaterialDetailPage';
import MaterialViewerPage from './pages/MaterialViewerPage';
import MaterialsPage from './pages/MaterialsPage';
import NotificationsPage from './pages/NotificationsPage';
import ProfileCheckPage from './pages/ProfileCheckPage';
import ProfileEditPage from './pages/ProfileEditPage';
import QnaDetailPage from './pages/QnaDetailPage';
import QnaListPage from './pages/QnaListPage';
import QnaNewPage from './pages/QnaNewPage';
import QuestDetailPage from './pages/QuestDetailPage';
import QuestPage from './pages/QuestPage';
import QuestSubmitPage from './pages/QuestSubmitPage';
import ReplaysPage from './pages/ReplaysPage';
import SurveyDetailPage from './pages/SurveyDetailPage';
import SurveyPage from './pages/SurveyPage';
import SurveyRespondPage from './pages/SurveyRespondPage';
import UnauthorizedPage from './pages/UnauthorizedPage';
import type { BoardScreenConfig, RoleAccess, UserProfile } from './types';

const boardScreens: Record<string, BoardScreenConfig> = {
  '/community/free': {
    boardCode: 'free',
    description: '교육생 커뮤니티 게시글을 카테고리와 검색어로 찾아봅니다.',
    emptyMessage: '게시글이 없습니다.',
    eyebrow: 'COMMUNITY',
    navLabel: '자유게시판',
    path: '/community/free',
    searchPlaceholder: '게시글 제목 또는 내용 검색',
    showEngagement: true,
    showWriteAction: true,
    title: '자유게시판',
    writePath: '/community/free/write',
  },
  '/help/faq': {
    boardCode: 'faq',
    description: '자주 묻는 질문과 답변을 확인합니다.',
    emptyMessage: '등록된 FAQ가 없습니다.',
    eyebrow: 'HELP DESK',
    navLabel: 'FAQ',
    path: '/help/faq',
    searchPlaceholder: 'FAQ 제목 검색',
    showEngagement: false,
    showWriteAction: false,
    title: 'FAQ',
  },
  '/help/notice': {
    boardCode: 'notice',
    description: 'SSAFY 운영 안내와 주요 공지를 확인합니다.',
    emptyMessage: '표시할 공지사항이 없습니다.',
    eyebrow: 'HELP DESK',
    navLabel: '공지사항',
    path: '/help/notice',
    searchPlaceholder: '공지 제목 또는 내용 검색',
    showEngagement: false,
    showWriteAction: false,
    title: '공지사항',
  },
  '/help/qna': {
    boardCode: 'qna',
    description: '1:1 문의 내역과 답변 상태를 확인합니다.',
    emptyMessage: '문의 내역이 없습니다.',
    eyebrow: 'HELP DESK',
    navLabel: '1:1 문의',
    path: '/help/qna',
    searchPlaceholder: '문의 제목 검색',
    showEngagement: true,
    showWriteAction: true,
    title: '1:1 문의',
    writePath: '/help/qna/new',
  },
};

function getCurrentPath(): string {
  return window.location.pathname;
}

function App() {
  const [path, setPath] = useState(getCurrentPath);
  const [user, setUser] = useState<UserProfile>(mockUser);
  const [roleAccess, setRoleAccess] = useState<RoleAccess>();
  const [accessError, setAccessError] = useState<string>();
  const [accessMessage, setAccessMessage] = useState('');

  useEffect(() => {
    const onPopState = () => setPath(getCurrentPath());
    window.addEventListener('popstate', onPopState);
    return () => window.removeEventListener('popstate', onPopState);
  }, []);

  useEffect(() => {
    const onAuthRequired = (event: Event) => {
      const message = event instanceof CustomEvent && typeof event.detail?.message === 'string'
        ? event.detail.message
        : '로그인이 필요한 화면입니다.';
      setAccessMessage(message);
      navigate('/login');
    };
    const onForbidden = (event: Event) => {
      const message = event instanceof CustomEvent && typeof event.detail?.message === 'string'
        ? event.detail.message
        : '접근 권한이 없습니다.';
      setAccessMessage(message);
      navigate('/forbidden');
    };

    window.addEventListener(AUTH_REQUIRED_EVENT, onAuthRequired);
    window.addEventListener(FORBIDDEN_EVENT, onForbidden);
    return () => {
      window.removeEventListener(AUTH_REQUIRED_EVENT, onAuthRequired);
      window.removeEventListener(FORBIDDEN_EVENT, onForbidden);
    };
  });

  const navigate = (nextPath: string) => {
    if (nextPath === path) return;
    window.history.pushState({}, '', nextPath);
    setPath(nextPath);
  };

  useEffect(() => {
    let cancelled = false;

    getMe()
      .then((response) => {
        if (!cancelled) setUser(response.user);
      })
      .catch((error) => {
        if (cancelled || getCurrentPath() === '/login') return;
        setAccessMessage(getErrorMessage(error));
        window.history.replaceState({}, '', '/login');
        setPath('/login');
      });

    return () => { cancelled = true; };
  }, []);

  useEffect(() => {
    let cancelled = false;
    getCurrentRoleAccess()
      .then((access) => {
        if (!cancelled) {
          setRoleAccess(access);
          setAccessError(undefined);
        }
      })
      .catch((error) => {
        if (!cancelled) setAccessError(getErrorMessage(error));
      });
    return () => { cancelled = true; };
  }, [user.email]);

  const handleLogout = () => {
    logout()
      .catch((error) => console.warn(`[auth] logout fallback navigation: ${getErrorMessage(error)}`))
      .finally(() => setUser(mockUser));
    setRoleAccess(undefined);
    navigate('/login');
  };

  const accessDenied = isDeniedPath(path, roleAccess);
  const page = accessDenied ? <UnauthorizedPage onGoHome={() => navigate('/')} path={path} /> : renderPage(path, roleAccess, navigate);

  if (path === '/login') {
    return <LoginPage message={accessMessage} onLogin={(nextUser) => { setUser(nextUser); setAccessMessage(''); navigate('/'); }} />;
  }

  return (
    <AppShell
      accessError={accessError}
      currentPath={path}
      onLogout={handleLogout}
      onNavigate={navigate}
      roleAccess={roleAccess}
      user={user}
    >
      {path === '/forbidden' ? <ForbiddenPage message={accessMessage} onNavigateHome={() => { setAccessMessage(''); navigate('/'); }} /> : page}
    </AppShell>
  );
}


function isDeniedPath(path: string, roleAccess?: RoleAccess): boolean {
  return Boolean(roleAccess?.deniedRoutes.some((route) => path === route || path.startsWith(`${route}/`)));
}

function renderPage(path: string, roleAccess: RoleAccess | undefined, navigate: (nextPath: string) => void) {
  const match = (pattern: RegExp) => path.match(pattern);
  const materialViewerMatch = match(/^\/learning\/materials\/(\d+)\/viewer$/);
  const materialMatch = match(/^\/learning\/materials\/(\d+)$/);
  const freePostMatch = match(/^\/community\/free\/(\d+)$/);
  const noticePostMatch = match(/^\/help\/notice\/(\d+)$/);
  const faqPostMatch = match(/^\/help\/faq\/(\d+)$/);
  const questSubmitMatch = match(/^\/quest\/(\d+)\/submit$/);
  const questMatch = match(/^\/quest\/(\d+)$/);
  const surveyRespondMatch = match(/^\/survey\/(\d+)\/respond$/);
  const surveyMatch = match(/^\/survey\/(\d+)$/);
  const qnaTicketMatch = match(/^\/help\/qna\/tickets\/(\d+)$/);

  if (path === '/') return <DashboardPage />;
  if (path === '/admin/campus') return <AdminCampusPage />;
  if (path === '/mycampus/attendance') return <AttendancePage />;
  if (path === '/mycampus/attendance/appeals/new') return <AttendanceAppealPage />;
  if (path === '/mycampus/level') return <LevelPage />;
  if (path === '/mycampus/notifications') return <NotificationsPage />;
  if (path === '/learning/curriculum') return <CurriculumPage />;
  if (path === '/learning/materials') return <MaterialsPage />;
  if (path === '/learning/replays') return <ReplaysPage />;
  if (path === '/profile/check') return <ProfileCheckPage onVerified={() => navigate('/profile/edit')} />;
  if (path === '/profile/edit') return <ProfileEditPage />;
  if (path === '/community/classmates') return <ClassmatesPage />;
  if (path === '/community/free/write' || path === '/community/free/new') return <BoardPostWritePage />;
  if (path === '/help/qna') return <QnaListPage canAnswerSupport={canAnswerSupport(roleAccess)} />;
  if (path === '/quest') return <QuestPage />;
  if (path === '/survey') return <SurveyPage />;
  if (path === '/help/qna/new') return <QnaNewPage />;
  if (qnaTicketMatch) return <QnaDetailPage canAnswerSupport={canAnswerSupport(roleAccess)} ticketId={Number(qnaTicketMatch[1])} />;
  if (questSubmitMatch) return <QuestSubmitPage questId={Number(questSubmitMatch[1])} />;
  if (surveyRespondMatch) return <SurveyRespondPage surveyId={Number(surveyRespondMatch[1])} />;
  if (materialViewerMatch) return <MaterialViewerPage materialId={Number(materialViewerMatch[1])} />;
  if (materialMatch) return <MaterialDetailPage materialId={Number(materialMatch[1])} />;
  if (freePostMatch) return <BoardDetailPage boardCode="free" postId={Number(freePostMatch[1])} title="자유게시판 상세" listPath="/community/free" />;
  if (noticePostMatch) return <BoardDetailPage boardCode="notice" postId={Number(noticePostMatch[1])} title="공지 상세" listPath="/help/notice" />;
  if (faqPostMatch) return <BoardDetailPage boardCode="faq" postId={Number(faqPostMatch[1])} title="FAQ 상세" listPath="/help/faq" />;
  if (questMatch) return <QuestDetailPage questId={Number(questMatch[1])} />;
  if (surveyMatch) return <SurveyDetailPage surveyId={Number(surveyMatch[1])} />;
  if (boardScreens[path]) return <BoardListPage config={boardScreens[path]} key={path} />;

  return <DashboardPage />;
}

function canAnswerSupport(roleAccess?: RoleAccess): boolean {
  return Boolean(roleAccess?.permissions.includes('*') || roleAccess?.permissions.includes('support:answer'));
}

export default App;

interface ForbiddenPageProps {
  message: string;
  onNavigateHome: () => void;
}

function ForbiddenPage({ message, onNavigateHome }: ForbiddenPageProps) {
  return (
    <main className="page">
      <section className="access-state" aria-labelledby="forbidden-title">
        <p className="eyebrow">ACCESS DENIED</p>
        <h1 id="forbidden-title">접근 권한이 없습니다.</h1>
        <p>{message || '현재 계정으로는 이 화면을 볼 수 없습니다. 권한을 확인하거나 관리자에게 문의해 주세요.'}</p>
        <button className="primary-action" onClick={onNavigateHome} type="button">
          대시보드로 돌아가기
        </button>
      </section>
    </main>
  );
}
