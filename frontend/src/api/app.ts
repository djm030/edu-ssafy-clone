import { buildQuery, fetchJson } from './client';
import {
  mockAttendanceRecords,
  mockClassmates,
  mockCurriculumWeeks,
  mockDashboard,
  mockMaterials,
  mockNotifications,
  mockQuests,
  mockReplays,
  mockSurveys,
  mockUser,
} from '../data/mockData';
import type {
  AdminCampusStructure,
  AdminClassGroupDraft,
  AdminClassGroupItem,
  AttendanceRecord,
  AttendanceAppealDraft,
  BoardPostDraft,
  Classmate,
  CurriculumWeek,
  DashboardSummary,
  LearningMaterial,
  LoginResponse,
  RoleAccess,
  NotificationItem,
  QnaDraft,
  QuestItem,
  QuestSubmissionDraft,
  ReplayItem,
  ProfileEditDraft,
  ProfileDetails,
  SupportTicketDraft,
  SupportTicketDetail,
  SupportTicketItem,
  SupportTicketMessageDraft,
  SupportTicketMessageItem,
  SupportTicketsResponse,
  SurveyResponseDraft,
  SurveyItem,
} from '../types';

type ItemResponse<T> = { item: T };

type BackendDashboardSummary = Omit<DashboardSummary, 'notifications'> & {
  notifications: {
    unreadCount: number;
    latest: Array<string | BackendNotificationItem>;
  };
};

type BackendAttendanceRecord = Partial<AttendanceRecord> & {
  checkInAt?: string | null;
  checkOutAt?: string | null;
  approvalType?: string | null;
  appealAvailable?: boolean;
  appealId?: number | null;
  appealStatus?: string | null;
  appealRequestedAt?: string | null;
};

type BackendNotificationItem = Partial<NotificationItem> & {
  body?: string | null;
  type?: NotificationItem['category'] | string | null;
};

type BackendCurriculumItem = Partial<CurriculumWeek> & {
  weekNo?: number | null;
  classDate?: string | null;
  startTime?: string | null;
  endTime?: string | null;
  type?: string | null;
  instructorName?: string | null;
  classroom?: string | null;
};

type BackendReplayItem = Partial<ReplayItem> & {
  curriculumScheduleId?: number | null;
  versionNo?: number | null;
  publishedAt?: string | null;
};

type BackendMaterialResource = {
  title?: string | null;
  type?: string | null;
  targetUrl?: string | null;
};

type BackendMaterialItem = Partial<LearningMaterial> & {
  materialTypeCode?: string | null;
  summary?: string | null;
  detailUrl?: string | null;
  resources?: BackendMaterialResource[];
};

type BackendQuestItem = Partial<QuestItem> & {
  startAt?: string | null;
  endAt?: string | null;
  submitStatus?: string | null;
  resultStatus?: string | null;
  type?: string | null;
  classification?: string | null;
  maxExp?: number | null;
};

type BackendSurveyItem = Partial<SurveyItem> & {
  startAt?: string | null;
  endAt?: string | null;
  status?: string | null;
  completed?: boolean;
  category?: string | null;
  questionCount?: number;
};

type BackendClassmate = Partial<Classmate> & {
  className?: string | null;
};

function toDateText(value?: string | null): string {
  return value ? value.slice(0, 10) : '-';
}

function isPastDate(value?: string | null): boolean {
  if (!value) return false;
  return value.slice(0, 10) < new Date().toISOString().slice(0, 10);
}

function isToday(value?: string | null): boolean {
  if (!value) return false;
  return value.slice(0, 10) === new Date().toISOString().slice(0, 10);
}

function toAttendanceStatus(item: BackendAttendanceRecord): AttendanceRecord['status'] {
  if (item.status === 'late') return 'late';
  if (item.status === 'absent') return 'absent';
  if (item.status === 'early_leave') return 'early_leave';
  if (item.status === 'excused') return 'excused';
  return 'present';
}

function toAttendanceRecord(item: BackendAttendanceRecord): AttendanceRecord {
  const appealStatus = item.appealStatus || undefined;
  return {
    id: Number(item.id),
    date: item.date || '-',
    status: toAttendanceStatus(item),
    checkIn: item.checkIn || item.checkInAt?.slice(0, 5),
    checkOut: item.checkOut || item.checkOutAt?.slice(0, 5),
    note: item.note || (appealStatus === 'requested' ? '소명 접수됨' : item.approvalType || undefined),
    appealAvailable: Boolean(item.appealAvailable),
    appealId: item.appealId ?? undefined,
    appealStatus,
    appealRequestedAt: item.appealRequestedAt || undefined,
  };
}

function toNotificationCategory(value?: string | null): NotificationItem['category'] {
  if (value === 'learning' || value === 'quest' || value === 'survey') return value;
  return 'notice';
}

function toNotificationItem(item: BackendNotificationItem): NotificationItem {
  return {
    id: Number(item.id),
    title: item.title || 'Notification',
    message: item.message || item.body || '',
    category: toNotificationCategory(item.category || item.type),
    createdAt: item.createdAt || '-',
    read: Boolean(item.read),
  };
}

function toDashboardSummary(summary: BackendDashboardSummary): DashboardSummary {
  return {
    ...summary,
    notifications: {
      unreadCount: summary.notifications.unreadCount,
      latest: summary.notifications.latest.map((item) => (typeof item === 'string' ? item : toNotificationItem(item).title)),
    },
  };
}

function toCurriculumStatus(item: BackendCurriculumItem): CurriculumWeek['status'] {
  if (item.status === 'done' || isPastDate(item.classDate)) return 'done';
  if (item.status === 'current' || isToday(item.classDate)) return 'current';
  return 'planned';
}

function toCurriculumWeek(item: BackendCurriculumItem): CurriculumWeek {
  const period = item.period || [item.classDate, [item.startTime, item.endTime].filter(Boolean).join(' ~ ')].filter(Boolean).join(' ');
  const lessonDetails = [item.title, item.instructorName, item.classroom, item.type].filter(Boolean);

  return {
    id: Number(item.id),
    week: item.week || item.weekNo || 0,
    title: item.title || 'Curriculum',
    period: period || '-',
    lessons: item.lessons?.length ? item.lessons : [lessonDetails.join(' · ') || 'Lesson'],
    status: toCurriculumStatus(item),
  };
}

function toReplayItem(item: BackendReplayItem): ReplayItem {
  return {
    id: Number(item.id),
    title: item.title || 'Replay',
    instructor: item.instructor || '-',
    date: item.date || toDateText(item.publishedAt),
    duration: item.duration || '-',
    category: item.category || `v${item.versionNo ?? 1}`,
    watched: Boolean(item.watched),
  };
}

function toMaterialType(value?: string | null): LearningMaterial['type'] {
  if (value === 'ebook' || value === 'video' || value === 'link') return value;
  return 'file';
}

function toLearningMaterial(item: BackendMaterialItem): LearningMaterial {
  const firstResource = item.resources?.[0];

  return {
    id: Number(item.id),
    title: item.title || 'Learning Material',
    type: toMaterialType(item.type || item.materialTypeCode),
    authorName: item.authorName || 'SSAFY',
    createdAt: item.createdAt || '-',
    viewCount: item.viewCount ?? 0,
    description: item.description || item.summary || undefined,
    fileName: item.fileName || firstResource?.title || item.detailUrl || firstResource?.targetUrl || undefined,
  };
}

function toQuestStatus(item: BackendQuestItem): QuestItem['status'] {
  if (item.resultStatus === 'graded' || item.status === 'graded') return 'graded';
  if (item.submitStatus === 'submitted' || item.submitStatus === 'done' || item.status === 'done') return 'done';
  return 'progress';
}

function toQuestItem(item: BackendQuestItem): QuestItem {
  return {
    id: Number(item.id),
    title: item.title || 'Quest',
    startsAt: item.startsAt || toDateText(item.startAt),
    endsAt: item.endsAt || toDateText(item.endAt),
    status: toQuestStatus(item),
    description: item.description || [item.type, item.classification].filter(Boolean).join(' · ') || undefined,
    tasks: item.tasks || (item.maxExp ? [`최대 EXP ${item.maxExp}`] : []),
  };
}

function toSurveyQuestions(item: BackendSurveyItem): SurveyItem['questions'] {
  if (item.questions?.length) {
    return item.questions.map((question) => ({
      id: Number(question.id),
      text: question.text || '문항',
      type: question.type || 'long_text',
      optionIds: question.optionIds || question.options?.map((option) => option.id),
      options: question.options?.map((option) => ({
        id: Number(option.id),
        text: option.text,
        displayOrder: option.displayOrder,
      })) || [],
    }));
  }

  const questionCount = item.questionCount && item.questionCount > 0 ? item.questionCount : 1;
  return Array.from({ length: questionCount }, (_, index) => ({
    id: index + 1,
    text: `문항 ${index + 1}`,
    type: 'long_text',
    options: [],
  }));
}

function toSurveyItem(item: BackendSurveyItem): SurveyItem {
  const questions = toSurveyQuestions(item);

  return {
    id: Number(item.id),
    title: item.title || '설문',
    required: Boolean(item.required),
    startsAt: item.startsAt || toDateText(item.startAt),
    endsAt: item.endsAt || toDateText(item.endAt),
    answered: item.answered ?? Boolean(item.completed),
    description: item.description || item.category || undefined,
    questionCount: item.questionCount ?? questions.length,
    questions,
  };
}

function toClassmate(item: BackendClassmate): Classmate {
  return {
    id: Number(item.id),
    name: item.name || 'Learner',
    campusName: item.campusName || '-',
    trackName: item.trackName || '-',
    teamName: item.teamName || item.className || undefined,
    statusMessage: item.statusMessage,
  };
}

export function login(email: string, password: string): Promise<LoginResponse> {
  return fetchJson<LoginResponse>('/api/auth/login', {
    body: JSON.stringify({ email, password }),
    fallback: () => ({
      user: {
        ...mockUser,
        email: email || mockUser.email,
      },
    }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });
}

export function getMe(): Promise<LoginResponse> {
  return fetchJson<LoginResponse>('/api/me', {
    fallback: () => ({ user: mockUser }),
  });
}

export function getCurrentRoleAccess(): Promise<RoleAccess> {
  return fetchJson<RoleAccess>('/api/auth/roles/current', {
    fallback: () => ({
      role: mockUser.role || 'learner',
      permissions: ['dashboard:read', 'profile:update', 'quest:submit'],
      deniedRoutes: ['/admin'],
    }),
  });
}

export function logout(): Promise<{ success: boolean; message: string }> {
  return fetchJson<{ success: boolean; message: string }>('/api/auth/logout', {
    fallback: () => ({ success: true, message: 'logged out locally' }),
    method: 'POST',
  });
}

export function getDashboardSummary(): Promise<DashboardSummary> {
  return fetchJson<BackendDashboardSummary>('/api/dashboard/summary', {
    fallback: () => mockDashboard,
  }).then(toDashboardSummary);
}

export function getAttendanceRecords(): Promise<{ items: AttendanceRecord[] }> {
  return fetchJson<{ items: BackendAttendanceRecord[] }>('/api/attendance/records', {
    fallback: () => ({ items: mockAttendanceRecords }),
  }).then((response) => ({ items: response.items.map(toAttendanceRecord) }));
}

export function getNotifications(): Promise<{ items: NotificationItem[] }> {
  return fetchJson<{ items: BackendNotificationItem[] }>('/api/notifications', {
    fallback: () => ({ items: mockNotifications }),
  }).then((response) => ({ items: response.items.map(toNotificationItem) }));
}

export function getCurriculum(): Promise<{ items: CurriculumWeek[] }> {
  return fetchJson<{ items: BackendCurriculumItem[] }>('/api/learning/curriculum', {
    fallback: () => ({ items: mockCurriculumWeeks }),
  }).then((response) => ({ items: response.items.map(toCurriculumWeek) }));
}

export function getReplays(query: { keyword?: string }): Promise<{ items: ReplayItem[] }> {
  const keyword = query.keyword?.trim().toLowerCase();
  const params = buildQuery({ keyword: query.keyword?.trim() });

  return fetchJson<{ items: BackendReplayItem[] }>(`/api/learning/replays${params}`, {
    fallback: () => ({
      items: mockReplays.filter((replay) => !keyword || replay.title.toLowerCase().includes(keyword)),
    }),
  }).then((response) => ({ items: response.items.map(toReplayItem) }));
}

export function getLearningMaterials(query: { keyword?: string; type?: string }): Promise<{ items: LearningMaterial[] }> {
  const keyword = query.keyword?.trim().toLowerCase();
  const params = buildQuery({ keyword: query.keyword?.trim(), type: query.type });

  return fetchJson<{ items: BackendMaterialItem[] }>(`/api/learning/materials${params}`, {
    fallback: () => ({
      items: mockMaterials.filter((material) => {
        const matchesType = !query.type || material.type === query.type;
        const matchesKeyword = !keyword || material.title.toLowerCase().includes(keyword);
        return matchesType && matchesKeyword;
      }),
    }),
  }).then((response) => ({ items: response.items.map(toLearningMaterial) }));
}

export function getLearningMaterial(id: number): Promise<LearningMaterial | undefined> {
  const fallbackMaterial = mockMaterials.find((material) => material.id === id);

  return fetchJson<ItemResponse<BackendMaterialItem> | undefined>(`/api/learning/materials/${id}`, {
    fallback: () => (fallbackMaterial ? { item: fallbackMaterial } : undefined),
  }).then((response) => (response?.item ? toLearningMaterial(response.item) : undefined));
}

export function getQuests(): Promise<{ items: QuestItem[] }> {
  return fetchJson<{ items: BackendQuestItem[] }>('/api/quests', {
    fallback: () => ({ items: mockQuests }),
  }).then((response) => ({ items: response.items.map(toQuestItem) }));
}

export function getQuest(id: number): Promise<QuestItem | undefined> {
  const fallbackQuest = mockQuests.find((quest) => quest.id === id);

  return fetchJson<ItemResponse<BackendQuestItem> | undefined>(`/api/quests/${id}`, {
    fallback: () => (fallbackQuest ? { item: fallbackQuest } : undefined),
  }).then((response) => (response?.item ? toQuestItem(response.item) : undefined));
}

export function getSurveys(): Promise<{ items: SurveyItem[] }> {
  return fetchJson<{ items: BackendSurveyItem[] }>('/api/surveys', {
    fallback: () => ({ items: mockSurveys }),
  }).then((response) => ({ items: response.items.map(toSurveyItem) }));
}

export function getSurvey(id: number): Promise<SurveyItem | undefined> {
  const fallbackSurvey = mockSurveys.find((survey) => survey.id === id);

  return fetchJson<ItemResponse<BackendSurveyItem> | undefined>(`/api/surveys/${id}`, {
    fallback: () => (fallbackSurvey ? { item: fallbackSurvey } : undefined),
  }).then((response) => (response?.item ? toSurveyItem(response.item) : undefined));
}

export function checkProfilePassword(password: string): Promise<{ verified: boolean }> {
  return fetchJson<{ valid?: boolean; verified?: boolean }>('/api/profile/password-check', {
    body: JSON.stringify({ password }),
    fallback: () => ({ valid: password.length > 0 }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  }).then((response) => ({ verified: Boolean(response.verified ?? response.valid) }));
}

export function getProfile(): Promise<ProfileDetails> {
  return fetchJson<{ profile: ProfileDetails }>('/api/profile', {
    fallback: () => ({
      profile: {
        ...mockUser,
        learnerNo: null,
        className: null,
        zipCode: null,
        addressLine1: null,
        addressLine2: null,
        mobilePhone: null,
        emergencyPhone: null,
        marketingOptIn: false,
      },
    }),
  }).then((response) => response.profile);
}

export function getClassmates(): Promise<{ items: Classmate[] }> {
  return fetchJson<{ items: BackendClassmate[] }>('/api/community/classmates', {
    fallback: () => ({ items: mockClassmates }),
  }).then((response) => ({ items: response.items.map(toClassmate) }));
}

export function createQna(draft: QnaDraft): Promise<{ id: number; title: string }> {
  return fetchJson<ItemResponse<{ id: number; title: string }>>('/api/boards/qna/posts', {
    body: JSON.stringify(draft),
    fallback: () => ({ item: { id: Date.now(), title: draft.title } }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  }).then((response) => response.item);
}

export function createSupportTicket(draft: SupportTicketDraft): Promise<SupportTicketItem> {
  return fetchJson<ItemResponse<SupportTicketItem>>('/api/support/tickets', {
    body: JSON.stringify(draft),
    fallback: () => ({ item: { id: Date.now(), title: draft.title, status: 'open', messageCount: 1 } }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  }).then((response) => response.item);
}

export function getSupportTickets(query: { page?: number; size?: number } = {}): Promise<SupportTicketsResponse> {
  const page = query.page || 1;
  const size = query.size || 20;
  const params = buildQuery({ page, size });

  return fetchJson<SupportTicketsResponse>(`/api/support/tickets${params}`, {
    fallback: () => ({
      items: [],
      page: { page, size, totalItems: 0, totalPages: 0 },
    }),
  });
}

export function getSupportTicket(ticketId: number): Promise<SupportTicketDetail | undefined> {
  return fetchJson<ItemResponse<SupportTicketDetail> | undefined>(`/api/support/tickets/${ticketId}`, {
    fallback: () => undefined,
  }).then((response) => response?.item);
}

export function createSupportTicketMessage(
  ticketId: number,
  draft: SupportTicketMessageDraft,
): Promise<{ item: SupportTicketMessageItem; ticket: SupportTicketItem }> {
  return fetchJson<{ item: SupportTicketMessageItem; ticket: SupportTicketItem }>(`/api/support/tickets/${ticketId}/messages`, {
    body: JSON.stringify(draft),
    fallback: () => ({
      item: {
        id: Date.now(),
        ticketId,
        senderName: '로컬 데모',
        type: 'user_message',
        content: draft.content,
        createdAt: new Date().toISOString(),
      },
      ticket: {
        id: ticketId,
        title: '1:1 문의',
        status: 'open',
        messageCount: 1,
        latestMessageAt: new Date().toISOString(),
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  });
}

export function createFreePost(draft: BoardPostDraft): Promise<{ id: number; title: string }> {
  return fetchJson<ItemResponse<{ id: number; title: string }>>('/api/boards/free/posts', {
    body: JSON.stringify(draft),
    fallback: () => ({ item: { id: Date.now(), title: draft.title } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}

export function submitAttendanceAppeal(draft: AttendanceAppealDraft): Promise<{ id: number; status: string }> {
  return fetchJson<ItemResponse<{ id: number; status: string }>>('/api/attendance/appeals', {
    body: JSON.stringify(draft),
    fallback: () => ({ item: { id: Date.now(), status: 'requested' } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}

export function submitQuest(draft: QuestSubmissionDraft): Promise<{ id: number; status: string }> {
  const payload = {
    content: draft.content,
    attachmentUrl: draft.repositoryUrl?.trim() || undefined,
  };

  return fetchJson<ItemResponse<{ id: number; status: string }>>(`/api/quests/${draft.questId}/submissions`, {
    body: JSON.stringify(payload),
    fallback: () => ({ item: { id: Date.now(), status: 'submitted' } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}

export function respondSurvey(draft: SurveyResponseDraft): Promise<{ id: number; completed: boolean; answerCount: number }> {
  const payload = {
    answers: draft.answers.map((answer) => ({
      questionId: answer.questionId,
      answerText: answer.answerText,
      optionIds: answer.optionIds?.length ? answer.optionIds : undefined,
    })),
  };

  return fetchJson<ItemResponse<{ id: number; completed: boolean; answerCount: number }>>(`/api/surveys/${draft.surveyId}/responses`, {
    body: JSON.stringify(payload),
    fallback: () => ({ item: { id: Date.now(), completed: true, answerCount: draft.answers.length } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}

export function updateProfile(draft: ProfileEditDraft): Promise<{ profile: ProfileDetails }> {
  return fetchJson<{ profile: ProfileDetails }>('/api/profile', {
    body: JSON.stringify(draft),
    fallback: () => ({ profile: { ...mockUser, ...draft } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'PUT',
  });
}

export function sendClassmateNotification(userId: number): Promise<{ status: string }> {
  return fetchJson<{ status?: string; item?: { status?: string } }>(`/api/community/classmates/${userId}/notifications`, {
    body: JSON.stringify({}),
    fallback: () => ({ status: 'sent' }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => ({ status: response.status || response.item?.status || 'sent' }));
}


export function getAdminCampusStructure(): Promise<AdminCampusStructure> {
  return fetchJson<AdminCampusStructure>('/api/admin/campus-structure', {
    fallback: () => ({
      campuses: [{ id: 1, name: '서울', active: true }],
      cohorts: [{ id: 12, name: '12기', year: 2026, active: true }],
      tracks: [{ id: 21, name: 'Java', description: '전공자 Java 트랙', active: true }],
      classes: [{ id: 101, campusId: 1, cohortId: 12, trackId: 21, name: '서울 1반', classroom: 'A101', capacity: 28, active: true }],
    }),
  });
}

export function createAdminClassGroup(draft: AdminClassGroupDraft): Promise<AdminClassGroupItem> {
  return fetchJson<{ item: AdminClassGroupItem }>('/api/admin/campus-structure/classes', {
    body: JSON.stringify(draft),
    fallback: () => ({ item: { id: Date.now(), active: true, ...draft } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => response.item);
}
