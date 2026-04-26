import { fetchJson, getErrorMessage } from './client';

export type ReadinessStatus = 'pass' | 'fail';

export interface ReadinessCheckResult {
  id: string;
  label: string;
  target: string;
  required: boolean;
  status: ReadinessStatus;
  message: string;
}

interface ReadinessCheckDefinition {
  id: string;
  label: string;
  target: string;
  required?: boolean;
  run: () => Promise<string>;
}

type JsonRecord = Record<string, unknown>;

function isRecord(value: unknown): value is JsonRecord {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function hasArrayField(value: unknown, field: string): boolean {
  return isRecord(value) && Array.isArray(value[field]);
}

function hasStatusUp(value: unknown): boolean {
  return isRecord(value) && value.status === 'UP';
}

function isRequiredCheckUp(value: unknown): boolean {
  return isRecord(value) && value.required !== false && value.status === 'UP';
}

function hasOperationalHealth(value: unknown): boolean {
  if (!hasStatusUp(value) || !Array.isArray((value as JsonRecord).checks)) {
    return false;
  }
  const requiredChecks = ((value as JsonRecord).checks as unknown[]).filter((check) => isRecord(check) && check.required !== false);
  return requiredChecks.length > 0 && requiredChecks.every(isRequiredCheckUp);
}

function hasUserProfile(value: unknown): boolean {
  return isRecord(value) && isRecord(value.user) && typeof value.user.email === 'string';
}

function hasAuthenticatedSession(value: unknown): boolean {
  return isRecord(value) && value.authenticated === true && typeof value.secondsRemaining === 'number';
}

async function expectJson(endpoint: string, predicate: (value: unknown) => boolean, passMessage: string): Promise<string> {
  const payload = await fetchJson<unknown>(endpoint);
  if (!predicate(payload)) {
    throw new Error('응답 형식이 운영 smoke 기준과 다릅니다.');
  }
  return passMessage;
}

async function expectText(endpoint: string, expected: string, passMessage: string): Promise<string> {
  const response = await fetch(endpoint, { credentials: 'include' });
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  const body = (await response.text()).trim();
  if (body !== expected) {
    throw new Error(`예상 응답 '${expected}' 대신 '${body}'를 받았습니다.`);
  }
  return passMessage;
}

async function expectBackendHealth(): Promise<string> {
  const payload = await fetchJson<unknown>('/api/health');
  if (!hasOperationalHealth(payload)) {
    throw new Error('Backend health 필수 의존성 점검이 UP이 아닙니다.');
  }
  const checks = isRecord(payload) && Array.isArray(payload.checks) ? payload.checks : [];
  return `Backend API health payload가 UP입니다. 필수 점검 ${checks.length}개를 확인했습니다.`;
}

export const READINESS_CHECKS: ReadinessCheckDefinition[] = [
  {
    id: 'nginx-health',
    label: 'Nginx health',
    target: '/nginx-health',
    run: () => expectText('/nginx-health', 'ok', 'Nginx reverse proxy health endpoint가 정상입니다.'),
  },
  {
    id: 'backend-health',
    label: 'Backend API health',
    target: '/api/health',
    run: expectBackendHealth,
  },
  {
    id: 'actuator-health',
    label: 'Actuator health',
    target: '/actuator/health',
    run: () => expectJson('/actuator/health', hasStatusUp, 'Actuator health payload가 UP입니다.'),
  },
  {
    id: 'session',
    label: 'Session',
    target: '/api/auth/session',
    run: () => expectJson('/api/auth/session', hasAuthenticatedSession, '로그인 세션이 유효합니다.'),
  },
  {
    id: 'profile',
    label: 'Profile',
    target: '/api/me',
    run: () => expectJson('/api/me', hasUserProfile, '현재 사용자 프로필을 조회했습니다.'),
  },
  {
    id: 'attendance',
    label: 'Attendance',
    target: '/api/attendance/records?size=1',
    run: () => expectJson('/api/attendance/records?size=1', (payload) => hasArrayField(payload, 'items'), '출석 목록 API가 응답했습니다.'),
  },
  {
    id: 'notifications',
    label: 'Notifications',
    target: '/api/notifications?size=1',
    run: () => expectJson('/api/notifications?size=1', (payload) => hasArrayField(payload, 'items'), '알림 목록 API가 응답했습니다.'),
  },
  {
    id: 'learning-materials',
    label: 'Learning materials',
    target: '/api/learning/materials?size=1',
    run: () => expectJson('/api/learning/materials?size=1', (payload) => hasArrayField(payload, 'items'), '학습자료 목록 API가 응답했습니다.'),
  },
  {
    id: 'learning-replays',
    label: 'Learning replays',
    target: '/api/learning/replays?size=1',
    run: () => expectJson('/api/learning/replays?size=1', (payload) => hasArrayField(payload, 'items'), '강의 다시보기 목록 API가 응답했습니다.'),
  },
  {
    id: 'board',
    label: 'Board',
    target: '/api/boards/free/posts?size=1',
    run: () => expectJson('/api/boards/free/posts?size=1', (payload) => hasArrayField(payload, 'items'), '게시판 목록 API가 응답했습니다.'),
  },
  {
    id: 'survey',
    label: 'Survey',
    target: '/api/surveys?size=1',
    run: () => expectJson('/api/surveys?size=1', (payload) => hasArrayField(payload, 'items'), '설문 목록 API가 응답했습니다.'),
  },
  {
    id: 'quest',
    label: 'Quest',
    target: '/api/quests?size=1',
    run: () => expectJson('/api/quests?size=1', (payload) => hasArrayField(payload, 'items'), '퀘스트 목록 API가 응답했습니다.'),
  },
  {
    id: 'support',
    label: 'Support',
    target: '/api/support/tickets?size=1',
    run: () => expectJson('/api/support/tickets?size=1', (payload) => hasArrayField(payload, 'items'), '1:1 문의 목록 API가 응답했습니다.'),
  },
];

export async function runReadinessChecks(): Promise<ReadinessCheckResult[]> {
  const results = await Promise.all(READINESS_CHECKS.map(async (check) => {
    try {
      const message = await check.run();
      return {
        id: check.id,
        label: check.label,
        target: check.target,
        required: check.required ?? true,
        status: 'pass' as const,
        message,
      };
    } catch (error) {
      return {
        id: check.id,
        label: check.label,
        target: check.target,
        required: check.required ?? true,
        status: 'fail' as const,
        message: getErrorMessage(error),
      };
    }
  }));

  return results;
}

export function summarizeReadiness(results: ReadinessCheckResult[]) {
  const failedRequired = results.filter((result) => result.required && result.status === 'fail').length;
  return {
    total: results.length,
    passed: results.filter((result) => result.status === 'pass').length,
    failed: results.filter((result) => result.status === 'fail').length,
    ready: results.length > 0 && failedRequired === 0,
  };
}
