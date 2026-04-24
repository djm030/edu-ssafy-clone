import { promises as fs } from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const DEBUG_PORT = Number(process.env.SSAFY_DEBUG_PORT || '9225');
const OUTPUT_ROOT = path.resolve(process.env.SSAFY_OUTPUT_ROOT || 'ssafy_pages');
const BASE_ORIGIN = 'https://edu.ssafy.com';
const USER_ID = process.env.SSAFY_USER_ID || '';
const USER_PWD = process.env.SSAFY_USER_PWD || '';

if (!USER_ID || !USER_PWD) {
  console.error('SSAFY_USER_ID and SSAFY_USER_PWD are required.');
  process.exit(1);
}

const FOLDERS = [
  '00_main',
  '01_auth',
  '02_dashboard',
  '03_lecture',
  '04_quest',
  '05_survey',
  '06_community',
  '07_notice',
  '08_profile',
  '09_popup',
];

const TASKS = [
  { key: 'auth_login', folder: '01_auth', url: '/comm/login/SecurityLoginForm.do', phase: 'prelogin', label: '로그인 화면' },
  { key: 'auth_pwd_search', folder: '01_auth', url: '/edu/login/pwdsearch/pwdSearchForm.do', phase: 'authenticated', label: '비밀번호 찾기' },
  { key: 'main_home', folder: '00_main', url: '/edu/main/index.do', phase: 'authenticated', label: '메인 대시보드' },
  {
    key: 'main_menu_overlay',
    folder: '09_popup',
    url: '/edu/main/index.do',
    phase: 'authenticated',
    label: '메인 메뉴 오버레이',
    stateSuffix: 'main_menu_overlay',
    action: {
      type: 'clickSelector',
      selector: '.burger-btn, .burger-btn.show-field',
      waitMs: 1500,
    },
  },
  { key: 'dashboard_level', folder: '02_dashboard', url: '/edu/mycampus/mylvlmlg/myLvlMlgView.do', phase: 'authenticated', label: '레벨/장학 포인트' },
  { key: 'dashboard_attendance', folder: '02_dashboard', url: '/edu/mycampus/attendance/attendanceClassList.do', phase: 'authenticated', label: '출석 현황' },
  { key: 'dashboard_notification', folder: '02_dashboard', url: '/edu/mycampus/notification/notificationList.do', phase: 'authenticated', label: '알림함' },
  { key: 'lecture_curriculum', folder: '03_lecture', url: '/edu/lectureroom/curriculumn/curriculumnWeeklyList.do', phase: 'authenticated', label: '주차별 커리큘럼' },
  { key: 'lecture_replay', folder: '03_lecture', url: '/edu/lectureroom/lecturereplay/lectureReplayNMyList.do', phase: 'authenticated', label: '내 강의 다시보기' },
  { key: 'lecture_openlearning', folder: '03_lecture', url: '/edu/lectureroom/openlearning/openLearningList.do', phase: 'authenticated', label: '학습자료 목록' },
  {
    key: 'lecture_openlearning_detail',
    folder: '03_lecture',
    phase: 'derived',
    sourceTask: 'lecture_openlearning',
    label: '학습자료 상세',
    clickOnclickPattern: 'fnView\\(',
  },
  { key: 'quest_list', folder: '04_quest', url: '/edu/lectureroom/questevaluation/questEvaluationList.do', phase: 'authenticated', label: 'Quest/평가 목록' },
  {
    key: 'quest_detail',
    folder: '04_quest',
    phase: 'derived',
    sourceTask: 'quest_list',
    label: 'Quest/평가 상세',
    clickOnclickPattern: 'fnDetail\\(',
  },
  { key: 'survey_list', folder: '05_survey', url: '/edu/lectureroom/survey/surveyList.do', phase: 'authenticated', label: '설문 목록' },
  {
    key: 'survey_detail',
    folder: '05_survey',
    phase: 'derived',
    sourceTask: 'survey_list',
    label: '설문 상세',
    hrefPattern: /\/edu\/lectureroom\/survey\/(detail|view|surveyView)\.do\?/i,
  },
  { key: 'community_free_list', folder: '06_community', url: '/edu/board/free/list.do', phase: 'authenticated', label: '자유게시판 목록' },
  {
    key: 'community_free_detail',
    folder: '06_community',
    phase: 'derived',
    sourceTask: 'community_free_list',
    label: '자유게시판 상세',
    clickOnclickPattern: 'fnDetail\\(',
  },
  { key: 'community_student_search', folder: '06_community', url: '/edu/community/search/searchStudentList.do', phase: 'authenticated', label: '우리반 보기' },
  { key: 'notice_list', folder: '07_notice', url: '/edu/board/notice/list.do', phase: 'authenticated', label: '공지사항 목록' },
  {
    key: 'notice_detail',
    folder: '07_notice',
    phase: 'derived',
    sourceTask: 'notice_list',
    label: '공지사항 상세',
    clickOnclickPattern: 'fnDetail\\(',
  },
  { key: 'faq_list', folder: '07_notice', url: '/edu/board/faq/list.do', phase: 'authenticated', label: 'FAQ 목록' },
  { key: 'qna_list', folder: '07_notice', url: '/edu/board/qna/list.do', phase: 'authenticated', label: '1:1 문의 목록' },
  { key: 'profile_pwd_check', folder: '08_profile', url: '/edu/general/user/userPwdCheckForm.do', phase: 'authenticated', label: '회원정보 비밀번호 확인' },
  {
    key: 'profile_edit',
    folder: '08_profile',
    url: '/edu/general/user/userPwdCheckForm.do',
    phase: 'authenticated',
    label: '회원정보 수정 화면',
    stateSuffix: 'profile_edit',
    action: {
      type: 'submitPasswordCheck',
      waitMs: 2000,
    },
  },
];

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function sha(input) {
  return crypto.createHash('sha1').update(input).digest('hex').slice(0, 10);
}

function normalizeUrl(rawUrl) {
  try {
    const url = new URL(rawUrl, BASE_ORIGIN);
    if (url.origin !== BASE_ORIGIN) return null;
    url.hash = '';
    const params = [...url.searchParams.entries()].sort(([a, av], [b, bv]) => a.localeCompare(b) || av.localeCompare(bv));
    url.search = params.length ? `?${new URLSearchParams(params).toString()}` : '';
    return url.toString();
  } catch {
    return null;
  }
}

function slugifyUrl(rawUrl, suffix = '') {
  const url = new URL(rawUrl, BASE_ORIGIN);
  const pathname = url.pathname.replace(/^\/+/, '').replace(/\/+$/, '') || 'root';
  const pathPart = pathname.replace(/[^a-zA-Z0-9._-]+/g, '__');
  const queryPart = [...url.searchParams.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([key, value]) => `${key}-${String(value).replace(/[^a-zA-Z0-9._-]+/g, '-')}`)
    .join('__');
  const querySlug = queryPart ? `__q__${queryPart}` : '';
  const suffixSlug = suffix ? `__state__${suffix.replace(/[^a-zA-Z0-9._-]+/g, '_')}` : '';
  return `${url.hostname.replace(/[^a-zA-Z0-9._-]+/g, '_')}__${pathPart}${querySlug}${suffixSlug}__${sha(`${url.toString()}::${suffix}`)}`;
}

async function ensureDir(dir) {
  await fs.mkdir(dir, { recursive: true });
}

async function writeText(filePath, content) {
  await ensureDir(path.dirname(filePath));
  await fs.writeFile(filePath, content, 'utf8');
}

async function writeJson(filePath, value) {
  await writeText(filePath, JSON.stringify(value, null, 2));
}

async function writeBinary(filePath, base64) {
  await ensureDir(path.dirname(filePath));
  await fs.writeFile(filePath, Buffer.from(base64, 'base64'));
}

class CDPPage {
  constructor(targetInfo) {
    this.targetInfo = targetInfo;
    this.ws = null;
    this.seq = 0;
    this.pending = new Map();
    this.inflight = 0;
    this.lastActivity = Date.now();
  }

  async connect() {
    this.ws = new WebSocket(this.targetInfo.webSocketDebuggerUrl);
    await new Promise((resolve, reject) => {
      this.ws.addEventListener('open', resolve, { once: true });
      this.ws.addEventListener('error', reject, { once: true });
    });
    this.ws.addEventListener('message', event => {
      const message = JSON.parse(event.data);
      if (message.id) {
        const pending = this.pending.get(message.id);
        if (!pending) return;
        this.pending.delete(message.id);
        if (message.error) pending.reject(new Error(JSON.stringify(message.error)));
        else pending.resolve(message.result);
        return;
      }
      if (message.method === 'Network.requestWillBeSent') {
        this.inflight += 1;
        this.lastActivity = Date.now();
      }
      if (message.method === 'Network.loadingFinished' || message.method === 'Network.loadingFailed') {
        this.inflight = Math.max(0, this.inflight - 1);
        this.lastActivity = Date.now();
      }
      if (message.method === 'Page.lifecycleEvent' || message.method === 'Network.responseReceived') {
        this.lastActivity = Date.now();
      }
    });
    await this.send('Page.enable');
    await this.send('Runtime.enable');
    await this.send('Network.enable', { maxPostDataSize: 65536 });
    await this.send('Page.setLifecycleEventsEnabled', { enabled: true });
    await this.send('Emulation.setDeviceMetricsOverride', {
      width: 1440,
      height: 2200,
      deviceScaleFactor: 1,
      mobile: false,
      screenWidth: 1440,
      screenHeight: 2200,
    }).catch(() => {});
  }

  send(method, params = {}) {
    const id = ++this.seq;
    this.ws.send(JSON.stringify({ id, method, params }));
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject });
    });
  }

  async evaluate(expression) {
    const result = await this.send('Runtime.evaluate', {
      expression,
      awaitPromise: true,
      returnByValue: true,
    });
    return result.result?.value;
  }

  async navigate(url) {
    await this.send('Page.navigate', { url });
    await this.waitForStable();
  }

  async waitForStable({ timeoutMs = 20000, idleMs = 1400 } = {}) {
    const start = Date.now();
    while (Date.now() - start < timeoutMs) {
      const readyState = await this.evaluate('document.readyState').catch(() => 'loading');
      if (readyState === 'complete' && this.inflight === 0 && Date.now() - this.lastActivity >= idleMs) {
        return;
      }
      await sleep(250);
    }
  }

  async close() {
    this.ws?.close();
  }
}

async function fetchJson(url, init = {}) {
  const response = await fetch(url, init);
  if (!response.ok) {
    throw new Error(`HTTP ${response.status} for ${url}`);
  }
  return response.json();
}

async function openBlankPage() {
  const targetInfo = await fetchJson(`http://127.0.0.1:${DEBUG_PORT}/json/new?about:blank`, { method: 'PUT' });
  const page = new CDPPage(targetInfo);
  await page.connect();
  return page;
}

async function closeTarget(page) {
  await page.close().catch(() => {});
  await fetch(`http://127.0.0.1:${DEBUG_PORT}/json/close/${page.targetInfo.id}`).catch(() => {});
}

async function extractPageInfo(page) {
  return page.evaluate(`(() => {
    const isVisible = el => {
      if (!el) return false;
      const style = window.getComputedStyle(el);
      const rect = el.getBoundingClientRect();
      return style.display !== 'none' && style.visibility !== 'hidden' && rect.width > 0 && rect.height > 0;
    };
    const links = [...document.querySelectorAll('a')].map((el, index) => ({
      index,
      text: (el.innerText || el.textContent || '').trim(),
      href: el.href || el.getAttribute('href') || '',
      target: el.target || '',
      id: el.id || '',
      className: el.className || '',
      onclick: el.getAttribute('onclick') || '',
      visible: isVisible(el),
    }));
    const buttons = [...document.querySelectorAll('button,input[type="button"],input[type="submit"]')].map((el, index) => ({
      index,
      text: (el.innerText || el.value || el.textContent || '').trim(),
      id: el.id || '',
      className: el.className || '',
      onclick: el.getAttribute('onclick') || '',
      visible: isVisible(el),
    }));
    const forms = [...document.forms].map((form, index) => ({
      index,
      id: form.id || '',
      name: form.name || '',
      action: form.action || '',
      method: form.method || '',
      fields: [...form.querySelectorAll('input,select,textarea')].map(field => ({
        tag: field.tagName,
        type: field.type || '',
        name: field.name || '',
        id: field.id || '',
      })),
    }));
    return {
      url: location.href,
      title: document.title,
      textSample: (document.body.innerText || '').trim().slice(0, 4000),
      links,
      buttons,
      forms,
    };
  })()`);
}

async function captureCurrentState(page, task, manifest, note = '') {
  await page.waitForStable({ timeoutMs: 25000, idleMs: 1500 });
  const currentUrl = await page.evaluate('location.href');
  const html = await page.evaluate('document.documentElement.outerHTML');
  const screenshot = await page.send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true, fromSurface: true });
  const pageInfo = await extractPageInfo(page);
  const slug = slugifyUrl(currentUrl, task.stateSuffix || '');
  const folderPath = path.join(OUTPUT_ROOT, task.folder);
  const basePath = path.join(folderPath, slug);

  await writeText(`${basePath}.html`, html);
  await writeBinary(`${basePath}.png`, screenshot.data);
  await writeJson(`${basePath}.json`, {
    key: task.key,
    label: task.label,
    folder: task.folder,
    capturedAt: new Date().toISOString(),
    note,
    ...pageInfo,
  });

  manifest.captures.push({
    key: task.key,
    label: task.label,
    folder: task.folder,
    url: currentUrl,
    slug,
    html: path.relative(OUTPUT_ROOT, `${basePath}.html`),
    png: path.relative(OUTPUT_ROOT, `${basePath}.png`),
    json: path.relative(OUTPUT_ROOT, `${basePath}.json`),
  });

  return { currentUrl, pageInfo, slug };
}

async function performAction(page, action) {
  if (!action) return;
  if (action.type === 'clickSelector') {
    await page.evaluate(`(() => {
      const el = document.querySelector(${JSON.stringify(action.selector)});
      if (!el) return false;
      el.click();
      return true;
    })()`);
    await sleep(action.waitMs || 1200);
    await page.waitForStable({ timeoutMs: 12000, idleMs: 700 });
    return;
  }

  if (action.type === 'submitPasswordCheck') {
    await page.evaluate(`(() => {
      const pwd = ${JSON.stringify(USER_PWD)};
      const candidate = document.querySelector('input[type="password"], #userPwd, #pwd, #currentPwd');
      if (!candidate) return false;
      candidate.focus();
      candidate.value = pwd;
      candidate.dispatchEvent(new Event('input', { bubbles: true }));
      candidate.dispatchEvent(new Event('change', { bubbles: true }));
      if (typeof fnPwdCheck === 'function') {
        fnPwdCheck();
        return true;
      }
      const submit =
        [...document.querySelectorAll('button,input[type="submit"],a')]
          .find(el => /(확인|다음|회원정보|저장|조회)/.test((el.innerText || el.value || '').trim())) ||
        document.querySelector('form button, form input[type="submit"]');
      if (submit) {
        submit.click();
        return true;
      }
      return false;
    })()`);
    await sleep(action.waitMs || 2200);
    await page.waitForStable({ timeoutMs: 15000, idleMs: 1000 });
    await page.evaluate(`(() => {
      if (/userPwdCheckForm\\.do/.test(location.href) && typeof _URL_USER_EDIT !== 'undefined') {
        const form = document.forms.pwdCheckForm || document.querySelector('form[name="pwdCheckForm"]');
        if (form) {
          form.action = _URL_USER_EDIT;
          form.submit();
          return true;
        }
      }
      return false;
    })()`).catch(() => {});
    await sleep(1500);
    await page.waitForStable({ timeoutMs: 15000, idleMs: 1000 });
  }
}

async function login(page, manifest) {
  await page.navigate(`${BASE_ORIGIN}/comm/login/SecurityLoginForm.do`);
  await captureCurrentState(page, TASKS.find(task => task.key === 'auth_login'), manifest, 'pre-login representative auth screen');
  await page.evaluate(`(() => {
    const setValue = (selector, value) => {
      const el = document.querySelector(selector);
      if (!el) return false;
      el.focus();
      el.value = value;
      el.dispatchEvent(new Event('input', { bubbles: true }));
      el.dispatchEvent(new Event('change', { bubbles: true }));
      return true;
    };
    setValue('#userId', ${JSON.stringify(USER_ID)});
    setValue('#userPwd', ${JSON.stringify(USER_PWD)});
    if (typeof fnLogin === 'function') {
      fnLogin();
      return true;
    }
    const form = document.querySelector('form[name="loginForm"]');
    if (form) {
      form.submit();
      return true;
    }
    return false;
  })()`);

  const start = Date.now();
  while (Date.now() - start < 20000) {
    const currentUrl = await page.evaluate('location.href').catch(() => '');
    if (currentUrl && !/SecurityLoginForm\.do/.test(currentUrl)) break;
    await sleep(400);
  }
  await page.waitForStable({ timeoutMs: 25000, idleMs: 1500 });
  const postLoginUrl = await page.evaluate('location.href');
  if (/SecurityLoginForm\.do/.test(postLoginUrl)) {
    throw new Error('Login stayed on the login screen.');
  }
}

function findDerivedUrl(sourceCapture, hrefPattern) {
  const candidate = (sourceCapture?.pageInfo?.links || []).find(
    link => link.visible && hrefPattern.test(`${link.href || ''} ${link.onclick || ''}`),
  );
  return candidate ? normalizeUrl(candidate.href) : null;
}

async function clickDerivedTransition(page, sourceTask, patternText) {
  await page.navigate(normalizeUrl(sourceTask.url));
  const clicked = await page.evaluate(`(() => {
    const pattern = new RegExp(${JSON.stringify(patternText)});
    const candidates = [...document.querySelectorAll('a,button,input[type="button"],input[type="submit"]')];
    const el = candidates.find(node => {
      const onclick = node.getAttribute('onclick') || '';
      const href = node.href || node.getAttribute('href') || '';
      const text = (node.innerText || node.value || node.textContent || '').trim();
      return pattern.test(onclick) || pattern.test(href) || pattern.test(text);
    });
    if (!el) return false;
    el.click();
    return true;
  })()`);
  if (!clicked) {
    return false;
  }
  await sleep(2200);
  await page.waitForStable({ timeoutMs: 20000, idleMs: 1200 });
  return true;
}

async function run() {
  for (const folder of FOLDERS) {
    await ensureDir(path.join(OUTPUT_ROOT, folder));
  }

  const manifest = {
    generatedAt: new Date().toISOString(),
    purpose: 'Representative SSAFY page capture for entity extraction and functional requirements drafting',
    scope: {
      strategy: 'recommended representative coverage',
      rule: 'per section, capture representative list/detail/form/popup states instead of every page',
    },
    captures: [],
    skipped: [],
  };

  const page = await openBlankPage();
  const resultsByTask = new Map();
  try {
    console.log('[login] authenticating');
    await login(page, manifest);
    console.log('[login] success');

    for (const task of TASKS) {
      if (task.phase === 'prelogin') {
        continue;
      }

      let targetUrl = null;
      if (task.phase === 'authenticated') {
        targetUrl = normalizeUrl(task.url);
      } else if (task.phase === 'derived') {
        const sourceTask = TASKS.find(candidate => candidate.key === task.sourceTask);
        if (!sourceTask) {
          manifest.skipped.push({ key: task.key, reason: `source task not found: ${task.sourceTask}` });
          continue;
        }
        if (task.clickOnclickPattern) {
          console.log(`[capture] ${task.key} -> transition from ${sourceTask.key}`);
          const clicked = await clickDerivedTransition(page, sourceTask, task.clickOnclickPattern);
          if (!clicked) {
            manifest.skipped.push({ key: task.key, reason: `no clickable transition found from ${task.sourceTask}` });
            continue;
          }
          const result = await captureCurrentState(page, task, manifest, task.label);
          resultsByTask.set(task.key, result);
          continue;
        }
        targetUrl = findDerivedUrl(resultsByTask.get(task.sourceTask), task.hrefPattern);
        if (!targetUrl) {
          manifest.skipped.push({ key: task.key, reason: `no derived link found from ${task.sourceTask}` });
          continue;
        }
      }

      if (!targetUrl) {
        manifest.skipped.push({ key: task.key, reason: 'no target url resolved' });
        continue;
      }

      console.log(`[capture] ${task.key} -> ${targetUrl}`);
      await page.navigate(targetUrl);
      if (task.action) {
        await performAction(page, task.action);
      }
      const result = await captureCurrentState(page, task, manifest, task.label);
      resultsByTask.set(task.key, result);
    }
  } finally {
    await closeTarget(page);
  }

  await writeJson(path.join(OUTPUT_ROOT, 'capture-manifest.json'), manifest);
  console.log(JSON.stringify({ captures: manifest.captures.length, skipped: manifest.skipped.length }, null, 2));
}

run().catch(async error => {
  const partial = {
    generatedAt: new Date().toISOString(),
    error: String(error?.message || error),
  };
  await writeJson(path.join(OUTPUT_ROOT, 'capture-manifest.partial.json'), partial).catch(() => {});
  console.error(error);
  process.exit(1);
});
