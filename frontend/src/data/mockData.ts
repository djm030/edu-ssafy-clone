import type {
  AttendanceRecord,
  BoardCategory,
  BoardCode,
  BoardPostListItem,
  Classmate,
  AcademicRuleCategory,
  CurriculumWeek,
  DashboardSummary,
  LearningMaterial,
  MentoringQuestionItem,
  MentorStoryItem,
  NotificationItem,
  QuestItem,
  ReplayItem,
  SurveyItem,
  UserProfile,
} from '../types';

export const mockUser: UserProfile = {
  id: 1,
  name: '김싸피',
  email: 'student@ssafy.com',
  role: 'learner',
  campusName: '서울',
  cohortName: '12기',
  trackName: 'Java',
};

export const mockDashboard: DashboardSummary = {
  user: { name: mockUser.name, campusName: mockUser.campusName, cohortName: mockUser.cohortName, trackName: mockUser.trackName },
  level: { level: 5, exp: 4200, nextLevelExp: 5000, scholarshipPoints: 85, rank: 12 },
  attendance: { present: 18, late: 1, absent: 0, appealAvailable: true },
  notifications: { unreadCount: 3, latest: ['공지사항 확인 필요', '이번 주 설문 미응답', 'Quest 채점 완료'] },
  today: { curriculumTitle: 'Spring Boot REST API', questTitle: '게시판 API 구현', surveyTitle: '주간 만족도 조사' },
};

export const mockAttendanceRecords: AttendanceRecord[] = [
  { id: 1, date: '2026-04-24', status: 'present', checkIn: '08:43', checkOut: '17:58' },
  { id: 2, date: '2026-04-23', status: 'present', checkIn: '08:51', checkOut: '18:01' },
  { id: 3, date: '2026-04-22', status: 'late', checkIn: '09:11', checkOut: '18:04', note: '지각', appealAvailable: true },
  { id: 4, date: '2026-04-21', status: 'present', checkIn: '08:39', checkOut: '17:55' },
  { id: 5, date: '2026-04-20', status: 'absent', checkIn: '-', checkOut: '-', note: '소명 가능', appealAvailable: true },
];

export const mockNotifications: NotificationItem[] = [
  { id: 1, title: '4월 프로젝트 일정 안내', message: '공통 프로젝트 일정이 업데이트되었습니다.', category: 'notice', createdAt: '2026-04-24 09:10', read: false },
  { id: 2, title: '주간 만족도 조사 미응답', message: '오늘 18시까지 설문 응답을 완료해 주세요.', category: 'survey', createdAt: '2026-04-24 08:30', read: false },
  { id: 3, title: 'Quest 채점 완료', message: 'React 목록 화면 구현 Quest가 채점되었습니다.', category: 'quest', createdAt: '2026-04-23 17:42', read: true },
  { id: 4, title: '강의 다시보기 등록', message: 'Spring Boot REST API 강의가 다시보기로 등록되었습니다.', category: 'learning', createdAt: '2026-04-23 14:05', read: true },
];

export const mockCurriculumWeeks: CurriculumWeek[] = [
  { id: 1, week: 1, title: 'Java 기본 문법', period: '2026-04-06 ~ 2026-04-10', lessons: ['변수와 타입', '조건문과 반복문', '객체지향 입문'], status: 'done' },
  { id: 2, week: 2, title: 'Spring Boot REST API', period: '2026-04-13 ~ 2026-04-17', lessons: ['Controller', 'Service 계층', '예외 처리'], status: 'done' },
  { id: 3, week: 3, title: 'React 화면 구현', period: '2026-04-20 ~ 2026-04-24', lessons: ['컴포넌트 설계', '상태 관리', 'API 연동'], status: 'current' },
  { id: 4, week: 4, title: '프로젝트 통합', period: '2026-04-27 ~ 2026-05-01', lessons: ['인증 흐름', '배포 점검', '회고'], status: 'planned' },
];

export const mockReplays: ReplayItem[] = [
  { id: 1, title: 'Spring Boot REST API', instructor: '김교수', date: '2026-04-24', duration: '01:42:00', category: 'Backend', watched: false },
  { id: 2, title: 'React 상태 관리', instructor: '이코치', date: '2026-04-23', duration: '01:18:00', category: 'Frontend', watched: true },
  { id: 3, title: 'Git 협업 전략', instructor: '박코치', date: '2026-04-22', duration: '00:54:00', category: 'Common', watched: false },
];

export const mockClassmates: Classmate[] = [
  { id: 1, name: '김싸피', campusName: '서울', trackName: 'Java', teamName: 'A반 1팀', statusMessage: '프로젝트 진행 중' },
  { id: 2, name: '박싸피', campusName: '서울', trackName: 'Java', teamName: 'A반 1팀', statusMessage: '알고리즘 스터디 모집' },
  { id: 3, name: '이싸피', campusName: '서울', trackName: 'Java', teamName: 'A반 2팀', statusMessage: 'JPA 학습 중' },
  { id: 4, name: '최싸피', campusName: '서울', trackName: 'Java', teamName: 'A반 2팀', statusMessage: '발표 자료 정리' },
];

export const mockCategories: Record<BoardCode, BoardCategory[]> = {
  notice: [{ id: 1, name: '전체 공지', sortOrder: 1, postCount: 3 }, { id: 2, name: '운영', sortOrder: 2, postCount: 1 }, { id: 3, name: '학습', sortOrder: 3, postCount: 1 }],
  free: [{ id: 4, name: '자유', sortOrder: 1, postCount: 2 }, { id: 5, name: '질문', sortOrder: 2, postCount: 1 }, { id: 6, name: '스터디', sortOrder: 3, postCount: 1 }],
  anonymous: [{ id: 11, name: '익명', sortOrder: 1, postCount: 1 }],
  faq: [{ id: 7, name: '학사', sortOrder: 1, postCount: 2 }, { id: 8, name: '시스템', sortOrder: 2, postCount: 1 }],
  qna: [{ id: 9, name: '출결', sortOrder: 1, postCount: 1 }, { id: 10, name: '학습', sortOrder: 2, postCount: 1 }],
};

export const mockPosts: BoardPostListItem[] = [
  { id: 101, boardCode: 'notice', category: { id: 1, name: '전체 공지' }, title: '4월 공통 프로젝트 일정 안내', authorName: '교육지원팀', createdAt: '2026-04-24T09:00:00+09:00', viewCount: 145, commentCount: 0, reactionCount: 0, bookmarkCount: 8, hasAttachment: true, isPinned: true, content: '4월 공통 프로젝트 일정과 제출 마감 시간이 확정되었습니다. 팀별 진행 상황을 확인하고 지정된 시간 안에 산출물을 제출해 주세요.', attachments: [{ id: 9101, originalFilename: 'project-schedule.pdf', storedPath: '/uploads/notice/project-schedule.pdf', mimeType: 'application/pdf', fileSize: 204800 }] },
  { id: 102, boardCode: 'notice', category: { id: 2, name: '운영' }, title: '캠퍼스 출결 소명 처리 기준', authorName: '운영자', createdAt: '2026-04-23T11:00:00+09:00', viewCount: 92, commentCount: 0, reactionCount: 0, bookmarkCount: 3, content: '출결 소명은 발생일 기준 3영업일 이내에 신청해야 하며 증빙 파일이 필요합니다.' },
  { id: 103, boardCode: 'notice', category: { id: 3, name: '학습' }, title: '금요일 라이브 강의 자료 배포', authorName: '교수지원', createdAt: '2026-04-22T15:20:00+09:00', viewCount: 71, commentCount: 0, reactionCount: 0, bookmarkCount: 5, hasAttachment: true, content: '이번 주 라이브 강의 자료가 학습자료실에 등록되었습니다.' },
  { id: 201, boardCode: 'free', category: { id: 4, name: '자유' }, title: '알고리즘 스터디 같이 하실 분 모집합니다', authorName: '박싸피', createdAt: '2026-04-24T13:40:00+09:00', viewCount: 38, commentCount: 6, reactionCount: 12, bookmarkCount: 4, isNew: true, content: '매주 화요일 저녁에 알고리즘 문제를 함께 풀 스터디원을 모집합니다. 관심 있는 분은 댓글을 남겨 주세요.' },
  { id: 202, boardCode: 'free', category: { id: 5, name: '질문' }, title: 'JPA N+1 문제 확인 방법이 궁금합니다', authorName: '이싸피', createdAt: '2026-04-24T10:12:00+09:00', viewCount: 51, commentCount: 4, reactionCount: 7, bookmarkCount: 2, content: '프로젝트에서 N+1 문제가 발생하는지 로그로 확인하는 방법을 알고 싶습니다.' },
  { id: 203, boardCode: 'free', category: { id: 6, name: '스터디' }, title: '관통 프로젝트 회고 템플릿 공유', authorName: '최싸피', createdAt: '2026-04-23T18:21:00+09:00', viewCount: 64, commentCount: 3, reactionCount: 15, bookmarkCount: 9, hasAttachment: true, content: '팀 회고에 사용할 수 있는 간단한 템플릿을 공유합니다.', attachments: [{ id: 9203, originalFilename: 'retrospective-template.xlsx', storedPath: '/uploads/free/retrospective-template.xlsx', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', fileSize: 32768 }] },
  { id: 204, boardCode: 'anonymous', category: { id: 11, name: '익명' }, title: '익명으로 학습 고민을 공유합니다', authorName: '익명', createdAt: '2026-04-24T19:00:00+09:00', viewCount: 27, commentCount: 2, reactionCount: 5, bookmarkCount: 1, content: '강의 진도를 따라가는 방법을 익명으로 묻고 싶습니다.', comments: [{ id: 2041, postId: 204, content: '스터디 시간을 짧게 고정해 보세요.', authorName: '익명', createdAt: '2026-04-24T20:00:00+09:00', replies: [] }] },
  { id: 301, boardCode: 'faq', category: { id: 7, name: '학사' }, title: '출결 소명은 언제까지 제출할 수 있나요?', authorName: '교육지원팀', createdAt: '2026-04-20T09:00:00+09:00', viewCount: 210, isPinned: true, content: '출결 소명은 발생일 기준 3영업일 이내에 제출할 수 있습니다. 소명 가능 상태가 표시될 때 신청해 주세요.' },
  { id: 302, boardCode: 'faq', category: { id: 8, name: '시스템' }, title: '비밀번호를 잊어버렸을 때 어떻게 하나요?', authorName: '운영자', createdAt: '2026-04-18T13:00:00+09:00', viewCount: 188, content: '로그인 화면의 비밀번호 찾기 링크를 이용하거나 캠퍼스 운영자에게 문의해 주세요.' },
  { id: 401, boardCode: 'qna', category: { id: 9, name: '출결' }, title: '오전 입실 기록이 누락되었습니다', authorName: '김싸피', createdAt: '2026-04-24T12:10:00+09:00', viewCount: 12, commentCount: 1, isNew: true, content: '오전 입실 시 QR 체크를 했는데 기록이 보이지 않습니다. 확인 부탁드립니다.' },
  { id: 402, boardCode: 'qna', category: { id: 10, name: '학습' }, title: '프로젝트 제출 파일 형식 문의', authorName: '박싸피', createdAt: '2026-04-23T16:40:00+09:00', viewCount: 19, commentCount: 2, content: '프로젝트 제출 시 압축 파일명 규칙과 포함해야 할 파일을 문의드립니다.' },
];



export const mockMentorStories: MentorStoryItem[] = [
  {
    id: 901,
    title: '비전공자에서 백엔드 개발자로 성장한 기록',
    summary: '꾸준한 학습 루틴과 코드 리뷰로 취업 준비 방향을 잡은 멘토 스토리입니다.',
    content: '매일 작은 기능을 끝까지 구현하고 회고를 남긴 경험이 실무 적응에 큰 도움이 되었습니다.',
    mentorName: '김멘토',
    mentorCompany: '네이버',
    mentorRole: '백엔드',
    viewCount: 42,
    publishedAt: '2026-04-24T09:00:00+09:00',
  },
  {
    id: 902,
    title: '프론트엔드 포트폴리오를 서비스처럼 만드는 법',
    summary: 'UI 완성도와 배포 경험을 중심으로 포트폴리오를 개선한 사례입니다.',
    content: '사용자 흐름, 접근성, 에러 상태를 함께 보여주면 단순 화면 과제보다 강한 인상을 남길 수 있습니다.',
    mentorName: '이멘토',
    mentorCompany: '카카오',
    mentorRole: '프론트엔드',
    viewCount: 31,
    publishedAt: '2026-04-23T10:30:00+09:00',
  },
];

export const mockMentoringQuestions: MentoringQuestionItem[] = [
  {
    id: 951,
    title: '백엔드 프로젝트 경험을 어떻게 포트폴리오로 정리할까요?',
    summary: '프로젝트 경험 정리 방향을 묻는 멘토링 질문입니다.',
    content: '팀 프로젝트에서 맡은 API와 장애 대응 경험을 어떤 구조로 정리하면 좋을지 궁금합니다.',
    category: '커리어',
    status: 'ANSWERED',
    anonymous: false,
    authorName: '김싸피',
    answerCount: 1,
    createdAt: '2026-04-24T12:00:00+09:00',
    answers: [
      {
        id: 95101,
        content: '문제 상황, 본인 의사결정, 검증 결과를 한 흐름으로 정리하면 실무 역량이 잘 드러납니다.',
        mentorName: 'Demo Mentor',
        createdAt: '2026-04-24T14:20:00+09:00',
      },
    ],
  },
  {
    id: 952,
    title: '프론트엔드 면접에서 상태 관리를 어떻게 설명하나요?',
    summary: '면접 답변 프레임을 묻는 열린 질문입니다.',
    content: 'React 프로젝트에서 서버 상태와 UI 상태를 분리했던 경험을 어떻게 말하면 좋을까요?',
    category: '면접',
    status: 'OPEN',
    anonymous: true,
    authorName: '익명 질문자',
    answerCount: 0,
    createdAt: '2026-04-23T16:30:00+09:00',
    answers: [],
  },
];

export const mockAcademicRuleCategories: AcademicRuleCategory[] = [
  {
    id: 31,
    name: '출결',
    displayOrder: 1,
    ruleCount: 2,
    rules: [
      { id: 3101, categoryId: 31, categoryName: '출결', question: '지각과 결석은 어떻게 산정되나요?', answer: '입실/퇴실 기록과 운영자가 승인한 소명 결과를 기준으로 출결 상태를 산정합니다.', updatedAt: '2026-04-24T09:00:00+09:00' },
      { id: 3102, categoryId: 31, categoryName: '출결', question: '출결 소명은 언제까지 가능한가요?', answer: '발생일 기준 3영업일 이내에 증빙 자료와 함께 1:1 문의 또는 출결 소명 화면에서 신청합니다.', updatedAt: '2026-04-24T09:05:00+09:00' },
    ],
  },
  {
    id: 32,
    name: '평가',
    displayOrder: 2,
    ruleCount: 1,
    rules: [
      { id: 3201, categoryId: 32, categoryName: '평가', question: 'Quest/평가 미제출은 어떻게 처리되나요?', answer: '제출 기간 종료 후 미제출 상태로 집계되며, 운영 정책에 따라 보완 제출 또는 감점이 적용될 수 있습니다.', updatedAt: '2026-04-24T10:00:00+09:00' },
    ],
  },
  {
    id: 33,
    name: '수료/포인트',
    displayOrder: 3,
    ruleCount: 1,
    rules: [
      { id: 3301, categoryId: 33, categoryName: '수료/포인트', question: '장학 포인트는 어디에서 확인하나요?', answer: '마이캠퍼스의 레벨&장학포인트 및 교육현황 화면에서 현재 포인트와 최근 반영 상태를 확인합니다.', updatedAt: '2026-04-24T11:00:00+09:00' },
    ],
  },
];

export const mockMaterials: LearningMaterial[] = [
  { id: 1, title: 'Spring Boot REST API 실습 자료', type: 'file', authorName: '교수지원', createdAt: '2026-04-24', viewCount: 184, fileName: 'spring-rest-api.pdf', description: 'REST API 계층 설계와 예외 처리 실습 자료입니다.', likeCount: 12, bookmarkCount: 6, liked: false, bookmarked: true },
  { id: 2, title: 'JPA 기본 개념 eBook', type: 'ebook', authorName: '교육지원팀', createdAt: '2026-04-23', viewCount: 231, fileName: 'jpa-basics.epub', description: '엔티티, 연관관계, 영속성 컨텍스트를 정리한 eBook입니다.', likeCount: 21, bookmarkCount: 9 },
  { id: 3, title: 'Vue 상태 관리 다시보기', type: 'video', authorName: '강의실', createdAt: '2026-04-22', viewCount: 97, description: 'Vue 상태 관리 강의 다시보기 링크입니다.', likeCount: 7, bookmarkCount: 4 },
  { id: 4, title: 'Git 협업 체크리스트', type: 'link', authorName: '운영자', createdAt: '2026-04-21', viewCount: 143, description: '팀 협업 전 확인해야 할 Git 규칙 체크리스트입니다.', likeCount: 15, bookmarkCount: 11 },
];

export const mockQuests: QuestItem[] = [
  { id: 1, title: '게시판 API 구현', startsAt: '2026-04-22', endsAt: '2026-04-26', status: 'progress', description: '게시판 목록 API와 검색 조건을 구현합니다.', tasks: ['카테고리 필터', '검색어 처리', '페이지네이션 응답'] },
  { id: 2, title: '출결 데이터 모델링', startsAt: '2026-04-18', endsAt: '2026-04-21', status: 'done', description: '출결 기록과 소명 상태를 모델링합니다.', tasks: ['테이블 설계', '상태 코드 정의'] },
  { id: 3, title: 'React 목록 화면 구현', startsAt: '2026-04-15', endsAt: '2026-04-17', status: 'graded', description: '목록 화면과 로딩/빈/오류 상태를 구현합니다.', tasks: ['목록 렌더링', '오류 상태', '빌드 검증'] },
];

export const mockSurveys: SurveyItem[] = [
  { id: 1, title: '주간 만족도 조사', required: true, startsAt: '2026-04-22', endsAt: '2026-04-25', answered: false, description: '이번 주 학습 경험을 확인하는 설문입니다.', questionCount: 2, questions: [{ id: 1, text: '이번 주 난이도는 적절했나요?' }, { id: 2, text: '추가 지원이 필요한 주제가 있나요?' }] },
  { id: 2, title: '프로젝트 팀 빌딩 설문', required: true, startsAt: '2026-04-20', endsAt: '2026-04-24', answered: true, description: '프로젝트 팀 구성 선호를 확인합니다.', questionCount: 2, questions: [{ id: 1, text: '선호하는 역할은 무엇인가요?' }, { id: 2, text: '협업 가능 시간을 선택해 주세요.' }] },
  { id: 3, title: '특강 수요 조사', required: false, startsAt: '2026-04-18', endsAt: '2026-04-27', answered: false, description: '희망 특강 주제를 조사합니다.', questionCount: 1, questions: [{ id: 1, text: '듣고 싶은 특강 주제를 입력해 주세요.' }] },
];
