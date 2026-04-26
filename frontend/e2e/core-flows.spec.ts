import { expect, test } from '@playwright/test';
import { loginAsDemoCoach, loginAsDemoLearner } from './fixtures';

test('demo learner can log in and navigate priority screens', async ({ page }) => {
  await loginAsDemoLearner(page);

  const routes = [
    ['출석현황', '/mycampus/attendance', /출석/],
    ['게시판', '/community/free', /자유게시판/],
    ['설문', '/survey', /설문/],
    ['1:1 문의', '/help/qna', /문의/],
    ['FAQ', '/help/faq', /FAQ/],
    ['학습자료', '/learning/materials', /학습자료/],
    ['커리큘럼 상세', '/learning/curriculum/1', /주차별 커리큘럼|일자별 시간표/],
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
      if (route === '/ops/readiness') {
        await expect(page.getByRole('cell', { name: 'Actuator metrics', exact: true })).toBeVisible();
        await expect(page.getByRole('cell', { name: 'Prometheus metrics', exact: true })).toBeVisible();
        await expect(page.getByText('READY')).toBeVisible();
      }
      if (route === '/help/faq') {
        await expect(page.locator('[aria-label="FAQ 목록"]')).toBeVisible();
        await page.locator('[aria-label="FAQ 목록"] summary').first().click();
        await expect(page.getByText('출결 소명은 발생일 기준')).toBeVisible();
      }
      await expect(page.locator('.inline-alert, .access-state')).toHaveCount(0);
    });
  }

  await test.step('공지/FAQ 상세는 운영자 관리 읽기 전용으로 노출한다', async () => {
    await page.goto('/help/faq/301');
    await expect(page.getByRole('heading', { name: 'FAQ 상세' })).toBeVisible();
    await expect(page.getByText('공지/FAQ는 운영자가 관리하는 읽기 전용 콘텐츠입니다.')).toBeVisible();
    await expect(page.getByRole('button', { name: '수정' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: '삭제' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: '댓글 등록' })).toHaveCount(0);
  });
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

test('learner classmate screen blocks staff-only notification actions', async ({ page }) => {
  await loginAsDemoLearner(page);
  await page.goto('/community/classmates');

  await expect(page.getByRole('heading', { name: '우리반 보기' })).toBeVisible();
  await expect(page.getByText('알림 보내기는 코치 또는 관리자 권한으로만 사용할 수 있습니다.')).toBeVisible();
  await expect(page.getByLabel('알림 메시지')).toBeDisabled();
  await expect(page.getByRole('button', { name: '알림 권한 없음' }).first()).toBeDisabled();
});

test('coach can send classmate notification from browser flow', async ({ page }) => {
  await loginAsDemoCoach(page);
  await page.goto('/community/classmates');

  await expect(page.getByRole('heading', { name: '우리반 보기' })).toBeVisible();
  await expect(page.getByLabel('알림 메시지')).toBeEnabled();
  await page.getByLabel('알림 메시지').fill('오늘 라이브 입장 알림입니다.');
  await page.getByRole('button', { name: '알림 보내기' }).first().click();
  await expect(page.getByText('이교육생님에게 알림을 보냈습니다.')).toBeVisible();
});

test('demo learner can complete mycampus learning actions without real credentials', async ({ page }) => {
  await loginAsDemoLearner(page);

  await test.step('이러닝 이어보기 이력을 저장한다', async () => {
    await page.goto('/mycampus/elearning');
    await expect(page.getByRole('heading', { name: '학습중 이러닝' })).toBeVisible();
    await page.getByRole('button', { name: '이어보기' }).first().click();
    await expect(page).toHaveURL(/\/mycampus\/elearning\/1$/);
    await expect(page.getByRole('heading', { name: '학습중 이러닝 상세' })).toBeVisible();
  });

  await test.step('찜한 항목을 해제한다', async () => {
    await page.goto('/mycampus/bookmarks');
    await expect(page.getByRole('heading', { name: '찜한 목록' })).toBeVisible();
    await page.getByRole('button', { name: '찜 해제' }).first().click();
    await expect(page.getByText('찜이 해제되었습니다.')).toBeVisible();
  });

  await test.step('서류 파일을 제출한다', async () => {
    await page.goto('/mycampus/documents');
    await expect(page.getByRole('heading', { name: '서류제출' })).toBeVisible();
    const fileInput = page.locator('input[type="file"]').first();
    await fileInput.setInputFiles({
      name: 'proof.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('demo document proof'),
    });
    await expect(fileInput).toHaveValue(/proof\.pdf/);
    await page.waitForTimeout(100);
    await page.locator('.list-card .primary-action').first().click();
    await expect(page.getByText('서류 제출이 완료되었습니다.')).toBeVisible();
  });

  await test.step('교육생 서약서에 동의한다', async () => {
    await page.goto('/mycampus/pledges');
    await expect(page.getByRole('heading', { name: '교육생 서약서' })).toBeVisible();
    await page.getByLabel(/서약 내용을 확인했고 동의합니다/).first().check();
    await page.getByRole('button', { name: '동의 제출' }).first().click();
    await expect(page.getByText('서약 동의가 저장되었습니다.')).toBeVisible();
  });

  await test.step('필수학습 이수 상태를 저장한다', async () => {
    await page.goto('/learning/required-studies');
    await expect(page.getByRole('heading', { name: '필수학습', exact: true })).toBeVisible();
    await page.getByRole('button', { name: '이수 처리' }).click();
    await expect(page.getByText('이수 처리가 완료되었습니다.')).toBeVisible();
  });

  await test.step('라이브 입장 기록을 저장한다', async () => {
    await page.goto('/learning/live');
    await expect(page.getByRole('heading', { name: '라이브 바로가기' })).toBeVisible();
    await page.getByRole('button', { name: '입장' }).first().click();
    await expect(page.getByText('입장 기록을 저장했습니다.')).toBeVisible();
  });

  await test.step('내강의 다시보기 시청 기록을 저장한다', async () => {
    await page.goto('/learning/replays/my');
    await expect(page.getByRole('heading', { name: '내강의 다시보기' })).toBeVisible();
    await page.getByRole('button', { name: '시청 기록' }).first().click();
    await expect(page.getByText('시청 기록을 저장했습니다.')).toBeVisible();
  });
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
