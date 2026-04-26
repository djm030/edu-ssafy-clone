import { expect, test } from '@playwright/test';

async function mockApi(page: import('@playwright/test').Page) {
  await page.route('**/*', async (route) => {
    const url = new URL(route.request().url());
    if (!url.pathname.startsWith('/api/')) {
      return route.continue();
    }
    const json = (body: unknown) => route.fulfill({ contentType: 'application/json', body: JSON.stringify(body) });
    if (url.pathname === '/api/auth/login' || url.pathname === '/api/me') {
      return json({ user: { id: 1, name: '김싸피', email: 'student@ssafy.com', role: 'learner', campusName: '서울', cohortName: '12기', trackName: 'Java' } });
    }
    if (url.pathname === '/api/auth/roles/current') {
      return json({ role: 'learner', permissions: ['learning:read'], deniedRoutes: [] });
    }
    if (url.pathname === '/api/auth/session') {
      return json({ authenticated: true, expiresAt: new Date(Date.now() + 3600_000).toISOString(), maxInactiveSeconds: 3600, secondsRemaining: 3600 });
    }
    if (url.pathname === '/api/external-services') {
      return json({ items: [
        { code: 'JOB_SSAFY', name: 'JOB SSAFY', url: 'https://job.ssafy.local', description: '채용 공고와 취업 지원 프로그램으로 이동합니다.', enabled: true, accessCount: 0 },
        { code: 'SSAFY_GIT', name: 'SSAFY GIT', url: 'https://git.ssafy.local', description: '프로젝트 저장소와 코드 리뷰 시스템으로 이동합니다.', enabled: true, accessCount: 0 },
        { code: 'MEETING_SSAFY', name: 'Meeting! SSAFY', url: 'https://meeting.ssafy.local', description: '라이브 세션 링크입니다.', enabled: false, accessCount: 0 },
      ] });
    }
    if (url.pathname.endsWith('/access-log')) {
      return json({ item: { code: 'JOB_SSAFY', name: 'JOB SSAFY', url: 'https://job.ssafy.local', accessedAt: new Date().toISOString() } });
    }
    return route.fulfill({ status: 503, contentType: 'application/json', body: JSON.stringify({ error: { message: 'fixture fallback' } }) });
  });
}

async function login(page: import('@playwright/test').Page) {
  await mockApi(page);
  await page.goto('/login');
  await page.getByLabel('이메일 또는 아이디').fill('student@ssafy.com');
  await page.getByLabel('비밀번호').fill('password');
  await page.getByRole('button', { name: '로그인' }).click();
  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole('banner').getByRole('button', { name: '외부 서비스' })).toBeVisible();
}

test('demo learner can log in and navigate priority screens', async ({ page }) => {
  await login(page);

  const routes = [
    ['출석현황', '/mycampus/attendance', /출석/],
    ['게시판', '/community/free', /자유게시판/],
    ['설문', '/survey', /설문/],
    ['1:1 문의', '/help/qna', /문의/],
    ['학습자료', '/learning/materials', /학습자료/],
    ['Quest', '/quest', /Quest|퀘스트/],
    ['운영 readiness', '/ops/readiness', /운영/],
    ['간담회 신청', '/mentoring/meetings', /간담회 신청/],
    ['간담회 정보', '/mentoring/meeting-results', /간담회 정보/],
    ['외부 서비스', '/external-services', /외부 서비스/],
  ] as const;

  for (const [label, route, heading] of routes) {
    await test.step(label, async () => {
      await page.goto(route);
      await expect(page.getByRole('main').first()).toContainText(heading);
      await expect(page.locator('.inline-alert, .access-state')).toHaveCount(0);
    });
  }
});

test('external service links expose enabled and disabled states without credentials', async ({ page }) => {
  await login(page);
  await page.goto('/external-services');

  await expect(page.getByRole('heading', { name: 'JOB SSAFY' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'SSAFY GIT' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Meeting! SSAFY' })).toBeVisible();
  await expect(page.getByRole('button', { name: '새 창으로 열기' }).first()).toBeEnabled();
  await expect(page.getByText('비활성')).toBeVisible();
});
