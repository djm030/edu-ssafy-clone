export type BoardCode = 'notice' | 'free' | 'faq' | 'qna';

export type LoadState = 'loading' | 'refreshing' | 'loaded' | 'empty' | 'error';

export interface ApiErrorPayload {
  error?: {
    code?: string;
    message?: string;
    status?: number;
    path?: string;
    requestId?: string;
    timestamp?: string;
  };
}

export interface PageMeta {
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface UserProfile {
  id?: number;
  name: string;
  email: string;
  role?: string;
  campusName: string;
  cohortName: string;
  trackName: string;
}

export interface ProfileDetails extends UserProfile {
  learnerNo?: string | null;
  className?: string | null;
  zipCode?: string | null;
  addressLine1?: string | null;
  addressLine2?: string | null;
  mobilePhone?: string | null;
  emergencyPhone?: string | null;
  marketingOptIn?: boolean;
}

export interface LoginResponse {
  user: UserProfile;
}

export interface RoleAccess {
  role: string;
  permissions: string[];
  deniedRoutes: string[];
}

export interface AccessPolicyItem {
  id: string;
  method: string;
  pathPattern: string;
  allowedRoles: string[];
  feature: string;
  description: string;
}

export interface AccessPolicyResponse {
  items: AccessPolicyItem[];
}

export interface DashboardSummary {
  user: Omit<UserProfile, 'id' | 'email' | 'role'>;
  level: {
    level: number;
    exp: number;
    nextLevelExp: number;
    scholarshipPoints: number;
    rank: number;
  };
  attendance: {
    present: number;
    late: number;
    absent: number;
    appealAvailable: boolean;
  };
  notifications: {
    unreadCount: number;
    latest: string[];
  };
  today: {
    curriculumTitle: string;
    questTitle: string;
    surveyTitle: string;
  };
}

export interface AttendanceRecord {
  id: number;
  date: string;
  status: 'present' | 'late' | 'absent' | 'early_leave' | 'excused';
  checkIn?: string;
  checkOut?: string;
  note?: string;
  appealAvailable?: boolean;
  appealId?: number;
  appealStatus?: string;
  appealRequestedAt?: string;
}

export interface AttendanceRecordFilters {
  dateFrom?: string;
  dateTo?: string;
  status?: AttendanceRecord['status'] | '';
}

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  category: 'notice' | 'learning' | 'quest' | 'survey';
  createdAt: string;
  read: boolean;
}

export interface NotificationReadResult {
  item: NotificationItem;
  unreadCount: number;
}

export interface NotificationsReadAllResult {
  items: NotificationItem[];
  unreadCount: number;
}

export interface NotificationDeleteResult {
  id: number;
  deleted: boolean;
  unreadCount: number;
}

export interface CurriculumWeek {
  id: number;
  week: number;
  title: string;
  period: string;
  lessons: string[];
  status: 'planned' | 'current' | 'done';
}

export interface ReplayItem {
  id: number;
  title: string;
  instructor: string;
  date: string;
  duration: string;
  category: string;
  watched: boolean;
}

export type ElearningProgressStatus = 'not_started' | 'in_progress' | 'completed';

export interface ElearningProgressItem {
  courseId: number;
  title: string;
  category?: string | null;
  thumbnailUrl?: string | null;
  provider?: string | null;
  description?: string | null;
  progressPercent: number;
  completedLessons: number;
  totalLessons: number;
  totalDurationSeconds: number;
  lastLessonTitle?: string | null;
  lastLearningAt?: string | null;
  status: ElearningProgressStatus;
  resumeUrl?: string | null;
}

export interface ElearningLessonItem {
  lessonId: number;
  lessonNo: number;
  title: string;
  durationSeconds: number;
  completed: boolean;
  completedAt?: string | null;
}

export interface ElearningProgressDetail extends ElearningProgressItem {
  lessons: ElearningLessonItem[];
}

export interface ElearningResumeResult {
  item: {
    courseId: number;
    resumeUrl?: string | null;
    resumedAt?: string | null;
    status: ElearningProgressStatus;
  };
}

export type BookmarkTargetType = 'material' | 'elearning' | 'replay';

export interface BookmarkItem {
  id: number;
  targetType: BookmarkTargetType;
  targetId: number;
  title: string;
  description?: string | null;
  thumbnailUrl?: string | null;
  targetUrl?: string | null;
  createdAt?: string | null;
}

export interface BookmarkDraft {
  targetType: BookmarkTargetType;
  targetId: number;
}

export type DocumentSubmissionStatus = 'not_submitted' | 'submitted' | 'rejected' | 'approved' | 'canceled';

export interface DocumentAttachmentItem {
  id: number;
  submissionId: number;
  requestId: number;
  filename: string;
  storageKey?: string | null;
  mimeType?: string | null;
  fileSize: number;
  createdAt?: string | null;
}

export interface DocumentRequestItem {
  id: number;
  title: string;
  description?: string | null;
  category: string;
  required: boolean;
  allowedExtensions: string;
  maxFileSizeBytes: number;
  startsAt?: string | null;
  dueAt?: string | null;
  status: DocumentSubmissionStatus;
  submittedAt?: string | null;
  reviewedAt?: string | null;
  reviewComment?: string | null;
  attachments: DocumentAttachmentItem[];
}

export interface DocumentSubmissionDraft {
  filename: string;
  mimeType?: string;
  contentBase64: string;
}

export interface DocumentSubmissionResult {
  item: DocumentRequestItem;
  submission: {
    id: number;
    requestId: number;
    status: DocumentSubmissionStatus;
    submittedAt?: string | null;
    attachments: DocumentAttachmentItem[];
  };
}

export interface PledgeItem {
  id: number;
  title: string;
  content: string;
  version: string;
  required: boolean;
  startsAt?: string | null;
  dueAt?: string | null;
  agreed: boolean;
  agreedAt?: string | null;
  versionSnapshot?: string | null;
}

export interface PledgeAgreementResult {
  item: PledgeItem;
  agreement: {
    id: number;
    pledgeId: number;
    agreed: boolean;
    agreedAt?: string | null;
    versionSnapshot?: string | null;
  };
}

export interface Classmate {
  id: number;
  name: string;
  campusName: string;
  trackName: string;
  teamName?: string;
  statusMessage?: string;
}

export interface BoardCategory {
  id: number;
  name: string;
  sortOrder?: number;
  postCount?: number;
}

export interface BoardPostListItem {
  id: number;
  boardCode: BoardCode;
  category?: {
    id: number;
    name: string;
  };
  title: string;
  authorName?: string;
  createdAt?: string;
  viewCount?: number;
  commentCount?: number;
  reactionCount?: number;
  bookmarkCount?: number;
  hasAttachment?: boolean;
  isPinned?: boolean;
  isNew?: boolean;
  content?: string;
  authorUserId?: number | null;
  comments?: BoardCommentItem[];
  attachments?: BoardAttachmentItem[];
}

export interface BoardCommentItem {
  id: number;
  postId: number;
  parentCommentId?: number | null;
  content: string;
  authorUserId?: number | null;
  authorName?: string;
  createdAt?: string;
  replies?: BoardCommentItem[];
}

export interface BoardAttachmentItem {
  id: number;
  postId?: number;
  originalFilename: string;
  storageKey?: string | null;
  storedPath?: string | null;
  mimeType?: string | null;
  fileSize?: number | null;
  createdAt?: string;
  demo?: boolean;
}

export interface BoardPostListResponse {
  items: BoardPostListItem[];
  page: PageMeta;
}

export interface BoardScreenConfig {
  boardCode: BoardCode;
  path: string;
  navLabel: string;
  eyebrow: string;
  title: string;
  description: string;
  searchPlaceholder: string;
  emptyMessage: string;
  showWriteAction: boolean;
  showEngagement: boolean;
  writePath?: string;
  writeDisabled?: boolean;
}

export interface LearningMaterial {
  id: number;
  title: string;
  type: 'ebook' | 'video' | 'file' | 'link';
  authorName: string;
  createdAt: string;
  viewCount: number;
  description?: string;
  fileName?: string;
  likeCount?: number;
  bookmarkCount?: number;
  liked?: boolean;
  bookmarked?: boolean;
  resources?: LearningMaterialResource[];
}

export interface LearningMaterialViewResult {
  item: LearningMaterial;
}

export interface LearningMaterialResource {
  id: number;
  materialId?: number;
  type: string;
  title: string;
  launchMode?: string;
  targetUrl?: string;
  displayOrder?: number;
}

export interface MaterialResourceAttachmentDraft {
  filename: string;
  mimeType?: string;
  contentBase64: string;
}

export interface MaterialResourceAttachmentItem {
  id: number;
  resourceId: number;
  materialId: number;
  filename: string;
  storageKey?: string;
  storedPath?: string;
  mimeType?: string;
  fileSize: number;
  checksumSha256?: string;
  createdAt?: string;
}

export interface MaterialResourceAttachmentResult {
  item: MaterialResourceAttachmentItem;
  resource: LearningMaterialResource;
}

export interface QuestItem {
  id: number;
  title: string;
  startsAt: string;
  endsAt: string;
  status: 'progress' | 'done' | 'graded';
  description?: string;
  tasks?: string[];
}

export interface SurveyItem {
  id: number;
  title: string;
  category?: string;
  required: boolean;
  startsAt: string;
  endsAt: string;
  status?: string;
  answered: boolean;
  description?: string;
  questionCount?: number;
  questions: SurveyQuestion[];
}

export interface SurveyQuestion {
  id: number;
  text: string;
  type?: string;
  optionIds?: number[];
  options?: SurveyOption[];
}

export interface SurveyOption {
  id: number;
  text: string;
  displayOrder?: number;
}

export interface SurveyCreateDraft {
  title: string;
  category: string;
  required: boolean;
  status: string;
  startAt?: string;
  endAt?: string;
  questions: SurveyQuestionCreateDraft[];
}

export interface SurveyQuestionCreateDraft {
  type: string;
  text: string;
  options?: string[];
}

export interface QnaDraft {
  categoryId?: number;
  title: string;
  content: string;
}

export interface BoardPostDraft {
  categoryId?: number;
  title: string;
  content: string;
}

export interface BoardAttachmentDraft {
  originalFilename: string;
  storageKey?: string;
  storedPath?: string;
  mimeType?: string;
  fileSize?: number;
  checksumSha256?: string;
  contentBase64?: string;
}

export interface AttendanceAppealDraft {
  attendanceRecordId: number;
  type: string;
  reason: string;
  requestedStatus?: string;
}

export interface AttendanceAppeal {
  id: number;
  attendanceRecordId: number;
  type: string;
  reason: string;
  requestedStatus?: string | null;
  status: string;
  requestedAt?: string | null;
  recordDate?: string | null;
  resolvedStatus?: string | null;
  resolvedAt?: string | null;
  resolutionComment?: string | null;
  resolvedByName?: string | null;
  demo?: boolean;
}

export interface AttendanceAppealResolveDraft {
  status: 'approved' | 'rejected';
  resolvedStatus?: string;
  comment?: string;
}

export interface QuestSubmissionDraft {
  questId: number;
  repositoryUrl?: string;
  content: string;
  attachment?: QuestSubmissionAttachmentDraft;
}

export interface QuestSubmissionResult {
  id: number;
  questId: number;
  status: string;
  submittedAt?: string;
  resultStatus?: string;
  score?: number | null;
  gradedAt?: string | null;
  demo?: boolean;
}

export interface QuestSubmissionAttachmentDraft {
  filename: string;
  mimeType?: string;
  contentBase64: string;
}

export interface QuestSubmissionAttachmentItem {
  id: number;
  questId: number;
  submissionId: number;
  filename: string;
  storageKey?: string;
  storedPath?: string;
  mimeType?: string;
  fileSize: number;
  checksumSha256?: string;
  createdAt?: string;
}

export interface QuestSubmissionAttachmentResult {
  item: QuestSubmissionAttachmentItem;
  submission: QuestSubmissionResult;
}

export interface SurveyResponseDraft {
  surveyId: number;
  answers: SurveyAnswerDraft[];
}

export interface SurveySavedResponse {
  id: number;
  surveyId: number;
  completed: boolean;
  respondedAt?: string;
  answers: SurveySavedAnswer[];
  demo?: boolean;
}

export interface SurveySavedAnswer {
  questionId: number;
  answerText?: string | null;
  optionIds: number[];
}

export interface SurveyAnswerDraft {
  questionId: number;
  answerText: string;
  optionIds?: number[];
}

export interface ProfileEditDraft {
  name: string;
  zipCode?: string;
  addressLine1?: string;
  addressLine2?: string;
  mobilePhone?: string;
  emergencyPhone?: string;
  marketingOptIn?: boolean;
}

export interface ProfilePasswordChangeDraft {
  currentPassword: string;
  newPassword: string;
}

export interface SupportTicketDraft {
  title: string;
  content: string;
}

export interface SupportTicketsResponse {
  items: SupportTicketItem[];
  page: PageMeta;
}

export interface SupportTicketItem {
  id: number;
  title: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
  closedAt?: string | null;
  messageCount?: number;
  latestMessageAt?: string | null;
}

export interface SupportTicketDetail extends SupportTicketItem {
  messages: SupportTicketMessageItem[];
}

export interface SupportTicketMessageItem {
  id: number;
  ticketId: number;
  senderUserId?: number | null;
  senderName?: string | null;
  type: string;
  content: string;
  createdAt?: string;
  attachments?: SupportTicketAttachmentItem[];
}

export interface SupportTicketMessageDraft {
  content: string;
}

export interface SupportTicketAttachmentItem {
  id: number;
  messageId: number;
  filename: string;
  storageKey?: string | null;
  storedPath?: string | null;
  mimeType?: string | null;
  fileSize?: number;
  checksumSha256?: string | null;
  createdAt?: string;
}

export interface SupportTicketAttachmentDraft {
  filename: string;
  mimeType?: string;
  contentBase64: string;
}

export interface AdminCampusStructure {
  campuses: AdminCampusItem[];
  cohorts: AdminCohortItem[];
  tracks: AdminTrackItem[];
  classes: AdminClassGroupItem[];
}

export interface AdminCampusItem { id: number; name: string; active: boolean; }
export interface AdminCohortItem { id: number; name: string; year: number; active: boolean; }
export interface AdminTrackItem { id: number; name: string; description?: string; active: boolean; }
export interface AdminClassGroupItem { id: number; campusId: number; cohortId: number; trackId: number; name: string; classroom?: string; capacity: number; active: boolean; }
export type AdminClassGroupDraft = Omit<AdminClassGroupItem, 'id' | 'active'>;
