export interface NavItem {
  path: string;
  label: string;
}

export interface NavSection {
  title: string;
  items: NavItem[];
}

export interface ScreenSmokeRoute {
  id: string;
  priority: number;
  feature: string;
  path: string;
  label: string;
}

export const navSections: NavSection[] = [
  { title: '홈', items: [{ path: '/', label: '대시보드' }] },
  { title: '운영', items: [{ path: '/ops/readiness', label: '운영 점검' }] },
  { title: '관리', items: [{ path: '/admin/campus', label: '캠퍼스 관리' }] },
  {
    title: '마이캠퍼스',
    items: [
      { path: '/mycampus/attendance', label: '출석현황' },
      { path: '/mycampus/elearning', label: '학습중 이러닝' },
      { path: '/mycampus/bookmarks', label: '찜한 목록' },
      { path: '/mycampus/documents', label: '서류제출' },
      { path: '/mycampus/pledges', label: '교육생 서약서' },
      { path: '/mycampus/education-status', label: '교육현황' },
      { path: '/mycampus/ebooks', label: 'SSAFY e-book' },
      { path: '/mycampus/level', label: '레벨/포인트' },
      { path: '/mycampus/notifications', label: '알림함' },
    ],
  },
  {
    title: '학습',
    items: [
      { path: '/learning/curriculum', label: '커리큘럼' },
      { path: '/learning/materials', label: '학습자료' },
      { path: '/learning/required-studies', label: '필수학습' },
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

export const screenSmokeRoutes: ScreenSmokeRoute[] = [
  { id: 'dashboard', priority: 1, feature: '인증/사용자 프로필', path: '/', label: '대시보드' },
  { id: 'login', priority: 1, feature: '인증/사용자 프로필', path: '/login', label: '로그인' },
  { id: 'profile-check', priority: 1, feature: '인증/사용자 프로필', path: '/profile/check', label: '회원정보 확인' },
  { id: 'profile-edit', priority: 1, feature: '인증/사용자 프로필', path: '/profile/edit', label: '회원정보 수정' },
  { id: 'attendance', priority: 2, feature: '출석 조회', path: '/mycampus/attendance', label: '출석현황' },
  { id: 'attendance-appeal', priority: 2, feature: '출석 이의신청', path: '/mycampus/attendance/appeals/new', label: '출석 이의신청' },
  { id: 'board-list', priority: 3, feature: '게시판', path: '/community/free', label: '자유게시판 목록' },
  { id: 'board-detail', priority: 3, feature: '게시글/댓글', path: '/community/free/1', label: '자유게시판 상세' },
  { id: 'board-write', priority: 3, feature: '게시글 작성', path: '/community/free/write', label: '자유게시판 작성' },
  { id: 'survey-list', priority: 4, feature: '설문', path: '/survey', label: '설문 목록' },
  { id: 'survey-detail', priority: 4, feature: '설문 문항', path: '/survey/1', label: '설문 상세' },
  { id: 'survey-respond', priority: 4, feature: '설문 응답', path: '/survey/1/respond', label: '설문 응답' },
  { id: 'support-list', priority: 5, feature: '1:1 문의', path: '/help/qna', label: '문의 목록' },
  { id: 'support-new', priority: 5, feature: '1:1 문의', path: '/help/qna/new', label: '문의 작성' },
  { id: 'support-detail', priority: 5, feature: '1:1 문의 답변', path: '/help/qna/tickets/1', label: '문의 상세' },
  { id: 'notifications', priority: 6, feature: '알림', path: '/mycampus/notifications', label: '알림함' },
  { id: 'elearning-list', priority: 10, feature: '학습중 이러닝', path: '/mycampus/elearning', label: '학습중 이러닝 목록' },
  { id: 'elearning-detail', priority: 10, feature: '학습중 이러닝', path: '/mycampus/elearning/1', label: '학습중 이러닝 상세' },
  { id: 'bookmarks', priority: 11, feature: '찜한 목록', path: '/mycampus/bookmarks', label: '찜한 목록' },
  { id: 'documents', priority: 12, feature: '서류제출', path: '/mycampus/documents', label: '서류제출' },
  { id: 'pledges', priority: 13, feature: '교육생 서약서', path: '/mycampus/pledges', label: '교육생 서약서' },
  { id: 'pledge-detail', priority: 13, feature: '교육생 서약서', path: '/mycampus/pledges/1', label: '교육생 서약서 상세' },
  { id: 'education-status', priority: 14, feature: '교육현황', path: '/mycampus/education-status', label: '교육현황' },
  { id: 'ebooks', priority: 15, feature: 'SSAFY e-book', path: '/mycampus/ebooks', label: 'SSAFY e-book' },
  { id: 'ebook-detail', priority: 15, feature: 'SSAFY e-book', path: '/mycampus/ebooks/1', label: 'SSAFY e-book 상세' },
  { id: 'curriculum', priority: 7, feature: '커리큘럼', path: '/learning/curriculum', label: '커리큘럼' },
  { id: 'materials', priority: 7, feature: '학습자료', path: '/learning/materials', label: '학습자료 목록' },
  { id: 'material-detail', priority: 7, feature: '학습자료', path: '/learning/materials/1', label: '학습자료 상세' },
  { id: 'material-viewer', priority: 7, feature: '학습자료 다시보기', path: '/learning/materials/1/viewer', label: '자료 뷰어' },
  { id: 'required-studies', priority: 16, feature: '필수학습', path: '/learning/required-studies', label: '필수학습 목록' },
  { id: 'required-study-detail', priority: 16, feature: '필수학습', path: '/learning/required-studies/1', label: '필수학습 상세' },
  { id: 'replays', priority: 7, feature: '강의 다시보기', path: '/learning/replays', label: '강의 다시보기' },
  { id: 'quest-list', priority: 8, feature: '퀘스트/평가', path: '/quest', label: '퀘스트 목록' },
  { id: 'quest-detail', priority: 8, feature: '퀘스트/평가', path: '/quest/1', label: '퀘스트 상세' },
  { id: 'quest-submit', priority: 8, feature: '퀘스트 제출', path: '/quest/1/submit', label: '퀘스트 제출' },
  { id: 'ops-readiness', priority: 9, feature: '프로덕션 하드닝', path: '/ops/readiness', label: '운영 준비 점검' },
];
