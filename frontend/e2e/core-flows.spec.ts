import { expect, test } from '@playwright/test';
import { loginAsDemoLearner } from './fixtures';

test('demo learner can log in and navigate priority screens', async ({ page }) => {
  await loginAsDemoLearner(page);

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
  await loginAsDemoLearner(page);
  await page.goto('/external-services');

  await expect(page.getByRole('heading', { name: 'JOB SSAFY' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'SSAFY GIT' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Meeting! SSAFY' })).toBeVisible();
  await expect(page.getByRole('button', { name: '새 창으로 열기' }).first()).toBeEnabled();
  await expect(page.getByText('비활성')).toBeVisible();
});
