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

test('demo learner can complete core write flows without real EduSSAFY credentials', async ({ page }) => {
  await loginAsDemoLearner(page);

  await test.step('출결 이의신청을 제출한다', async () => {
    await page.goto('/mycampus/attendance/appeals/new');
    await expect(page.getByRole('heading', { name: '출결 소명 신청' })).toBeVisible();
    await page.getByLabel('사유').fill('브라우저 E2E 소명 테스트');
    await page.getByRole('button', { name: '신청', exact: true }).click();
    await expect(page.getByText('출결 소명이 접수되었습니다.')).toBeVisible();
  });

  await test.step('자유게시판 글을 작성한다', async () => {
    await page.goto('/community/free/write');
    await expect(page.getByRole('heading', { name: '자유게시판 글쓰기' })).toBeVisible();
    await page.getByLabel('제목').fill('브라우저 E2E 게시글');
    await page.getByLabel('내용').fill('실제 계정 없이 데모 세션에서 작성 흐름을 검증합니다.');
    await page.getByRole('button', { name: '등록', exact: true }).click();
    await expect(page.getByText('게시글이 등록되었습니다.')).toBeVisible();
  });

  await test.step('게시글 상세에서 댓글을 등록한다', async () => {
    await page.goto('/community/free/201');
    await expect(page.getByRole('heading', { name: '자유게시판 상세' })).toBeVisible();
    await page.getByPlaceholder('댓글을 입력하세요').fill('브라우저 E2E 댓글');
    await page.getByRole('button', { name: '댓글 등록' }).click();
    await expect(page.getByText('댓글이 등록되었습니다.')).toBeVisible();
  });

  await test.step('설문 응답을 저장한다', async () => {
    await page.goto('/survey/1/respond');
    await expect(page.getByRole('heading', { name: '설문 응답' })).toBeVisible();
    const answers = page.locator('textarea');
    await expect(answers.first()).toBeVisible();
    await answers.nth(0).fill('적절했습니다.');
    await answers.nth(1).fill('추가 지원은 없습니다.');
    await page.getByRole('button', { name: '제출', exact: true }).click();
    await expect(page.getByText('설문 응답이 저장되었습니다.')).toBeVisible();
  });

  await test.step('1:1 문의를 등록한다', async () => {
    await page.goto('/help/qna/new');
    await expect(page.getByRole('heading', { name: '1:1 문의 등록' })).toBeVisible();
    await page.getByLabel('제목').fill('브라우저 E2E 문의');
    await page.getByLabel('내용').fill('데모 세션으로 문의 등록 흐름을 검증합니다.');
    await page.getByRole('button', { name: '등록', exact: true }).click();
    await expect(page.getByText('문의가 등록되었습니다.')).toBeVisible();
  });

  await test.step('Quest 제출을 저장한다', async () => {
    await page.goto('/quest/1/submit');
    await expect(page.getByRole('heading', { name: 'Quest 제출' })).toBeVisible();
    await page.getByLabel('저장소 URL').fill('https://git.ssafy.local/demo/quest');
    await page.getByLabel('제출 내용').fill('브라우저 E2E Quest 제출 내용');
    await page.getByRole('button', { name: '제출', exact: true }).click();
    await expect(page.getByText('Quest가 제출되었습니다.')).toBeVisible();
  });
});
