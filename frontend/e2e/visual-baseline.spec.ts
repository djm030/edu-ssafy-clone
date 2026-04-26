import { expect, test } from '@playwright/test';
import { loginAsDemoLearner, stabilizeVisuals } from './fixtures';

type VisualTarget = {
  name: string;
  path: string;
  text: RegExp;
};

const targets: VisualTarget[] = [
  { name: 'dashboard', path: '/', text: /오늘의 학습|대시보드|SSAFY/ },
  { name: 'attendance', path: '/mycampus/attendance', text: /출석/ },
  { name: 'level', path: '/mycampus/level', text: /레벨|장학/ },
  { name: 'elearning', path: '/mycampus/elearning', text: /이러닝|학습중/ },
  { name: 'bookmarks', path: '/mycampus/bookmarks', text: /찜한|북마크/ },
  { name: 'documents', path: '/mycampus/documents', text: /서류/ },
  { name: 'curriculum', path: '/learning/curriculum', text: /커리큘럼/ },
  { name: 'quest', path: '/quest', text: /Quest|퀘스트/ },
  { name: 'materials', path: '/learning/materials', text: /학습자료/ },
  { name: 'survey', path: '/survey', text: /설문/ },
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
