export type BoardCode = 'notice' | 'free' | 'faq' | 'qna';

export type LoadState = 'loading' | 'refreshing' | 'loaded' | 'empty' | 'error';

export interface ApiErrorPayload {
  error?: {
    code?: string;
    message?: string;
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

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  category: 'notice' | 'learning' | 'quest' | 'survey';
  createdAt: string;
  read: boolean;
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
  comments?: BoardCommentItem[];
}

export interface BoardCommentItem {
  id: number;
  postId: number;
  content: string;
  authorName?: string;
  createdAt?: string;
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
  required: boolean;
  startsAt: string;
  endsAt: string;
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

export interface AttendanceAppealDraft {
  attendanceRecordId: number;
  type: string;
  reason: string;
  requestedStatus?: string;
}

export interface QuestSubmissionDraft {
  questId: number;
  repositoryUrl?: string;
  content: string;
}

export interface SurveyResponseDraft {
  surveyId: number;
  answers: SurveyAnswerDraft[];
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

export interface SupportTicketDraft {
  title: string;
  content: string;
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
