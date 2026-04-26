import { expect, test } from '@playwright/test';
import { loginAsDemoLearner, stabilizeVisuals } from './fixtures';

type VisualTarget = {
  name: string;
  path: string;
  text: RegExp;
};

const targets: VisualTarget[] = [
  { name: 'login', path: '/login', text: /로그인/ },
  { name: 'dashboard', path: '/', text: /오늘의 학습|대시보드|SSAFY/ },
  { name: 'attendance', path: '/mycampus/attendance', text: /출석/ },
  { name: 'level', path: '/mycampus/level', text: /레벨|장학/ },
  { name: 'elearning', path: '/mycampus/elearning', text: /이러닝|학습중/ },
  { name: 'bookmarks', path: '/mycampus/bookmarks', text: /찜한|북마크/ },
  { name: 'documents', path: '/mycampus/documents', text: /서류/ },
  { name: 'pledges', path: '/mycampus/pledges', text: /서약서/ },
  { name: 'ebooks', path: '/mycampus/ebooks', text: /e-book|SSAFY e-book/ },
  { name: 'education-status', path: '/mycampus/education-status', text: /교육현황/ },
  { name: 'curriculum', path: '/learning/curriculum', text: /커리큘럼/ },
  { name: 'quest', path: '/quest', text: /Quest|퀘스트/ },
  { name: 'materials', path: '/learning/materials', text: /학습자료/ },
  { name: 'survey', path: '/survey', text: /설문/ },
  { name: 'free-board', path: '/community/free', text: /자유게시판/ },
  { name: 'anonymous-board', path: '/community/anonymous', text: /익명 게시판/ },
  { name: 'classmates', path: '/community/classmates', text: /우리반 보기/ },
  { name: 'notice', path: '/help/notice', text: /공지사항/ },
  { name: 'faq', path: '/help/faq', text: /FAQ/ },
  { name: 'academic-rules', path: '/help/academic-rules', text: /학사규정/ },
  { name: 'qna', path: '/help/qna', text: /1:1 문의/ },
  { name: 'mentor-stories', path: '/mentoring/stories', text: /멘토 스토리/ },
  { name: 'mentoring-questions', path: '/mentoring/questions', text: /멘토링 Q&A/ },
  { name: 'mentoring-meetings', path: '/mentoring/meetings', text: /간담회 신청/ },
];

const viewports = [
  { name: 'desktop', width: 1440, height: 900 },
  { name: 'mobile', width: 390, height: 844 },
] as const;

test.describe('visual baseline smoke', () => {
  for (const viewport of viewports) {
    test.describe(viewport.name, () => {
      test.use({ viewport: { width: viewport.width, height: viewport.height } });

      for (const target of targets) {
        test(`${target.name} matches ${viewport.name} baseline`, async ({ page }) => {
          await loginAsDemoLearner(page);
          await page.goto(target.path);
          await stabilizeVisuals(page);
          await expect(page.getByRole('main').first()).toContainText(target.text);
          await expect(page).toHaveScreenshot(`${target.name}-${viewport.name}.png`, {
            fullPage: true,
            animations: 'disabled',
            caret: 'hide',
            maxDiffPixelRatio: 0.02,
          });
        });
      }
    });
  }
});
