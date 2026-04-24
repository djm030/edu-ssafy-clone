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
  AttendanceRecord,
  AttendanceAppealDraft,
  BoardPostDraft,
  Classmate,
  CurriculumWeek,
  DashboardSummary,
  LearningMaterial,
  LoginResponse,
  NotificationItem,
  QnaDraft,
  QuestItem,
  QuestSubmissionDraft,
  ReplayItem,
  ProfileEditDraft,
  SurveyResponseDraft,
  SurveyItem,
  UserProfile,
} from '../types';

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

export function getDashboardSummary(): Promise<DashboardSummary> {
  return fetchJson<DashboardSummary>('/api/dashboard/summary', {
    fallback: () => mockDashboard,
  });
}

export function getAttendanceRecords(): Promise<{ items: AttendanceRecord[] }> {
  return fetchJson<{ items: AttendanceRecord[] }>('/api/attendance/records', {
    fallback: () => ({ items: mockAttendanceRecords }),
  });
}

export function getNotifications(): Promise<{ items: NotificationItem[] }> {
  return fetchJson<{ items: NotificationItem[] }>('/api/notifications', {
    fallback: () => ({ items: mockNotifications }),
  });
}

export function getCurriculum(): Promise<{ items: CurriculumWeek[] }> {
  return fetchJson<{ items: CurriculumWeek[] }>('/api/learning/curriculum', {
    fallback: () => ({ items: mockCurriculumWeeks }),
  });
}

export function getReplays(query: { keyword?: string }): Promise<{ items: ReplayItem[] }> {
  const keyword = query.keyword?.trim().toLowerCase();
  const params = buildQuery({ keyword: query.keyword?.trim() });

  return fetchJson<{ items: ReplayItem[] }>(`/api/learning/replays${params}`, {
    fallback: () => ({
      items: mockReplays.filter((replay) => !keyword || replay.title.toLowerCase().includes(keyword)),
    }),
  });
}

export function getLearningMaterials(query: { keyword?: string; type?: string }): Promise<{ items: LearningMaterial[] }> {
  const keyword = query.keyword?.trim().toLowerCase();
  const params = buildQuery({ keyword: query.keyword?.trim(), type: query.type });

  return fetchJson<{ items: LearningMaterial[] }>(`/api/learning/materials${params}`, {
    fallback: () => ({
      items: mockMaterials.filter((material) => {
        const matchesType = !query.type || material.type === query.type;
        const matchesKeyword = !keyword || material.title.toLowerCase().includes(keyword);
        return matchesType && matchesKeyword;
      }),
    }),
  });
}

export function getLearningMaterial(id: number): Promise<LearningMaterial | undefined> {
  return fetchJson<LearningMaterial | undefined>(`/api/learning/materials/${id}`, {
    fallback: () => mockMaterials.find((material) => material.id === id),
  });
}

export function getQuests(): Promise<{ items: QuestItem[] }> {
  return fetchJson<{ items: QuestItem[] }>('/api/quests', {
    fallback: () => ({ items: mockQuests }),
  });
}

export function getQuest(id: number): Promise<QuestItem | undefined> {
  return fetchJson<QuestItem | undefined>(`/api/quests/${id}`, {
    fallback: () => mockQuests.find((quest) => quest.id === id),
  });
}

export function getSurveys(): Promise<{ items: SurveyItem[] }> {
  return fetchJson<{ items: SurveyItem[] }>('/api/surveys', {
    fallback: () => ({ items: mockSurveys }),
  });
}

export function getSurvey(id: number): Promise<SurveyItem | undefined> {
  return fetchJson<SurveyItem | undefined>(`/api/surveys/${id}`, {
    fallback: () => mockSurveys.find((survey) => survey.id === id),
  });
}

export function checkProfilePassword(password: string): Promise<{ verified: boolean }> {
  return fetchJson<{ verified: boolean }>('/api/profile/password-check', {
    body: JSON.stringify({ password }),
    fallback: () => ({ verified: password.length > 0 }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });
}

export function getClassmates(): Promise<{ items: Classmate[] }> {
  return fetchJson<{ items: Classmate[] }>('/api/community/classmates', {
    fallback: () => ({ items: mockClassmates }),
  });
}

export function createQna(draft: QnaDraft): Promise<{ id: number; title: string }> {
  return fetchJson<{ id: number; title: string }>('/api/boards/qna/posts', {
    body: JSON.stringify(draft),
    fallback: () => ({ id: Date.now(), title: draft.title }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });
}

export function createFreePost(draft: BoardPostDraft): Promise<{ id: number; title: string }> {
  return fetchJson<{ id: number; title: string }>('/api/boards/free/posts', {
    body: JSON.stringify(draft),
    fallback: () => ({ id: Date.now(), title: draft.title }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  });
}

export function submitAttendanceAppeal(draft: AttendanceAppealDraft): Promise<{ id: number; status: string }> {
  return fetchJson<{ id: number; status: string }>('/api/attendance/appeals', {
    body: JSON.stringify(draft),
    fallback: () => ({ id: Date.now(), status: '접수' }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  });
}

export function submitQuest(draft: QuestSubmissionDraft): Promise<{ id: number; status: string }> {
  return fetchJson<{ id: number; status: string }>(`/api/quests/${draft.questId}/submissions`, {
    body: JSON.stringify(draft),
    fallback: () => ({ id: Date.now(), status: '제출완료' }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  });
}

export function respondSurvey(draft: SurveyResponseDraft): Promise<{ id: number; status: string }> {
  return fetchJson<{ id: number; status: string }>(`/api/surveys/${draft.surveyId}/responses`, {
    body: JSON.stringify(draft),
    fallback: () => ({ id: Date.now(), status: '응답완료' }),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  });
}

export function updateProfile(draft: ProfileEditDraft): Promise<{ user: UserProfile }> {
  return fetchJson<{ user: UserProfile }>('/api/profile', {
    body: JSON.stringify(draft),
    fallback: () => ({ user: { ...mockUser, ...draft } }),
    headers: { 'Content-Type': 'application/json' },
    method: 'PUT',
  });
}
