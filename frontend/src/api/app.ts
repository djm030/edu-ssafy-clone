import { ApiError, buildQuery, fetchJson } from './client';
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
  AccessPolicyResponse,
  AttendanceAppeal,
  AttendanceRecord,
  AttendanceAppealDraft,
  AttendanceAppealResolveDraft,
  AttendanceRecordFilters,
  BookmarkDraft,
  BookmarkItem,
  BoardPostDraft,
  Classmate,
  CurriculumWeek,
  DashboardSummary,
  DocumentAttachmentItem,
  DocumentRequestItem,
  DocumentSubmissionDraft,
  DocumentSubmissionResult,
  EducationStatusSummary,
  EbookAccessLogResult,
  EbookItem,
  ElearningProgressDetail,
  ElearningProgressItem,
  ElearningResumeResult,
  LearningMaterial,
  LearningMaterialResource,
  LearningMaterialViewResult,
  LiveSessionItem,
  LiveSessionJoinResult,
  LoginResponse,
  MaterialResourceAttachmentDraft,
  MaterialResourceAttachmentResult,
  PledgeAgreementResult,
  PledgeItem,
  RoleAccess,
  NotificationItem,
  NotificationDeleteResult,
  NotificationReadResult,
  NotificationsReadAllResult,
  QnaDraft,
  QuestItem,
  RequiredStudyCompleteResult,
  RequiredStudyItem,
  QuestSubmissionAttachmentDraft,
  QuestSubmissionAttachmentResult,
  QuestSubmissionDraft,
  QuestSubmissionResult,
  ReplayItem,
  ReplayWatchLogResult,
  ProfileEditDraft,
  ProfileDetails,
  ProfilePasswordChangeDraft,
  SupportTicketAttachmentDraft,
  SupportTicketAttachmentItem,
  SupportTicketDraft,
  SupportTicketDetail,
  SupportTicketItem,
  SupportTicketMessageDraft,
  SupportTicketMessageItem,
  SupportTicketsResponse,
  SurveyResponseDraft,
  SurveyCreateDraft,
  SurveySavedResponse,
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
  classDate?: string | null;
  lastWatchedAt?: string | null;
  watchCount?: number | null;
};

type BackendElearningProgressItem = Partial<ElearningProgressItem> & {
  courseId?: number | null;
  id?: number | null;
  lessons?: ElearningProgressDetail['lessons'];
};

type BackendBookmarkItem = Partial<BookmarkItem> & {
  targetType?: string | null;
  targetId?: number | null;
  createdAt?: string | null;
};

type BackendDocumentAttachmentItem = Partial<DocumentAttachmentItem> & {
  submissionId?: number | null;
  requestId?: number | null;
  fileSize?: number | null;
  createdAt?: string | null;
};

type BackendDocumentRequestItem = Partial<DocumentRequestItem> & {
  maxFileSizeBytes?: number | null;
  startsAt?: string | null;
  dueAt?: string | null;
  submittedAt?: string | null;
  reviewedAt?: string | null;
  attachments?: BackendDocumentAttachmentItem[];
};

type BackendPledgeItem = Partial<PledgeItem> & {
  startsAt?: string | null;
  dueAt?: string | null;
  agreedAt?: string | null;
};

type BackendEbookItem = Partial<EbookItem> & {
  createdAt?: string | null;
  lastAccessedAt?: string | null;
  accessCount?: number | null;
};

type BackendRequiredStudyItem = Partial<RequiredStudyItem> & {
  dueAt?: string | null;
  completedAt?: string | null;
};

type BackendLiveSessionItem = Partial<LiveSessionItem> & {
  startsAt?: string | null;
  endsAt?: string | null;
  createdAt?: string | null;
  lastJoinedAt?: string | null;
  joinCount?: number | null;
};

type BackendMaterialResource = {
  id?: number | null;
  materialId?: number | null;
  title?: string | null;
  type?: string | null;
  launchMode?: string | null;
  targetUrl?: string | null;
  displayOrder?: number | null;
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

type BackendSurveySavedResponse = Partial<SurveySavedResponse> & {
  answers?: Array<{
    questionId: number;
    answerText?: string | null;
    optionIds?: number[];
  }>;
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

function filterMockAttendanceRecords(filters: AttendanceRecordFilters): AttendanceRecord[] {
  return mockAttendanceRecords.filter((record) => {
    if (filters.dateFrom && record.date < filters.dateFrom) return false;
    if (filters.dateTo && record.date > filters.dateTo) return false;
    if (filters.status && record.status !== filters.status) return false;
    return true;
  });
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
    date: item.date || toDateText(item.classDate || item.publishedAt),
    duration: item.duration || '-',
    category: item.category || `v${item.versionNo ?? 1}`,
    watched: Boolean(item.watched ?? item.lastWatchedAt),
    versionNo: item.versionNo ?? undefined,
    scope: item.scope || undefined,
    classroom: item.classroom || undefined,
    watchCount: Number(item.watchCount ?? 0),
    lastWatchedAt: item.lastWatchedAt || undefined,
  };
}

function toElearningStatus(status?: string | null): ElearningProgressItem['status'] {
  if (status === 'not_started' || status === 'completed') return status;
  return 'in_progress';
}

function toElearningProgressItem(item: BackendElearningProgressItem): ElearningProgressItem {
  return {
    courseId: Number(item.courseId ?? item.id),
    title: item.title || '이러닝 과정',
    category: item.category || undefined,
    thumbnailUrl: item.thumbnailUrl || undefined,
    provider: item.provider || 'SSAFY e-Learning',
    description: item.description || undefined,
    progressPercent: Math.min(100, Math.max(0, item.progressPercent ?? 0)),
    completedLessons: item.completedLessons ?? 0,
    totalLessons: item.totalLessons ?? 0,
    totalDurationSeconds: item.totalDurationSeconds ?? 0,
    lastLessonTitle: item.lastLessonTitle || undefined,
    lastLearningAt: item.lastLearningAt || undefined,
    status: toElearningStatus(item.status),
    resumeUrl: item.resumeUrl || undefined,
  };
}

function toElearningProgressDetail(item: BackendElearningProgressItem): ElearningProgressDetail {
  return {
    ...toElearningProgressItem(item),
    lessons: (item.lessons || []).map((lesson) => ({
      lessonId: Number(lesson.lessonId),
      lessonNo: Number(lesson.lessonNo),
      title: lesson.title || '차시',
      durationSeconds: Number(lesson.durationSeconds ?? 0),
      completed: Boolean(lesson.completed),
      completedAt: lesson.completedAt || undefined,
    })),
  };
}

function toBookmarkTargetType(value?: string | null): BookmarkItem['targetType'] {
  if (value === 'elearning' || value === 'replay') return value;
  return 'material';
}

function toBookmarkItem(item: BackendBookmarkItem): BookmarkItem {
  return {
    id: Number(item.id),
    targetType: toBookmarkTargetType(item.targetType),
    targetId: Number(item.targetId),
    title: item.title || '찜한 콘텐츠',
    description: item.description || undefined,
    thumbnailUrl: item.thumbnailUrl || undefined,
    targetUrl: item.targetUrl || undefined,
    createdAt: item.createdAt || undefined,
  };
}

function toDocumentStatus(value?: string | null): DocumentRequestItem['status'] {
  if (value === 'submitted' || value === 'rejected' || value === 'approved' || value === 'canceled') return value;
  return 'not_submitted';
}

function toDocumentAttachmentItem(item: BackendDocumentAttachmentItem): DocumentAttachmentItem {
  return {
    id: Number(item.id),
    submissionId: Number(item.submissionId),
    requestId: Number(item.requestId),
    filename: item.filename || '제출 파일',
    storageKey: item.storageKey || undefined,
    mimeType: item.mimeType || undefined,
    fileSize: Number(item.fileSize ?? 0),
    createdAt: item.createdAt || undefined,
  };
}

function toDocumentRequestItem(item: BackendDocumentRequestItem): DocumentRequestItem {
  return {
    id: Number(item.id),
    title: item.title || '서류 제출',
    description: item.description || undefined,
    category: item.category || 'general',
    required: Boolean(item.required),
    allowedExtensions: item.allowedExtensions || '.pdf,.jpg,.jpeg,.png',
    maxFileSizeBytes: Number(item.maxFileSizeBytes ?? 0),
    startsAt: item.startsAt || undefined,
    dueAt: item.dueAt || undefined,
    status: toDocumentStatus(item.status),
    submittedAt: item.submittedAt || undefined,
    reviewedAt: item.reviewedAt || undefined,
    reviewComment: item.reviewComment || undefined,
    attachments: (item.attachments || []).map(toDocumentAttachmentItem),
  };
}

function toPledgeItem(item: BackendPledgeItem): PledgeItem {
  return {
    id: Number(item.id),
    title: item.title || '교육생 서약서',
    content: item.content || '',
    version: item.version || '-',
    required: Boolean(item.required),
    startsAt: item.startsAt || undefined,
    dueAt: item.dueAt || undefined,
    agreed: Boolean(item.agreed),
    agreedAt: item.agreedAt || undefined,
    versionSnapshot: item.versionSnapshot || undefined,
  };
}

function toEbookItem(item: BackendEbookItem): EbookItem {
  return {
    id: Number(item.id),
    title: item.title || 'SSAFY e-book',
    description: item.description || undefined,
    thumbnailUrl: item.thumbnailUrl || undefined,
    category: item.category || undefined,
    externalUrl: item.externalUrl || '#',
    createdAt: item.createdAt || undefined,
    lastAccessedAt: item.lastAccessedAt || undefined,
    accessCount: Number(item.accessCount ?? 0),
  };
}

function toRequiredStudyStatus(value?: string | null): RequiredStudyItem['status'] {
  if (value === 'in_progress' || value === 'completed' || value === 'overdue') return value;
  return 'not_started';
}

function toRequiredStudyItem(item: BackendRequiredStudyItem): RequiredStudyItem {
  return {
    id: Number(item.id),
    title: item.title || '필수학습',
    description: item.description || undefined,
    category: item.category || undefined,
    requiredForTrack: item.requiredForTrack || undefined,
    dueAt: item.dueAt || undefined,
    contentType: item.contentType || 'url',
    contentUrl: item.contentUrl || '#',
    status: toRequiredStudyStatus(item.status),
    progressPercent: Number(item.progressPercent ?? 0),
    completedAt: item.completedAt || undefined,
  };
}

function toLiveSessionStatus(value?: string | null): LiveSessionItem['status'] {
  if (value === 'scheduled' || value === 'ended') return value;
  return 'live';
}

function toLiveSessionItem(item: BackendLiveSessionItem): LiveSessionItem {
  return {
    id: Number(item.id),
    title: item.title || '라이브 강의',
    track: item.track || undefined,
    cohort: item.cohort || undefined,
    classRoom: item.classRoom || undefined,
    startsAt: item.startsAt || '',
    endsAt: item.endsAt || '',
    joinUrl: item.joinUrl || '#',
    status: toLiveSessionStatus(item.status),
    createdAt: item.createdAt || undefined,
    lastJoinedAt: item.lastJoinedAt || undefined,
    joinCount: Number(item.joinCount ?? 0),
  };
}

function toMaterialType(value?: string | null): LearningMaterial['type'] {
  if (value === 'ebook' || value === 'video' || value === 'link') return value;
  return 'file';
}

function toLearningMaterialResource(item: BackendMaterialResource): LearningMaterialResource {
  return {
    id: Number(item.id ?? 0),
    materialId: item.materialId == null ? undefined : Number(item.materialId),
    type: item.type || 'file',
    title: item.title || item.targetUrl || '학습자료 리소스',
    launchMode: item.launchMode || undefined,
    targetUrl: item.targetUrl || undefined,
    displayOrder: item.displayOrder ?? undefined,
  };
}

function toLearningMaterial(item: BackendMaterialItem): LearningMaterial {
  const firstResource = item.resources?.[0];
  const resources = item.resources?.map(toLearningMaterialResource) || [];

  return {
    id: Number(item.id),
    title: item.title || 'Learning Material',
    type: toMaterialType(item.type || item.materialTypeCode),
    authorName: item.authorName || 'SSAFY',
    createdAt: item.createdAt || '-',
    viewCount: item.viewCount ?? 0,
    description: item.description || item.summary || undefined,
    fileName: item.fileName || firstResource?.title || item.detailUrl || firstResource?.targetUrl || undefined,
    likeCount: item.likeCount ?? 0,
    bookmarkCount: item.bookmarkCount ?? 0,
    liked: item.liked ?? false,
    bookmarked: item.bookmarked ?? false,
    resources,
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
    category: item.category || undefined,
    required: Boolean(item.required),
    startsAt: item.startsAt || toDateText(item.startAt),
    endsAt: item.endsAt || toDateText(item.endAt),
    status: item.status || undefined,
    answered: item.answered ?? Boolean(item.completed),
    description: item.description || item.category || undefined,
    questionCount: item.questionCount ?? questions.length,
    questions,
  };
}

function toSurveySavedResponse(item: BackendSurveySavedResponse): SurveySavedResponse {
  return {
    id: Number(item.id),
    surveyId: Number(item.surveyId),
    completed: Boolean(item.completed),
    respondedAt: item.respondedAt,
    answers: (item.answers || []).map((answer) => ({
      questionId: Number(answer.questionId),
      answerText: answer.answerText ?? '',
      optionIds: (answer.optionIds || []).map(Number),
    })),
    demo: item.demo,
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
      permissions: ['dashboard:read', 'attendance:read', 'profile:update', 'quest:submit'],
      deniedRoutes: ['/admin'],
    }),
  });
}

export function getAccessPolicy(): Promise<AccessPolicyResponse> {
  return fetchJson<AccessPolicyResponse>('/api/auth/access-policy', {
    fallback: () => ({
      items: [
        {
          id: 'attendance-appeal-resolve',
          method: 'PATCH',
          pathPattern: '/api/attendance/appeals/{appealId}/resolve',
          allowedRoles: ['coach', 'admin'],
          feature: '출석 이의신청',
          description: '출석 이의신청 처리와 출석 상태 정정은 staff 역할 이상만 수행한다.',
        },
        {
          id: 'support-answer',
          method: 'POST',
          pathPattern: '/api/support/tickets/{ticketId}/answers',
          allowedRoles: ['coach', 'admin'],
          feature: '1:1 문의',
          description: '문의 답변 등록은 지원 담당 staff 역할 이상으로 제한한다.',
        },
      ],
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

export function getEducationStatus(): Promise<EducationStatusSummary> {
  return fetchJson<EducationStatusSummary>('/api/mycampus/education-status');
}

export function getAttendanceRecords(filters: AttendanceRecordFilters = {}): Promise<{ items: AttendanceRecord[] }> {
  const query = buildQuery({
    dateFrom: filters.dateFrom,
    dateTo: filters.dateTo,
    status: filters.status,
  });
  return fetchJson<{ items: BackendAttendanceRecord[] }>(`/api/attendance/records${query}`, {
    fallback: () => ({ items: filterMockAttendanceRecords(filters) }),
  }).then((response) => ({ items: response.items.map(toAttendanceRecord) }));
}

export function getNotifications(): Promise<{ items: NotificationItem[] }> {
  return fetchJson<{ items: BackendNotificationItem[] }>('/api/notifications', {
    fallback: () => ({ items: mockNotifications }),
  }).then((response) => ({ items: response.items.map(toNotificationItem) }));
}

export function markNotificationRead(notificationId: number): Promise<NotificationReadResult> {
  return fetchJson<{ item: BackendNotificationItem; unreadCount: number }>(`/api/notifications/${notificationId}/read`, {
    method: 'PATCH',
    fallback: () => {
      const item = mockNotifications.find((notification) => notification.id === notificationId);
      return {
        item: { ...(item || mockNotifications[0]), id: notificationId, read: true },
        unreadCount: Math.max(0, mockNotifications.filter((notification) => !notification.read && notification.id !== notificationId).length),
      };
    },
  }).then((response) => ({
    item: toNotificationItem(response.item),
    unreadCount: response.unreadCount,
  }));
}

export function markAllNotificationsRead(): Promise<NotificationsReadAllResult> {
  return fetchJson<{ items: BackendNotificationItem[]; unreadCount: number }>('/api/notifications/read-all', {
    method: 'PATCH',
    fallback: () => ({
      items: mockNotifications.map((notification) => ({ ...notification, read: true })),
      unreadCount: 0,
    }),
  }).then((response) => ({
    items: response.items.map(toNotificationItem),
    unreadCount: response.unreadCount,
  }));
}

export function deleteNotification(notificationId: number): Promise<NotificationDeleteResult> {
  return fetchJson<NotificationDeleteResult>(`/api/notifications/${notificationId}`, {
    fallback: () => ({
      id: notificationId,
      deleted: true,
      unreadCount: Math.max(0, mockNotifications.filter((notification) => !notification.read && notification.id !== notificationId).length),
    }),
    method: 'DELETE',
  });
}

export function getCurriculum(): Promise<{ items: CurriculumWeek[] }> {
  return fetchJson<{ items: BackendCurriculumItem[] }>('/api/learning/curriculum', {
    fallback: () => ({ items: mockCurriculumWeeks }),
  }).then((response) => ({ items: response.items.map(toCurriculumWeek) }));
}

export function getReplays(query: { keyword?: string }): Promise<{ items: ReplayItem[] }> {
  return getMyReplays(query);
}

export function getMyReplays(query: { keyword?: string }): Promise<{ items: ReplayItem[] }> {
  const keyword = query.keyword?.trim().toLowerCase();
  const params = buildQuery({ keyword: query.keyword?.trim() });

  return fetchJson<{ items: BackendReplayItem[] }>(`/api/replays/my${params}`, {
    fallback: () => ({
      items: mockReplays.filter((replay) => !keyword || replay.title.toLowerCase().includes(keyword)),
    }),
  }).then((response) => ({ items: response.items.map(toReplayItem) }));
}

export function getAllReplays(query: { keyword?: string }): Promise<{ items: ReplayItem[] }> {
  const keyword = query.keyword?.trim().toLowerCase();
  const params = buildQuery({ keyword: query.keyword?.trim() });

  return fetchJson<{ items: BackendReplayItem[] }>(`/api/replays/all${params}`, {
    fallback: () => ({
      items: mockReplays.filter((replay) => !keyword || replay.title.toLowerCase().includes(keyword)),
    }),
  }).then((response) => ({ items: response.items.map(toReplayItem) }));
}

export function recordReplayWatch(replayId: number): Promise<ReplayWatchLogResult> {
  return fetchJson<{ item: BackendReplayItem; watchLog: ReplayWatchLogResult['watchLog'] }>(`/api/replays/${replayId}/watch-log`, {
    method: 'POST',
  }).then((response) => ({ item: toReplayItem(response.item), watchLog: response.watchLog }));
}

export function getBookmarks(query: { targetType?: string; page?: number; size?: number }): Promise<{ items: BookmarkItem[] }> {
  const params = buildQuery({
    page: query.page,
    size: query.size,
    targetType: query.targetType && query.targetType !== 'all' ? query.targetType : undefined,
  });
  return fetchJson<{ items: BackendBookmarkItem[] }>(`/api/me/bookmarks${params}`)
    .then((response) => ({ items: response.items.map(toBookmarkItem) }));
}

export function createBookmark(draft: BookmarkDraft): Promise<{ item: BookmarkItem }> {
  return fetchJson<ItemResponse<BackendBookmarkItem>>('/api/me/bookmarks', {
    body: JSON.stringify(draft),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => ({ item: toBookmarkItem(response.item) }));
}

export function deleteBookmark(bookmarkId: number): Promise<{ id: number; deleted: boolean }> {
  return fetchJson<{ id: number; deleted: boolean }>(`/api/me/bookmarks/${bookmarkId}`, { method: 'DELETE' });
}

export function getDocumentRequests(query: { page?: number; size?: number } = {}): Promise<{ items: DocumentRequestItem[] }> {
  const params = buildQuery({ page: query.page, size: query.size });
  return fetchJson<{ items: BackendDocumentRequestItem[] }>(`/api/documents/requests${params}`)
    .then((response) => ({ items: response.items.map(toDocumentRequestItem) }));
}

export function getDocumentRequest(requestId: number): Promise<DocumentRequestItem> {
  return fetchJson<ItemResponse<BackendDocumentRequestItem>>(`/api/documents/requests/${requestId}`)
    .then((response) => toDocumentRequestItem(response.item));
}

export function submitDocument(requestId: number, draft: DocumentSubmissionDraft): Promise<DocumentSubmissionResult> {
  return fetchJson<{ item: BackendDocumentRequestItem; submission: DocumentSubmissionResult['submission'] }>(`/api/documents/requests/${requestId}/submissions`, {
    body: JSON.stringify(draft),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => ({
    item: toDocumentRequestItem(response.item),
    submission: {
      ...response.submission,
      status: toDocumentStatus(response.submission.status),
      attachments: (response.submission.attachments || []).map(toDocumentAttachmentItem),
    },
  }));
}

export function cancelDocumentSubmission(requestId: number, submissionId: number): Promise<{ requestId: number; submissionId: number; canceled: boolean }> {
  return fetchJson<{ requestId: number; submissionId: number; canceled: boolean }>(`/api/documents/requests/${requestId}/submissions/${submissionId}`, { method: 'DELETE' });
}

export function getPledges(query: { page?: number; size?: number } = {}): Promise<{ items: PledgeItem[] }> {
  const params = buildQuery({ page: query.page, size: query.size });
  return fetchJson<{ items: BackendPledgeItem[] }>(`/api/pledges${params}`)
    .then((response) => ({ items: response.items.map(toPledgeItem) }));
}

export function getPledge(pledgeId: number): Promise<PledgeItem> {
  return fetchJson<ItemResponse<BackendPledgeItem>>(`/api/pledges/${pledgeId}`)
    .then((response) => toPledgeItem(response.item));
}

export function agreePledge(pledgeId: number): Promise<PledgeAgreementResult> {
  return fetchJson<{ item: BackendPledgeItem; agreement: PledgeAgreementResult['agreement'] }>(`/api/pledges/${pledgeId}/agreements`, {
    body: JSON.stringify({ agreed: true }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => ({ item: toPledgeItem(response.item), agreement: response.agreement }));
}

export function getEbooks(query: { page?: number; size?: number } = {}): Promise<{ items: EbookItem[] }> {
  const params = buildQuery({ page: query.page, size: query.size });
  return fetchJson<{ items: BackendEbookItem[] }>(`/api/ebooks${params}`)
    .then((response) => ({ items: response.items.map(toEbookItem) }));
}

export function getEbook(ebookId: number): Promise<EbookItem> {
  return fetchJson<ItemResponse<BackendEbookItem>>(`/api/ebooks/${ebookId}`)
    .then((response) => toEbookItem(response.item));
}

export function recordEbookAccess(ebookId: number): Promise<EbookAccessLogResult> {
  return fetchJson<{ item: BackendEbookItem; accessLog: EbookAccessLogResult['accessLog'] }>(`/api/ebooks/${ebookId}/access-log`, {
    method: 'POST',
  }).then((response) => ({ item: toEbookItem(response.item), accessLog: response.accessLog }));
}

export function getRequiredStudies(query: { page?: number; size?: number } = {}): Promise<{ items: RequiredStudyItem[] }> {
  const params = buildQuery({ page: query.page, size: query.size });
  return fetchJson<{ items: BackendRequiredStudyItem[] }>(`/api/required-studies${params}`)
    .then((response) => ({ items: response.items.map(toRequiredStudyItem) }));
}

export function getRequiredStudy(studyId: number): Promise<RequiredStudyItem> {
  return fetchJson<ItemResponse<BackendRequiredStudyItem>>(`/api/required-studies/${studyId}`)
    .then((response) => toRequiredStudyItem(response.item));
}

export function completeRequiredStudy(studyId: number): Promise<RequiredStudyCompleteResult> {
  return fetchJson<{ item: BackendRequiredStudyItem }>(`/api/required-studies/${studyId}/complete`, { method: 'POST' })
    .then((response) => ({ item: toRequiredStudyItem(response.item) }));
}

export function getTodayLiveSessions(): Promise<{ items: LiveSessionItem[] }> {
  return fetchJson<{ items: BackendLiveSessionItem[] }>('/api/live-sessions/today')
    .then((response) => ({ items: response.items.map(toLiveSessionItem) }));
}

export function getCurrentLiveSession(): Promise<LiveSessionItem | undefined> {
  return fetchJson<{ item?: BackendLiveSessionItem | null }>('/api/live-sessions/current')
    .then((response) => (response.item ? toLiveSessionItem(response.item) : undefined));
}

export function joinLiveSession(sessionId: number): Promise<LiveSessionJoinResult> {
  return fetchJson<{ item: BackendLiveSessionItem; joinLog: LiveSessionJoinResult['joinLog'] }>(`/api/live-sessions/${sessionId}/join`, {
    method: 'POST',
  }).then((response) => ({ item: toLiveSessionItem(response.item), joinLog: response.joinLog }));
}

export function getElearningProgress(query: {
  keyword?: string;
  status?: string;
  page?: number;
  size?: number;
}): Promise<{ items: ElearningProgressItem[] }> {
  const params = buildQuery({
    keyword: query.keyword?.trim(),
    page: query.page,
    size: query.size,
    status: query.status && query.status !== 'all' ? query.status : undefined,
  });

  return fetchJson<{ items: BackendElearningProgressItem[] }>(`/api/elearning/in-progress${params}`)
    .then((response) => ({ items: response.items.map(toElearningProgressItem) }));
}

export function getElearningProgressDetail(courseId: number): Promise<ElearningProgressDetail | undefined> {
  return fetchJson<ItemResponse<BackendElearningProgressItem>>(`/api/elearning/in-progress/${courseId}`)
    .then((response) => toElearningProgressDetail(response.item))
    .catch((error) => {
      if (error instanceof ApiError && error.status === 404) return undefined;
      throw error;
    });
}

export function resumeElearning(courseId: number): Promise<ElearningResumeResult> {
  return fetchJson<ElearningResumeResult>(`/api/elearning/in-progress/${courseId}/resume`, { method: 'POST' });
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

export function recordLearningMaterialView(id: number): Promise<LearningMaterialViewResult> {
  const fallbackMaterial = mockMaterials.find((material) => material.id === id) || mockMaterials[0] || {
    authorName: 'SSAFY',
    createdAt: '-',
    id,
    title: 'Learning Material',
    type: 'file' as const,
    viewCount: 0,
  };

  return fetchJson<ItemResponse<BackendMaterialItem>>(`/api/learning/materials/${id}/views`, {
    method: 'POST',
    fallback: () => ({
      item: {
        ...fallbackMaterial,
        id,
        viewCount: (fallbackMaterial?.viewCount ?? 0) + 1,
      },
    }),
  }).then((response) => ({ item: toLearningMaterial(response.item) }));
}

export function toggleLearningMaterialReaction(
  id: number,
  type: 'bookmark' | 'like',
  active: boolean,
): Promise<LearningMaterialViewResult> {
  const fallbackMaterial = mockMaterials.find((material) => material.id === id) || mockMaterials[0] || {
    authorName: 'SSAFY',
    createdAt: '-',
    id,
    title: 'Learning Material',
    type: 'file' as const,
    viewCount: 0,
  };

  return fetchJson<ItemResponse<BackendMaterialItem>>(`/api/learning/materials/${id}/reactions/${type}`, {
    method: active ? 'DELETE' : 'POST',
    fallback: () => ({
      item: {
        ...fallbackMaterial,
        id,
        likeCount: type === 'like'
          ? Math.max(0, (fallbackMaterial.likeCount ?? 0) + (active ? -1 : 1))
          : fallbackMaterial.likeCount,
        bookmarkCount: type === 'bookmark'
          ? Math.max(0, (fallbackMaterial.bookmarkCount ?? 0) + (active ? -1 : 1))
          : fallbackMaterial.bookmarkCount,
        liked: type === 'like' ? !active : fallbackMaterial.liked,
        bookmarked: type === 'bookmark' ? !active : fallbackMaterial.bookmarked,
      },
    }),
  }).then((response) => ({ item: toLearningMaterial(response.item) }));
}

export function createMaterialResourceAttachment(
  materialId: number,
  resourceId: number,
  draft: MaterialResourceAttachmentDraft,
): Promise<MaterialResourceAttachmentResult> {
  return fetchJson<MaterialResourceAttachmentResult>(
    `/api/learning/materials/${materialId}/resources/${resourceId}/attachments`,
    {
      body: JSON.stringify(draft),
      headers: { 'Content-Type': 'application/json' },
      method: 'POST',
    },
  );
}

export function materialResourceAttachmentUrl(
  materialId: number,
  resourceId: number,
  attachmentId: number,
): string {
  return `/api/learning/materials/${materialId}/resources/${resourceId}/attachments/${attachmentId}`;
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

export function getQuestSubmission(questId: number): Promise<QuestSubmissionResult | undefined> {
  return fetchJson<ItemResponse<QuestSubmissionResult> | undefined>(`/api/quests/${questId}/submission`, {
    fallback: () => undefined,
  }).then((response) => response?.item);
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

export function getSurveyResponse(id: number): Promise<SurveySavedResponse | undefined> {
  return fetchJson<ItemResponse<BackendSurveySavedResponse>>(`/api/surveys/${id}/responses/current`)
    .then((response) => toSurveySavedResponse(response.item))
    .catch((error) => {
      if (error instanceof ApiError && error.status === 404) return undefined;
      throw error;
    });
}

function surveyPayload(draft: SurveyCreateDraft) {
  return {
    ...draft,
    startAt: draft.startAt || undefined,
    endAt: draft.endAt || undefined,
    questions: draft.questions.map((question) => ({
      type: question.type,
      text: question.text,
      options: question.options?.map((text) => ({ text })).filter((option) => option.text.trim()) || [],
    })),
  };
}

function fallbackSurveyItem(draft: SurveyCreateDraft, id = Date.now()): BackendSurveyItem {
  return {
    id,
    title: draft.title,
    category: draft.category,
    required: draft.required,
    status: draft.status,
    completed: false,
    questionCount: draft.questions.length,
    questions: draft.questions.map((question, index) => ({
      id: index + 1,
      text: question.text,
      type: question.type,
      options: question.options?.map((text, optionIndex) => ({
        id: optionIndex + 1,
        text,
        displayOrder: optionIndex + 1,
      })) || [],
    })),
  };
}

export function createSurvey(draft: SurveyCreateDraft): Promise<SurveyItem> {
  return fetchJson<ItemResponse<BackendSurveyItem>>('/api/surveys', {
    body: JSON.stringify(surveyPayload(draft)),
    fallback: () => ({ item: fallbackSurveyItem(draft) }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => toSurveyItem(response.item));
}

export function updateSurvey(id: number, draft: SurveyCreateDraft): Promise<SurveyItem> {
  return fetchJson<ItemResponse<BackendSurveyItem>>(`/api/surveys/${id}`, {
    body: JSON.stringify(surveyPayload(draft)),
    fallback: () => ({ item: fallbackSurveyItem(draft, id) }),
    headers: { 'Content-Type': 'application/json' },
    method: 'PUT',
  }).then((response) => toSurveyItem(response.item));
}

export function deleteSurvey(id: number): Promise<{ id: number; deleted: boolean; demo?: boolean }> {
  return fetchJson<ItemResponse<{ id: number; deleted: boolean; demo?: boolean }>>(`/api/surveys/${id}`, {
    fallback: () => ({ item: { id, deleted: true, demo: true } }),
    method: 'DELETE',
  }).then((response) => response.item);
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

export function getProfileEditAuthorization(): Promise<{ verified: boolean; verifiedUntil?: string | null; ttlSeconds: number }> {
  return fetchJson<{ verified: boolean; verifiedUntil?: string | null; ttlSeconds: number }>('/api/profile/edit-authorization', {
    fallback: () => ({ verified: true, verifiedUntil: new Date(Date.now() + 10 * 60 * 1000).toISOString(), ttlSeconds: 600 }),
  });
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

export function changeProfilePassword(draft: ProfilePasswordChangeDraft): Promise<{ success: boolean; message: string }> {
  return fetchJson<{ success: boolean; message: string }>('/api/profile/password', {
    body: JSON.stringify(draft),
    fallback: () => ({ success: true, message: '비밀번호가 변경되었습니다.' }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'PATCH',
  });
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
        attachments: [],
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

export function createSupportTicketAnswer(
  ticketId: number,
  draft: SupportTicketMessageDraft,
): Promise<{ item: SupportTicketMessageItem; ticket: SupportTicketItem }> {
  return fetchJson<{ item: SupportTicketMessageItem; ticket: SupportTicketItem }>(`/api/support/tickets/${ticketId}/answers`, {
    body: JSON.stringify(draft),
    fallback: () => ({
      item: {
        id: Date.now(),
        ticketId,
        senderName: '운영진',
        type: 'admin_reply',
        content: draft.content,
        createdAt: new Date().toISOString(),
        attachments: [],
      },
      ticket: {
        id: ticketId,
        title: '1:1 문의',
        status: 'answered',
        messageCount: 1,
        latestMessageAt: new Date().toISOString(),
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  });
}

export function createSupportTicketMessageAttachment(
  ticketId: number,
  messageId: number,
  draft: SupportTicketAttachmentDraft,
): Promise<{ item: SupportTicketAttachmentItem; message: SupportTicketMessageItem }> {
  return fetchJson<{ item: SupportTicketAttachmentItem; message: SupportTicketMessageItem }>(
    `/api/support/tickets/${ticketId}/messages/${messageId}/attachments`,
    {
      body: JSON.stringify(draft),
      fallback: () => {
        const item = {
          id: Date.now(),
          messageId,
          filename: draft.filename,
          mimeType: draft.mimeType || 'application/octet-stream',
          fileSize: Math.ceil((draft.contentBase64.length * 3) / 4),
          createdAt: new Date().toISOString(),
        };
        return {
          item,
          message: {
            id: messageId,
            ticketId,
            senderName: '로컬 데모',
            type: 'user_message',
            content: '',
            createdAt: new Date().toISOString(),
            attachments: [item],
          },
        };
      },
      headers: { 'Content-Type': 'application/json' },
      method: 'POST',
    },
  );
}

export function supportTicketAttachmentDownloadUrl(ticketId: number, messageId: number, attachmentId: number): string {
  return `/api/support/tickets/${ticketId}/messages/${messageId}/attachments/${attachmentId}`;
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

export function getAttendanceAppeals(): Promise<{ items: AttendanceAppeal[] }> {
  return fetchJson<{ items: AttendanceAppeal[] }>('/api/attendance/appeals', {
    fallback: () => ({ items: [] }),
  });
}

export function getPendingAttendanceAppeals(): Promise<{ items: AttendanceAppeal[] }> {
  return fetchJson<{ items: AttendanceAppeal[] }>('/api/attendance/appeals/pending', {
    fallback: () => ({ items: [] }),
  });
}

export function cancelAttendanceAppeal(appealId: number): Promise<AttendanceAppeal> {
  return fetchJson<ItemResponse<AttendanceAppeal>>(`/api/attendance/appeals/${appealId}/cancel`, {
    fallback: () => ({ item: { id: appealId, attendanceRecordId: 0, type: 'status_change', reason: '', status: 'canceled' } }),
    method: 'PATCH',
  }).then((response) => response.item);
}

export function resolveAttendanceAppeal(appealId: number, draft: AttendanceAppealResolveDraft): Promise<AttendanceAppeal> {
  return fetchJson<ItemResponse<AttendanceAppeal>>(`/api/attendance/appeals/${appealId}/resolve`, {
    body: JSON.stringify(draft),
    fallback: () => ({
      item: {
        id: appealId,
        attendanceRecordId: 0,
        type: 'status_change',
        reason: '',
        requestedStatus: draft.resolvedStatus,
        status: draft.status,
        resolvedStatus: draft.status === 'approved' ? draft.resolvedStatus : undefined,
        resolutionComment: draft.comment,
      },
    }),
    headers: { 'Content-Type': 'application/json' },
    method: 'PATCH',
  }).then((response) => response.item);
}

export function submitQuest(draft: QuestSubmissionDraft): Promise<QuestSubmissionResult> {
  const payload = {
    content: draft.content,
    attachmentUrl: draft.repositoryUrl?.trim() || undefined,
  };

  return fetchJson<ItemResponse<QuestSubmissionResult>>(`/api/quests/${draft.questId}/submissions`, {
    body: JSON.stringify(payload),
    fallback: () => ({ item: { id: Date.now(), questId: draft.questId, status: 'submitted', demo: true } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then(async (response) => {
    if (!draft.attachment || response.item.demo) return response.item;
    await createQuestSubmissionAttachment(draft.questId, response.item.id, draft.attachment);
    return response.item;
  });
}

export function createQuestSubmissionAttachment(
  questId: number,
  submissionId: number,
  draft: QuestSubmissionAttachmentDraft,
): Promise<QuestSubmissionAttachmentResult> {
  return fetchJson<QuestSubmissionAttachmentResult>(
    `/api/quests/${questId}/submissions/${submissionId}/attachments`,
    {
      body: JSON.stringify(draft),
      headers: { 'Content-Type': 'application/json' },
      method: 'POST',
    },
  );
}

export function questSubmissionAttachmentUrl(
  questId: number,
  submissionId: number,
  attachmentId: number,
): string {
  return `/api/quests/${questId}/submissions/${submissionId}/attachments/${attachmentId}`;
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

export function sendClassmateNotification(userId: number): Promise<{ id?: number; status: string }> {
  return fetchJson<{ status?: string; item?: { id?: number; status?: string } }>(`/api/community/classmates/${userId}/notifications`, {
    body: JSON.stringify({}),
    fallback: () => ({ status: 'sent' }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  }).then((response) => ({ id: response.item?.id, status: response.status || response.item?.status || 'sent' }));
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
