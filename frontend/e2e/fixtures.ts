import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';

export async function installApiFixture(page: Page) {
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
