import { promises as fs } from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const DEBUG_PORT = Number(process.env.SSAFY_DEBUG_PORT || '9226');
const OUTPUT_ROOT = path.resolve(process.env.SSAFY_OUTPUT_ROOT || 'ssafy_pages');
const BASE_ORIGIN = 'https://edu.ssafy.com';
const USER_ID = process.env.SSAFY_USER_ID || '';
const USER_PWD = process.env.SSAFY_USER_PWD || '';
const TARGET_KEYS = ['profile_edit', 'lecture_openlearning_detail', 'community_free_detail', 'notice_detail'];
const HAS_CREDENTIALS = Boolean(USER_ID && USER_PWD);
const TARGET_META = {
  profile_edit: {
    key: 'profile_edit',
    label: '회원정보 수정 화면',
    folder: '08_profile',
    priority: 1,
    entryUrl: `${BASE_ORIGIN}/edu/general/user/userPwdCheckForm.do`,
    entryFunction: 'fnPwdCheck() -> POST /edu/general/user/userPwdCheck.do -> submit /edu/general/user/userForm.do',
  },
  lecture_openlearning_detail: {
    key: 'lecture_openlearning_detail',
    label: '학습자료 상세/PDF 동작',
    folder: '09_popup',
    priority: 2,
    entryUrl: `${BASE_ORIGIN}/edu/lectureroom/openlearning/openLearningList.do`,
    entryFunction: "fnView(contId) -> POST /edu/lectureroom/openlearning/openLearningView.do",
  },
  community_free_detail: {
    key: 'community_free_detail',
    label: '자유게시판 상세',
    folder: '06_community',
    priority: 3,
    entryUrl: `${BASE_ORIGIN}/edu/board/free/list.do`,
    entryFunction: "fnDetail(brdItmSeq) -> /edu/board/free/detail.do",
  },
  notice_detail: {
    key: 'notice_detail',
    label: '공지사항 상세',
    folder: '07_notice',
    priority: 4,
    entryUrl: `${BASE_ORIGIN}/edu/board/notice/list.do`,
    entryFunction: "fnDetail(brdItmSeq) -> /edu/board/notice/detail.do",
  },
};

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function sha(input) {
  return crypto.createHash('sha1').update(input).digest('hex').slice(0, 10);
}

function normalizeUrl(rawUrl) {
  try {
    const url = new URL(rawUrl, BASE_ORIGIN);
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

async function fetchJson(url, init = {}) {
  const response = await fetch(url, init);
  if (!response.ok) throw new Error(`HTTP ${response.status} for ${url}`);
  return response.json();
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
      if (message.method === 'Page.javascriptDialogOpening') {
        this.send('Page.handleJavaScriptDialog', { accept: true }).catch(() => {});
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
    await this.send('Page.setDownloadBehavior', { behavior: 'deny' }).catch(() => {});
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
      if (readyState === 'complete' && this.inflight === 0 && Date.now() - this.lastActivity >= idleMs) return;
      await sleep(250);
    }
  }

  async close() {
    this.ws?.close();
  }
}

async function listTargets() {
  const targets = await fetchJson(`http://127.0.0.1:${DEBUG_PORT}/json/list`);
  return targets.filter(target => target.type === 'page');
}

async function openBlankPage() {
  const targetInfo = await fetchJson(`http://127.0.0.1:${DEBUG_PORT}/json/new?about:blank`, { method: 'PUT' });
  const page = new CDPPage(targetInfo);
  await page.connect();
  return page;
}

async function connectTarget(targetInfo) {
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
        value: field.type === 'password' ? '' : (field.value || ''),
      })),
    }));
    return {
      url: location.href,
      title: document.title,
      textSample: (document.body.innerText || '').trim().slice(0, 6000),
      links,
      buttons,
      forms,
    };
  })()`);
}

async function captureCurrentState(page, task, manifest, extra = {}) {
  await page.waitForStable({ timeoutMs: 30000, idleMs: 1500 });
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
    ...extra,
    ...pageInfo,
  });

  const captureEntry = {
    key: task.key,
    label: task.label,
    folder: task.folder,
    url: currentUrl,
    slug,
    html: path.relative(OUTPUT_ROOT, `${basePath}.html`),
    png: path.relative(OUTPUT_ROOT, `${basePath}.png`),
    json: path.relative(OUTPUT_ROOT, `${basePath}.json`),
  };

  const idx = manifest.captures.findIndex(item => item.key === task.key);
  if (idx >= 0) manifest.captures[idx] = captureEntry;
  else manifest.captures.push(captureEntry);
  manifest.skipped = (manifest.skipped || []).filter(item => item.key !== task.key);

  return { currentUrl, pageInfo, slug };
}

function removeStaleTargetCaptures(manifest) {
  manifest.captures = (manifest.captures || []).filter(item => {
    if (!TARGET_KEYS.includes(item.key)) return true;
    return !/SecurityLoginForm\.do/i.test(item.url || '');
  });
}

function upsertSkipped(manifest, key, reason, extra = {}) {
  const nextEntry = { key, reason, ...extra };
  const idx = (manifest.skipped || []).findIndex(item => item.key === key);
  if (idx >= 0) manifest.skipped[idx] = nextEntry;
  else manifest.skipped.push(nextEntry);
}

function createTargetStatuses() {
  return Object.fromEntries(
    TARGET_KEYS.map(key => [
      key,
      {
        ...TARGET_META[key],
        status: 'pending',
        finalUrl: null,
        pageType: null,
        confirmedUiElements: [],
        missingElements: [],
        operationalRisk: null,
        evidence: [],
      },
    ]),
  );
}

function setStatus(statuses, key, patch) {
  statuses[key] = {
    ...statuses[key],
    ...patch,
  };
}

async function writeRunArtifacts(manifest, statuses, errorMessage = null) {
  manifest.generatedAt = new Date().toISOString();
  removeStaleTargetCaptures(manifest);
  await writeJson(path.join(OUTPUT_ROOT, 'capture-manifest.json'), manifest);
  await writeJson(path.join(OUTPUT_ROOT, 'targeted-capture-summary.json'), {
    generatedAt: new Date().toISOString(),
    updatedKeys: TARGET_KEYS,
    authentication: {
      hasConfiguredCredentials: HAS_CREDENTIALS,
      error: errorMessage,
    },
    targets: TARGET_KEYS.map(key => {
      const capture = manifest.captures.find(item => item.key === key) || null;
      const skipped = (manifest.skipped || []).find(item => item.key === key) || null;
      const status = statuses[key] || TARGET_META[key];
      return {
        ...TARGET_META[key],
        status: status.status || (capture ? 'captured' : skipped ? 'skipped' : 'pending'),
        entryUrl: status.entryUrl || TARGET_META[key].entryUrl,
        entryFunction: status.entryFunction || TARGET_META[key].entryFunction,
        finalUrl: status.finalUrl || capture?.url || null,
        pageType: status.pageType || null,
        capture,
        skipped,
        confirmedUiElements: status.confirmedUiElements || [],
        missingElements: status.missingElements || [],
        operationalRisk: status.operationalRisk || null,
        evidence: status.evidence || [],
      };
    }),
  });

  if (errorMessage) {
    await writeJson(path.join(OUTPUT_ROOT, 'targeted-capture-error.json'), {
      generatedAt: new Date().toISOString(),
      error: errorMessage,
    });
    return;
  }

  await fs.rm(path.join(OUTPUT_ROOT, 'targeted-capture-error.json'), { force: true }).catch(() => {});
}

async function login(page) {
  if (!HAS_CREDENTIALS) {
    throw new Error('No active SSAFY session and SSAFY_USER_ID/SSAFY_USER_PWD are not configured.');
  }
  await page.navigate(`${BASE_ORIGIN}/comm/login/SecurityLoginForm.do`);
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

async function ensureAuthenticated(page) {
  await page.navigate(`${BASE_ORIGIN}/edu/main/index.do`);
  const currentUrl = await page.evaluate('location.href').catch(() => '');
  if (!/SecurityLoginForm\.do/i.test(currentUrl)) {
    return { mode: 'existing-session', currentUrl };
  }

  await login(page);
  const postLoginUrl = await page.evaluate('location.href').catch(() => '');
  return { mode: 'credential-login', currentUrl: postLoginUrl };
}

async function clickFirstMatching(page, { pattern, selector = 'a,button,input[type="button"],input[type="submit"]' }) {
  return page.evaluate(`(() => {
    const re = new RegExp(${JSON.stringify(pattern)});
    const isVisible = el => {
      if (!el) return false;
      const style = window.getComputedStyle(el);
      const rect = el.getBoundingClientRect();
      return style.display !== 'none' && style.visibility !== 'hidden' && rect.width > 0 && rect.height > 0;
    };
    const candidates = [...document.querySelectorAll(${JSON.stringify(selector)})];
    const el = candidates.find(node => {
      const onclick = node.getAttribute('onclick') || '';
      const href = node.href || node.getAttribute('href') || '';
      const text = (node.innerText || node.value || node.textContent || '').trim();
      return isVisible(node) && (re.test(onclick) || re.test(href) || re.test(text));
    });
    if (!el) return null;
    const meta = {
      text: (el.innerText || el.value || el.textContent || '').trim(),
      onclick: el.getAttribute('onclick') || '',
      href: el.href || el.getAttribute('href') || '',
      target: el.target || '',
    };
    el.click();
    return meta;
  })()`);
}

async function waitForTransitionOrPopup(page, baselineTargetIds, baselineUrl, { timeoutMs = 15000 } = {}) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    const targets = await listTargets();
    const newTarget = targets.find(target =>
      !baselineTargetIds.has(target.id) &&
      target.url &&
      target.url !== 'about:blank' &&
      !target.url.startsWith('devtools://')
    );
    if (newTarget) {
      const popupPage = await connectTarget(newTarget);
      await popupPage.waitForStable({ timeoutMs: 15000, idleMs: 1200 }).catch(() => {});
      return { kind: 'popup', page: popupPage };
    }

    const currentUrl = await page.evaluate('location.href').catch(() => baselineUrl);
    if (currentUrl && currentUrl !== baselineUrl) {
      await page.waitForStable({ timeoutMs: 20000, idleMs: 1200 }).catch(() => {});
      return { kind: 'same-tab', page };
    }
    await sleep(400);
  }

  await page.waitForStable({ timeoutMs: 10000, idleMs: 1000 }).catch(() => {});
  return { kind: 'same-tab', page };
}

async function captureLearningMaterialPopup(page, manifest, statuses) {
  const task = {
    key: 'lecture_openlearning_detail',
    label: '?????? ???/PDF ???',
    folder: '09_popup',
    stateSuffix: 'learning_material_pdf_popup',
  };

  await page.navigate(`${BASE_ORIGIN}/edu/lectureroom/openlearning/openLearningList.do`);
  const baselineUrl = await page.evaluate('location.href');
  const baselineTargetIds = new Set((await listTargets()).map(target => target.id));
  const clickMeta = await clickFirstMatching(page, { pattern: 'fnView\(' });
  if (!clickMeta) {
    upsertSkipped(manifest, task.key, 'no fnView transition found from open learning list');
    setStatus(statuses, task.key, {
      status: 'skipped',
      finalUrl: baselineUrl,
      pageType: 'same-page',
      missingElements: ['fnView candidate from openLearningList.do'],
      operationalRisk: '???? ??/PDF ??? ???? ??',
      evidence: ['openLearningList.do?? fnView ?? ??? ?? ?????.'],
    });
    return;
  }
  const transition = await waitForTransitionOrPopup(page, baselineTargetIds, baselineUrl);
  const capturePage = transition.page;
  try {
    const result = await captureCurrentState(capturePage, task, manifest, {
      note: '?????? ?????? ????? ????????????????? ???/??? ???',
      source: 'openLearningList.do',
      transitionKind: transition.kind,
      clickMeta,
    });
    setStatus(statuses, task.key, {
      status: 'captured',
      finalUrl: result.currentUrl,
      pageType: transition.kind === 'popup' ? 'popup' : 'same-page',
      confirmedUiElements: [clickMeta.text || 'fnView target clicked'].filter(Boolean),
      operationalRisk: 'PDF ??/???? ?? ??? ?? ??? ? ?? ??? ?????.',
      evidence: [`entry function: ${TARGET_META[task.key].entryFunction}`],
    });
  } finally {
    if (capturePage !== page) {
      await closeTarget(capturePage).catch(() => {});
    }
  }
}

async function captureCommunityDetail(page, manifest, statuses) {
  const task = {
    key: 'community_free_detail',
    label: '???????????',
    folder: '06_community',
  };

  await page.navigate(`${BASE_ORIGIN}/edu/board/free/list.do`);
  const baselineUrl = await page.evaluate('location.href');
  const baselineTargetIds = new Set((await listTargets()).map(target => target.id));
  const clickMeta = await clickFirstMatching(page, { pattern: 'fnDetail\(' });
  if (!clickMeta) {
    upsertSkipped(manifest, task.key, 'no fnDetail transition found from community free list');
    setStatus(statuses, task.key, {
      status: 'skipped',
      finalUrl: baselineUrl,
      pageType: 'same-page',
      missingElements: ['fnDetail candidate from free/list.do'],
      operationalRisk: '?? ????? ?? ?? ??',
      evidence: ['free/list.do?? fnDetail ?? ??? ?? ?????.'],
    });
    return;
  }
  const transition = await waitForTransitionOrPopup(page, baselineTargetIds, baselineUrl);
  const capturePage = transition.page;
  try {
    const result = await captureCurrentState(capturePage, task, manifest, {
      note: '?????????????? ????? ??? ?? ???',
      source: 'free/list.do',
      transitionKind: transition.kind,
      clickMeta,
    });
    setStatus(statuses, task.key, {
      status: 'captured',
      finalUrl: result.currentUrl,
      pageType: transition.kind === 'popup' ? 'popup' : 'same-page',
      confirmedUiElements: [clickMeta.text || 'fnDetail target clicked'],
      operationalRisk: '??/??? ?? ??? ????? ?? ???? ??? ? ????.',
      evidence: [`entry function: ${TARGET_META[task.key].entryFunction}`],
    });
  } finally {
    if (capturePage !== page) {
      await closeTarget(capturePage).catch(() => {});
    }
  }
}

async function captureNoticeDetail(page, manifest, statuses) {
  const task = {
    key: 'notice_detail',
    label: '?????? ???',
    folder: '07_notice',
  };

  await page.navigate(`${BASE_ORIGIN}/edu/board/notice/list.do`);
  const baselineUrl = await page.evaluate('location.href');
  const baselineTargetIds = new Set((await listTargets()).map(target => target.id));
  const clickMeta = await clickFirstMatching(page, { pattern: 'fnDetail\(' });
  if (!clickMeta) {
    upsertSkipped(manifest, task.key, 'no fnDetail transition found from notice list');
    setStatus(statuses, task.key, {
      status: 'skipped',
      finalUrl: baselineUrl,
      pageType: 'same-page',
      missingElements: ['fnDetail candidate from notice/list.do'],
      operationalRisk: '?? ?? ?? ?? ??',
      evidence: ['notice/list.do?? fnDetail ?? ??? ?? ?????.'],
    });
    return;
  }
  const transition = await waitForTransitionOrPopup(page, baselineTargetIds, baselineUrl);
  const capturePage = transition.page;
  try {
    const result = await captureCurrentState(capturePage, task, manifest, {
      note: '?????? ?????? ????? ??? ??? ???',
      source: 'notice/list.do',
      transitionKind: transition.kind,
      clickMeta,
    });
    setStatus(statuses, task.key, {
      status: 'captured',
      finalUrl: result.currentUrl,
      pageType: transition.kind === 'popup' ? 'popup' : 'same-page',
      confirmedUiElements: [clickMeta.text || 'fnDetail target clicked'],
      operationalRisk: '??/?? ?? richness? ?? ??? ?? ??? ?? ??? ? ????.',
      evidence: [`entry function: ${TARGET_META[task.key].entryFunction}`],
    });
  } finally {
    if (capturePage !== page) {
      await closeTarget(capturePage).catch(() => {});
    }
  }
}

async function captureProfileEdit(page, manifest, statuses) {
  const task = {
    key: 'profile_edit',
    label: '?????? ??? ???',
    folder: '08_profile',
    stateSuffix: 'profile_edit',
  };

  let flow = 'direct';
  await page.navigate(`${BASE_ORIGIN}/edu/general/user/userForm.do`);
  let currentUrl = await page.evaluate('location.href');

  if (/userPwdCheckForm\.do/i.test(currentUrl)) {
    flow = 'password-check';
    await page.evaluate(`(() => {
      const field = document.querySelector('#currentPwd, input[name="currentPwd"], input[type="password"]');
      if (!field) return false;
      field.focus();
      field.value = ${JSON.stringify(USER_PWD)};
      field.dispatchEvent(new Event('input', { bubbles: true }));
      field.dispatchEvent(new Event('change', { bubbles: true }));
      if (typeof fnPwdCheck === 'function') {
        fnPwdCheck();
        return true;
      }
      return false;
    })()`).catch(() => false);

    const start = Date.now();
    while (Date.now() - start < 15000) {
      currentUrl = await page.evaluate('location.href').catch(() => currentUrl);
      if (/userForm\.do/i.test(currentUrl)) break;
      await sleep(400);
    }

    if (!/userForm\.do/i.test(currentUrl)) {
      flow = 'forced-submit';
      await page.evaluate(`(() => {
        const form = document.forms.pwdCheckForm || document.querySelector('form[name="pwdCheckForm"]');
        if (!form) return false;
        form.action = '/edu/general/user/userForm.do';
        form.method = 'post';
        form.submit();
        return true;
      })()`).catch(() => false);
      await page.waitForStable({ timeoutMs: 15000, idleMs: 1200 }).catch(() => {});
    }
  }

  currentUrl = await page.evaluate('location.href');
  if (!/userForm\.do/i.test(currentUrl)) {
    upsertSkipped(manifest, task.key, `profile edit not reached; current url: ${currentUrl}`);
    setStatus(statuses, task.key, {
      status: 'skipped',
      finalUrl: currentUrl,
      pageType: 'same-page',
      missingElements: ['actual /edu/general/user/userForm.do form'],
      operationalRisk: '?? ???? ?? ? ???',
      evidence: [`current url after profile flow: ${currentUrl}`],
    });
    return;
  }

  const result = await captureCurrentState(page, task, manifest, {
    note: '?????? ??? ??? ??? ???/?????? ???????????????',
    flow,
  });
  setStatus(statuses, task.key, {
    status: 'captured',
    finalUrl: result.currentUrl,
    pageType: 'same-page',
    confirmedUiElements: ['actual /edu/general/user/userForm.do reached'],
    operationalRisk: '???? ?? ?? ?? ? ????? ???? ???.',
    evidence: [`profile flow: ${flow}`],
  });
}

async function loadManifest() {
  const manifestPath = path.join(OUTPUT_ROOT, 'capture-manifest.json');
  try {
    return JSON.parse(await fs.readFile(manifestPath, 'utf8'));
  } catch {
    return {
      generatedAt: new Date().toISOString(),
      purpose: 'Representative SSAFY page capture for entity extraction and functional requirements drafting',
      scope: {
        strategy: 'recommended representative coverage',
        rule: 'per section, capture representative list/detail/form/popup states instead of every page',
      },
      captures: [],
      skipped: [],
    };
  }
}

async function run() {
  const manifest = await loadManifest();
  manifest.generatedAt = new Date().toISOString();

  const page = await openBlankPage();
  try {
    console.log('[login] authenticating');
    await login(page);
    console.log('[login] success');

    await captureLearningMaterialPopup(page, manifest);
    await captureCommunityDetail(page, manifest);
    await captureNoticeDetail(page, manifest);
    await captureProfileEdit(page, manifest);
  } finally {
    await closeTarget(page).catch(() => {});
  }

  await writeJson(path.join(OUTPUT_ROOT, 'capture-manifest.json'), manifest);
  await writeJson(path.join(OUTPUT_ROOT, 'targeted-capture-summary.json'), {
    generatedAt: new Date().toISOString(),
    updatedKeys: ['lecture_openlearning_detail', 'community_free_detail', 'notice_detail', 'profile_edit'],
    captures: manifest.captures.filter(item => ['lecture_openlearning_detail', 'community_free_detail', 'notice_detail', 'profile_edit'].includes(item.key)),
    skipped: (manifest.skipped || []).filter(item => ['lecture_openlearning_detail', 'community_free_detail', 'notice_detail', 'profile_edit'].includes(item.key)),
  });
}

run().catch(async error => {
  await writeJson(path.join(OUTPUT_ROOT, 'targeted-capture-error.json'), {
    generatedAt: new Date().toISOString(),
    error: String(error?.message || error),
  }).catch(() => {});
  console.error(error);
  process.exit(1);
});
