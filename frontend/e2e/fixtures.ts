import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';

export async function installApiFixture(page: Page) {
  const timestamp = '2026-04-26T09:00:00.000Z';
  const elearningItem = {
    courseId: 1,
    title: 'Java 심화 이러닝',
    category: 'Java',
    provider: 'SSAFY e-Learning',
    description: '데모 교육생에게 배정된 진행 중 이러닝입니다.',
    progressPercent: 42,
    completedLessons: 5,
    totalLessons: 12,
    totalDurationSeconds: 7200,
    lastLessonTitle: '객체지향 핵심',
    lastLearningAt: timestamp,
    status: 'in_progress',
    resumeUrl: '/mycampus/elearning/1',
  };
  const documentItem = {
    id: 1,
    title: '신분 확인 서류',
    description: '교육생 본인 확인을 위한 데모 제출 요청입니다.',
    category: 'identity',
    required: true,
    allowedExtensions: '.pdf,.png',
    maxFileSizeBytes: 5242880,
    startsAt: timestamp,
    dueAt: '2026-05-01T00:00:00.000Z',
    status: 'not_submitted',
    attachments: [],
  };
  const pledgeItem = {
    id: 1,
    title: '교육생 보안 서약서',
    content: '데모 교육생 보안 서약 내용',
    version: '2026.1',
    required: true,
    startsAt: timestamp,
    dueAt: '2026-05-01T00:00:00.000Z',
    agreed: false,
  };
  const requiredStudyItem = {
    id: 1,
    title: '필수 보안 학습',
    description: '교육생이 완료해야 하는 보안 필수학습입니다.',
    category: 'security',
    requiredForTrack: 'Java',
    dueAt: '2026-05-01T00:00:00.000Z',
    contentType: 'url',
    contentUrl: '#',
    status: 'in_progress',
    progressPercent: 80,
  };
  const liveSessionItem = {
    id: 1,
    title: '알고리즘 라이브',
    track: 'Java',
    cohort: '12기',
    classRoom: '서울 1반',
    startsAt: timestamp,
    endsAt: '2026-04-26T10:00:00.000Z',
    joinUrl: '#',
    status: 'live',
    joinCount: 0,
  };
  const replayItem = {
    id: 1,
    title: '알고리즘 다시보기',
    instructor: '김코치',
    date: '2026-04-25',
    duration: '45분',
    category: 'Java',
    watched: false,
    watchCount: 0,
  };

  await page.route('**/*', async (route) => {
    const url = new URL(route.request().url());
    if (!url.pathname.startsWith('/api/')) {
      return route.continue();
    }

    const json = (body: unknown) => route.fulfill({ contentType: 'application/json', body: JSON.stringify(body) });
    if (url.pathname === '/api/auth/login' || url.pathname === '/api/me') {
      return json({ user: { id: 1, name: '김싸피', email: 'student@ssafy.local', role: 'learner', campusName: '서울', cohortName: '12기', trackName: 'Java' } });
    }
    if (url.pathname === '/api/auth/roles/current') {
      return json({ role: 'learner', permissions: ['learning:read', 'board:write'], deniedRoutes: [] });
    }
    if (url.pathname === '/api/auth/session') {
      return json({ authenticated: true, expiresAt: '2026-12-31T23:59:59.000Z', maxInactiveSeconds: 3600, secondsRemaining: 3600 });
    }
    if (url.pathname === '/api/elearning/in-progress') {
      return json({ items: [elearningItem] });
    }
    if (url.pathname === '/api/elearning/in-progress/1') {
      return json({
        item: {
          ...elearningItem,
          lessons: [
            { lessonId: 1, lessonNo: 1, title: '객체지향 핵심', durationSeconds: 1800, completed: true, completedAt: timestamp },
            { lessonId: 2, lessonNo: 2, title: '컬렉션 실습', durationSeconds: 2400, completed: false },
          ],
        },
      });
    }
    if (url.pathname === '/api/elearning/in-progress/1/resume') {
      return json({ item: { courseId: 1, resumeUrl: '/mycampus/elearning/1', resumedAt: timestamp, status: 'in_progress' } });
    }
    if (url.pathname === '/api/me/bookmarks') {
      return json({ items: [{ id: 1, targetType: 'elearning', targetId: 1, title: 'Java 심화 이러닝', description: '이어볼 학습', targetUrl: '/mycampus/elearning/1', createdAt: timestamp }] });
    }
    if (url.pathname === '/api/me/bookmarks/1') {
      return json({ id: 1, deleted: true });
    }
    if (url.pathname === '/api/documents/requests') {
      return json({ items: [documentItem] });
    }
    if (url.pathname === '/api/documents/requests/1/submissions') {
      return json({
        item: { ...documentItem, status: 'submitted', submittedAt: timestamp, attachments: [{ id: 1, submissionId: 1, requestId: 1, filename: 'proof.pdf', mimeType: 'application/pdf', fileSize: 10, createdAt: timestamp }] },
        submission: { id: 1, requestId: 1, status: 'submitted', submittedAt: timestamp, attachments: [{ id: 1, submissionId: 1, requestId: 1, filename: 'proof.pdf', mimeType: 'application/pdf', fileSize: 10, createdAt: timestamp }] },
      });
    }
    if (url.pathname === '/api/pledges') {
      return json({ items: [pledgeItem] });
    }
    if (url.pathname === '/api/pledges/1/agreements') {
      return json({ item: { ...pledgeItem, agreed: true, agreedAt: timestamp, versionSnapshot: '2026.1' }, agreement: { id: 1, pledgeId: 1, agreed: true, agreedAt: timestamp, versionSnapshot: '2026.1' } });
    }
    if (url.pathname === '/api/required-studies') {
      return json({ items: [requiredStudyItem] });
    }
    if (url.pathname === '/api/required-studies/1/complete') {
      return json({ item: { ...requiredStudyItem, status: 'completed', progressPercent: 100, completedAt: timestamp } });
    }
    if (url.pathname === '/api/live-sessions/today') {
      return json({ items: [liveSessionItem] });
    }
    if (url.pathname === '/api/live-sessions/current') {
      return json({ item: liveSessionItem });
    }
    if (url.pathname === '/api/live-sessions/1/join') {
      return json({ item: { ...liveSessionItem, joinCount: 1, lastJoinedAt: timestamp }, joinLog: { id: 1, sessionId: 1, joinedAt: timestamp } });
    }
    if (url.pathname === '/api/replays/my' || url.pathname === '/api/replays/all') {
      return json({ items: [replayItem] });
    }
    if (url.pathname === '/api/replays/1/watch-log') {
      return json({ item: { ...replayItem, watched: true, watchCount: 1, lastWatchedAt: timestamp }, watchLog: { id: 1, replayId: 1, watchedAt: timestamp } });
    }
    if (url.pathname === '/api/external-services') {
      return json({ items: [
        { code: 'JOB_SSAFY', name: 'JOB SSAFY', url: 'https://job.ssafy.local', description: '채용 공고와 취업 지원 프로그램으로 이동합니다.', enabled: true, accessCount: 0 },
        { code: 'SSAFY_GIT', name: 'SSAFY GIT', url: 'https://git.ssafy.local', description: '프로젝트 저장소와 코드 리뷰 시스템으로 이동합니다.', enabled: true, accessCount: 0 },
        { code: 'MEETING_SSAFY', name: 'Meeting! SSAFY', url: 'https://meeting.ssafy.local', description: '라이브 세션 링크입니다.', enabled: false, accessCount: 0 },
      ] });
    }
    if (url.pathname.endsWith('/access-log')) {
      return json({ item: { code: 'JOB_SSAFY', name: 'JOB SSAFY', url: 'https://job.ssafy.local', accessedAt: '2026-04-26T00:00:00.000Z' } });
    }
    if (/^\/api\/surveys\/\d+\/responses\/current$/.test(url.pathname)) {
      return route.fulfill({ status: 404, contentType: 'application/json', body: JSON.stringify({ error: { message: 'saved survey response not found' } }) });
    }

    return route.fulfill({ status: 503, contentType: 'application/json', body: JSON.stringify({ error: { message: 'fixture fallback' } }) });
  });
}

export async function loginAsDemoLearner(page: Page) {
  await installApiFixture(page);
  await page.goto('/login');
  await page.getByLabel('이메일 또는 아이디').fill('student@ssafy.local');
  await page.getByLabel('비밀번호').fill('password');
  await page.getByRole('button', { name: '로그인' }).click();
  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole('banner').getByRole('button', { name: '외부 서비스' })).toBeVisible();
}

export async function stabilizeVisuals(page: Page) {
  await page.addStyleTag({
    content: `
      *, *::before, *::after {
        animation-delay: 0s !important;
        animation-duration: 0s !important;
        transition-delay: 0s !important;
        transition-duration: 0s !important;
        caret-color: transparent !important;
      }
    `,
  });
}
