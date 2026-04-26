import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';

type FixtureUser = {
  id: number;
  name: string;
  email: string;
  role: string;
  campusName: string;
  cohortName: string;
  trackName: string;
};

type FixtureOptions = {
  permissions?: string[];
  user?: FixtureUser;
};

const learnerUser: FixtureUser = {
  id: 1,
  name: '김싸피',
  email: 'student@ssafy.local',
  role: 'learner',
  campusName: '서울',
  cohortName: '12기',
  trackName: 'Java',
};

const coachUser: FixtureUser = {
  id: 2,
  name: '박코치',
  email: 'coach@ssafy.local',
  role: 'coach',
  campusName: '서울',
  cohortName: '12기',
  trackName: 'Java',
};

export async function installApiFixture(page: Page, options: FixtureOptions = {}) {
  const currentUser = options.user || learnerUser;
  const permissions = options.permissions || ['learning:read', 'board:write'];
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
    const method = route.request().method();
    const json = (body: unknown) => route.fulfill({ contentType: 'application/json', body: JSON.stringify(body) });
    if (url.pathname === '/nginx-health') {
      return route.fulfill({ contentType: 'text/plain', body: 'ok' });
    }
    if (url.pathname === '/actuator/health') {
      return json({ status: 'UP' });
    }
    if (url.pathname === '/actuator/metrics') {
      return json({ names: ['application.started.time', 'http.server.requests', 'jvm.memory.used'] });
    }
    if (url.pathname === '/actuator/prometheus') {
      return route.fulfill({
        contentType: 'text/plain; version=0.0.4',
        body: '# HELP jvm_memory_used_bytes The amount of used memory\n# TYPE jvm_memory_used_bytes gauge\njvm_memory_used_bytes{area="heap",id="G1 Eden Space"} 1.0\n# HELP http_server_requests_seconds_count HTTP server request count\n# TYPE http_server_requests_seconds_count counter\nhttp_server_requests_seconds_count{method="GET",uri="/api/readiness",status="200"} 1.0\n',
      });
    }
    if (!url.pathname.startsWith('/api/')) {
      return route.continue();
    }

    if (url.pathname === '/api/auth/login' || url.pathname === '/api/me') {
      return json({ user: currentUser });
    }
    if (url.pathname === '/api/auth/roles/current') {
      return json({ role: currentUser.role, permissions, deniedRoutes: [] });
    }
    if (url.pathname === '/api/auth/session') {
      return json({ authenticated: true, expiresAt: '2026-12-31T23:59:59.000Z', maxInactiveSeconds: 3600, secondsRemaining: 3600 });
    }
    if (url.pathname === '/api/readiness') {
      return json({
        status: 'UP',
        checkedAt: timestamp,
        service: 'edussafy-backend',
        profile: 'e2e',
        checks: [
          { name: 'database', status: 'UP', required: true, message: 'fixture database ready' },
          { name: 'temp-storage', status: 'UP', required: true, message: 'fixture storage ready' },
        ],
      });
    }
    if (url.pathname === '/api/auth/access-policy') {
      return json({
        items: [
          { id: 'classmate-notification', feature: '우리반 알림', method: 'POST', pathPattern: '/api/community/classmates/{id}/notifications', allowedRoles: ['coach', 'admin'] },
        ],
      });
    }
    if (url.pathname === '/api/attendance/records') {
      return json({
        items: [{
          id: 1,
          date: '2026-04-26',
          status: 'late',
          checkIn: '09:17',
          checkOut: '18:00',
          appealAvailable: true,
        }],
      });
    }
    if (url.pathname === '/api/attendance/appeals' && method === 'POST') {
      return json({ item: { id: 9001, status: 'requested' } });
    }
    if (url.pathname === '/api/attendance/appeals') {
      return json({ items: [] });
    }
    if (url.pathname === '/api/notifications') {
      return json({ items: [{ id: 1, title: '운영 smoke 알림', read: false, createdAt: timestamp }] });
    }
    if (url.pathname === '/api/learning/materials') {
      return json({ items: [{ id: 1, title: '운영 smoke 학습자료', category: 'Java' }] });
    }
    if (url.pathname === '/api/learning/replays') {
      return json({ items: [replayItem] });
    }
    if (url.pathname === '/api/boards/free/posts' && method === 'POST') {
      return json({ item: { id: 201, boardCode: 'free', title: '브라우저 E2E 게시글', content: '실제 계정 없이 데모 세션에서 작성 흐름을 검증합니다.', authorName: '김싸피', createdAt: timestamp } });
    }
    if (url.pathname === '/api/boards/free/posts') {
      return json({ items: [{ id: 201, boardCode: 'free', title: '운영 smoke 게시글', authorName: '김싸피', createdAt: timestamp }], page: { page: 1, size: 1, totalItems: 1, totalPages: 1 } });
    }
    if (url.pathname === '/api/boards/free/posts/201') {
      return json({ post: { id: 201, boardCode: 'free', title: '운영 smoke 게시글', content: '게시글 상세 본문', authorName: '김싸피', createdAt: timestamp, viewCount: 3, commentCount: 0, reactionCount: 0, bookmarkCount: 0, comments: [], attachments: [] } });
    }
    if (url.pathname === '/api/boards/free/posts/201/comments') {
      return json({ item: { id: 301, postId: 201, content: '브라우저 E2E 댓글', authorName: '김싸피', createdAt: timestamp, replies: [] } });
    }
    if (url.pathname === '/api/surveys') {
      return json({ items: [{ id: 1, title: '운영 smoke 설문', status: 'open', questionCount: 2, questions: [{ id: 1, text: '만족도', type: 'long_text' }, { id: 2, text: '추가 의견', type: 'long_text' }] }] });
    }
    if (url.pathname === '/api/surveys/1') {
      return json({ item: { id: 1, title: '운영 smoke 설문', status: 'open', questionCount: 2, questions: [{ id: 1, text: '만족도', type: 'long_text' }, { id: 2, text: '추가 의견', type: 'long_text' }] } });
    }
    if (url.pathname === '/api/surveys/1/responses') {
      return json({ item: { id: 501, completed: true, answerCount: 2 } });
    }
    if (url.pathname === '/api/quests') {
      return json({ items: [{ id: 1, title: '운영 smoke Quest', status: 'open', dueAt: '2026-05-01T00:00:00.000Z' }] });
    }
    if (url.pathname === '/api/quests/1') {
      return json({ item: { id: 1, title: '운영 smoke Quest', status: 'open', description: 'Quest 제출 smoke', dueAt: '2026-05-01T00:00:00.000Z' } });
    }
    if (url.pathname === '/api/quests/1/submission') {
      return route.fulfill({ status: 404, contentType: 'application/json', body: JSON.stringify({ error: { message: 'quest submission not found' } }) });
    }
    if (url.pathname === '/api/quests/1/submissions') {
      return json({ item: { id: 701, questId: 1, status: 'submitted', demo: true } });
    }
    if (url.pathname === '/api/support/tickets' && method === 'POST') {
      return json({ item: { id: 801, title: '브라우저 E2E 문의', status: 'open', messageCount: 1, latestMessageAt: timestamp } });
    }
    if (url.pathname === '/api/support/tickets') {
      return json({ items: [{ id: 1, title: '운영 smoke 문의', status: 'waiting' }] });
    }
    if (url.pathname === '/api/community/classmates') {
      return json({
        items: [
          { id: 7, name: '이교육생', email: 'classmate@ssafy.local', role: 'learner', memberRole: 'learner', campusName: '서울', cohortName: '12기', trackName: 'Java', className: '서울 1반', statusMessage: '알고리즘 스터디 모집 중' },
          { id: 8, name: '박코치', email: 'coach@ssafy.local', role: 'coach', memberRole: 'coach', campusName: '서울', cohortName: '12기', trackName: 'Java', className: '서울 1반', statusMessage: '질문 환영' },
        ],
        summary: { totalCount: 2, learnerCount: 1, coachCount: 1, staffCount: 0 },
      });
    }
    if (url.pathname === '/api/community/classmates/7/notifications') {
      return json({ status: 'sent', item: { id: 77, status: 'sent' } });
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

export async function loginAsDemoCoach(page: Page) {
  await installApiFixture(page, {
    permissions: ['dashboard:read', 'notifications:send', 'learning:manage'],
    user: coachUser,
  });
  await page.goto('/login');
  await page.getByLabel('이메일 또는 아이디').fill('coach@ssafy.local');
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
