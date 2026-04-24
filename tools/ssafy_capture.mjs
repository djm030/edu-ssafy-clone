import { promises as fs } from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const DEBUG_PORT = Number(process.env.SSAFY_DEBUG_PORT || '9223');
const OUTPUT_ROOT = path.resolve(process.env.SSAFY_OUTPUT_ROOT || 'ssafy_pages');
const BASE_ORIGIN = 'https://edu.ssafy.com';
const LOGIN_URL = `${BASE_ORIGIN}/comm/login/SecurityLoginForm.do`;
const MAIN_URL = `${BASE_ORIGIN}/edu/main/index.do`;
const MAX_PAGES = Number(process.env.SSAFY_MAX_PAGES || '140');
const USER_ID = process.env.SSAFY_USER_ID || '';
const USER_PWD = process.env.SSAFY_USER_PWD || '';

if (!USER_ID || !USER_PWD) {
  console.error('SSAFY_USER_ID and SSAFY_USER_PWD are required.');
  process.exit(1);
}

const FOLDER_RULES = [
  { folder: '09_popup', test: (_, ctx) => Boolean(ctx?.isPopup) },
  { folder: '01_auth', test: u => /^\/comm\/login\//.test(u.pathname) || /^\/edu\/login\//.test(u.pathname) },
  { folder: '08_profile', test: u => /^\/edu\/general\/user\//.test(u.pathname) || /^\/edu\/mycampus\/notification\//.test(u.pathname) },
  { folder: '00_main', test: u => /^\/edu\/main\//.test(u.pathname) },
  { folder: '04_quest', test: u => /\/questevaluation\//.test(u.pathname) || /quest/i.test(u.pathname) },
  { folder: '05_survey', test: u => /\/survey\//.test(u.pathname) },
  { folder: '03_lecture', test: u => /^\/edu\/lectureroom\//.test(u.pathname) },
  { folder: '06_community', test: u => /^\/edu\/community\//.test(u.pathname) || /\/board\/(free|anonymity|mentoReview)\//.test(u.pathname) },
  { folder: '07_notice', test: u => /\/board\/(notice|faq|qna|rule|mentoNotice|mentoQna|mentoState|docReq)\//.test(u.pathname) },
  { folder: '02_dashboard', test: u => /^\/edu\/mycampus\//.test(u.pathname) },
];

const SAFE_BUTTON_TEXT = /(상세|보기|조회|확인|열기|닫기|팝업|more|tab|알림함)/i;
const BLOCKED_URL_PATTERNS = [
  /logout/i,
  /SecurityJobLoginSSOForm/i,
  /meeting\.ssafy\.com/i,
  /project\.ssafy\.com/i,
  /javascript:/i,
  /#none/i,
  /#go/i,
  /\/download/i,
  /fileDown/i,
  /\/viewer\//i,
  /\/vod\//i,
  /\/stream/i,
  /\/video/i,
  /\/attachment/i,
  /\.(pdf|zip|rar|7z|doc|docx|xls|xlsx|ppt|pptx|hwp|hwpx|mp4|avi|mov|wmv|png|jpe?g|gif|bmp)$/i,
];

const state = {
  manifest: {
    generatedAt: new Date().toISOString(),
    maxPages: MAX_PAGES,
    pages: [],
    popups: [],
    blocked: [],
  },
  seen: new Set(),
  queued: new Set(),
};

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function sha(input) {
  return crypto.createHash('sha1').update(input).digest('hex').slice(0, 10);
}

function slugifyUrl(rawUrl) {
  const url = new URL(rawUrl);
  const pathname = url.pathname.replace(/^\/+/, '').replace(/\/+$/, '') || 'root';
  const pathPart = pathname.replace(/[^a-zA-Z0-9._-]+/g, '__');
  const params = [...url.searchParams.entries()].sort(([a], [b]) => a.localeCompare(b));
  const queryPart = params.length
    ? '__q__' + params.map(([k, v]) => `${k}-${String(v).replace(/[^a-zA-Z0-9._-]+/g, '-')}`).join('__')
    : '';
  return `${url.hostname.replace(/[^a-zA-Z0-9._-]+/g, '_')}__${pathPart}${queryPart}__${sha(rawUrl)}`;
}

function classifyUrl(rawUrl, ctx = {}) {
  const url = new URL(rawUrl, BASE_ORIGIN);
  for (const rule of FOLDER_RULES) {
    if (rule.test(url, ctx)) return rule.folder;
  }
  return '00_main';
}

function normalizeUrl(rawUrl) {
  try {
    const url = new URL(rawUrl, BASE_ORIGIN);
    if (url.origin !== BASE_ORIGIN) return null;
    url.hash = '';
    const params = [...url.searchParams.entries()].sort(([a, av], [b, bv]) => a.localeCompare(b) || av.localeCompare(bv));
    url.search = params.length ? `?${new URLSearchParams(params).toString()}` : '';
    const href = url.toString();
    if (BLOCKED_URL_PATTERNS.some(pattern => pattern.test(href))) return null;
    return href;
  } catch {
    return null;
  }
}

function extractUrlsFromText(text) {
  if (!text) return [];
  const matches = new Set();
  const regexes = [
    /https?:\/\/[^'"\s)]+/g,
    /(?:^|[^a-zA-Z0-9])((?:\/edu|\/comm)\/[^'"\s)]+\.do(?:\?[^'"\s)]*)?)/g,
  ];
  for (const regex of regexes) {
    for (const match of text.matchAll(regex)) {
      const value = match[1] || match[0];
      matches.add(value.trim());
    }
  }
  return [...matches];
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
    this.handlers = new Map();
    this.network = { requests: [], responses: [], failures: [] };
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
      if (message.method) {
        this.handleEvent(message.method, message.params || {});
      }
    });
    await this.send('Page.enable');
    await this.send('Runtime.enable');
    await this.send('Network.enable', { maxPostDataSize: 65536 });
    await this.send('Page.setLifecycleEventsEnabled', { enabled: true });
    await this.send('Page.setDownloadBehavior', { behavior: 'deny', downloadPath: OUTPUT_ROOT }).catch(() => {});
    await this.send('Emulation.setDeviceMetricsOverride', {
      width: 1440,
      height: 1600,
      deviceScaleFactor: 1,
      mobile: false,
      screenWidth: 1440,
      screenHeight: 1600,
    }).catch(() => {});
    await this.send('Emulation.setUserAgentOverride', {
      userAgent:
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36',
    }).catch(() => {});
  }

  handleEvent(method, params) {
    const handlers = this.handlers.get(method) || [];
    for (const handler of handlers) handler(params);
    if (method === 'Network.requestWillBeSent') {
      this.inflight += 1;
      this.lastActivity = Date.now();
      this.network.requests.push({
        requestId: params.requestId,
        url: params.request?.url,
        method: params.request?.method,
        type: params.type,
        documentURL: params.documentURL,
        initiator: params.initiator?.type,
        postData: params.request?.postData,
        timestamp: params.timestamp,
      });
    }
    if (method === 'Network.responseReceived') {
      this.lastActivity = Date.now();
      this.network.responses.push({
        requestId: params.requestId,
        url: params.response?.url,
        status: params.response?.status,
        mimeType: params.response?.mimeType,
        remoteIPAddress: params.response?.remoteIPAddress,
        fromDiskCache: params.response?.fromDiskCache,
        fromServiceWorker: params.response?.fromServiceWorker,
        encodedDataLength: params.response?.encodedDataLength,
        type: params.type,
        timestamp: params.timestamp,
      });
    }
    if (method === 'Network.loadingFinished' || method === 'Network.loadingFailed') {
      this.inflight = Math.max(0, this.inflight - 1);
      this.lastActivity = Date.now();
      if (method === 'Network.loadingFailed') {
        this.network.failures.push({ ...params });
      }
    }
  }

  on(method, handler) {
    const handlers = this.handlers.get(method) || [];
    handlers.push(handler);
    this.handlers.set(method, handlers);
  }

  send(method, params = {}) {
    const id = ++this.seq;
    this.ws.send(JSON.stringify({ id, method, params }));
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject });
    });
  }

  async navigate(url) {
    await this.send('Page.navigate', { url });
    await this.waitForStable();
  }

  async waitForStable({ timeoutMs = 20000, idleMs = 1500 } = {}) {
    const start = Date.now();
    while (Date.now() - start < timeoutMs) {
      const readyState = await this.evaluate('document.readyState').catch(() => 'loading');
      if (readyState === 'complete' && this.inflight === 0 && Date.now() - this.lastActivity >= idleMs) {
        return;
      }
      await sleep(300);
    }
  }

  async evaluate(expression) {
    const result = await this.send('Runtime.evaluate', {
      expression,
      awaitPromise: true,
      returnByValue: true,
    });
    return result.result?.value;
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
  await page.close();
  await fetch(`http://127.0.0.1:${DEBUG_PORT}/json/close/${page.targetInfo.id}`).catch(() => {});
}

async function capturePageArtifacts(page, rawUrl, ctx = {}) {
  const currentUrl = await page.evaluate('location.href');
  const folder = classifyUrl(currentUrl || rawUrl, ctx);
  const slug = slugifyUrl(currentUrl || rawUrl);
  const folderPath = path.join(OUTPUT_ROOT, folder);
  await ensureDir(folderPath);

  const pageData = await page.evaluate(`(() => {
    const isVisible = el => {
      if (!el) return false;
      const style = window.getComputedStyle(el);
      const rect = el.getBoundingClientRect();
      return style.display !== 'none' && style.visibility !== 'hidden' && rect.width > 0 && rect.height > 0;
    };
    const linkNodes = [...document.querySelectorAll('a')].map((el, index) => ({
      index,
      tag: el.tagName,
      text: (el.innerText || el.textContent || '').trim(),
      href: el.href || el.getAttribute('href') || '',
      target: el.target || '',
      className: el.className || '',
      id: el.id || '',
      onclick: el.getAttribute('onclick') || '',
      visible: isVisible(el),
    }));
    const buttonNodes = [...document.querySelectorAll('button,input[type="button"],input[type="submit"],a[href^="javascript:"]')].map((el, index) => ({
      index,
      tag: el.tagName,
      type: el.type || '',
      text: (el.innerText || el.value || el.textContent || '').trim(),
      href: el.href || '',
      className: el.className || '',
      id: el.id || '',
      onclick: el.getAttribute('onclick') || '',
      visible: isVisible(el),
    }));
    const forms = [...document.forms].map((form, index) => ({
      index,
      name: form.name || '',
      id: form.id || '',
      method: form.method || '',
      action: form.action || '',
      fields: [...form.querySelectorAll('input,select,textarea')].map(field => ({
        tag: field.tagName,
        type: field.type || '',
        name: field.name || '',
        id: field.id || '',
        value: field.type === 'password' ? '<redacted>' : field.value,
      })),
    }));
    const popupSelectors = ['[role="dialog"]', '.modal', '.popup', '.layer-popup', '.pop-layer', '.ui-dialog', '.modal-wrap', '.dialog'];
    const popupNodes = popupSelectors.flatMap(selector => [...document.querySelectorAll(selector)])
      .filter((el, index, arr) => arr.indexOf(el) === index && isVisible(el))
      .map((el, index) => ({
        index,
        selector: el.className || el.id || el.getAttribute('role') || el.tagName,
        text: (el.innerText || '').trim().slice(0, 2000),
        html: el.outerHTML,
      }));
    return {
      url: location.href,
      title: document.title,
      textSample: (document.body.innerText || '').trim().slice(0, 4000),
      readyState: document.readyState,
      links: linkNodes,
      buttons: buttonNodes,
      forms,
      popups: popupNodes,
    };
  })()`);

  const html = await page.evaluate('document.documentElement.outerHTML');
  const screenshot = await page.send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true, fromSurface: true }).then(r => r.data);

  const basePath = path.join(folderPath, slug);
  await writeText(`${basePath}.html`, html);
  await writeBinary(`${basePath}.png`, screenshot);

  const resourcesDir = path.join(folderPath, `${slug}__resources`);
  await ensureDir(resourcesDir);
  const resourceManifest = [];
  for (const [index, response] of (page.network.responses || []).entries()) {
    const requestId = response.requestId;
    if (!requestId || !response.url) continue;
    try {
      const bodyResult = await page.send('Network.getResponseBody', { requestId });
      const resourceUrl = new URL(response.url);
      const bodyBaseName = `${String(index + 1).padStart(3, '0')}__${slugifyUrl(resourceUrl.toString())}`;
      const extFromPath = path.extname(resourceUrl.pathname || '');
      const mime = response.mimeType || '';
      let ext = extFromPath;
      if (!ext) {
        if (/json/i.test(mime)) ext = '.json';
        else if (/javascript/i.test(mime)) ext = '.js';
        else if (/css/i.test(mime)) ext = '.css';
        else if (/html/i.test(mime)) ext = '.html';
        else if (/svg/i.test(mime)) ext = '.svg';
        else if (/png/i.test(mime)) ext = '.png';
        else if (/jpe?g/i.test(mime)) ext = '.jpg';
        else if (/gif/i.test(mime)) ext = '.gif';
        else if (/webp/i.test(mime)) ext = '.webp';
        else if (/woff2/i.test(mime)) ext = '.woff2';
        else if (/woff/i.test(mime)) ext = '.woff';
        else if (/ttf/i.test(mime)) ext = '.ttf';
        else if (/plain/i.test(mime)) ext = '.txt';
        else ext = '.bin';
      }
      const filePath = path.join(resourcesDir, `${bodyBaseName}${ext}`);
      if (bodyResult.base64Encoded) await fs.writeFile(filePath, Buffer.from(bodyResult.body, 'base64'));
      else await fs.writeFile(filePath, bodyResult.body, 'utf8');
      resourceManifest.push({ ...response, savedAs: path.relative(OUTPUT_ROOT, filePath), base64Encoded: Boolean(bodyResult.base64Encoded) });
    } catch (error) {
      resourceManifest.push({ ...response, bodyError: String(error.message || error) });
    }
  }

  await writeJson(`${basePath}.json`, {
    ...pageData,
    requestedUrl: rawUrl,
    capturedAt: new Date().toISOString(),
    folder,
    slug,
    isPopup: Boolean(ctx.isPopup),
    network: page.network,
    resourceManifest,
  });

  const popupRecords = [];
  for (const popup of pageData.popups || []) {
    const popupSlug = `${slug}__popup_${String(popup.index + 1).padStart(2, '0')}`;
    const popupBase = path.join(OUTPUT_ROOT, '09_popup', popupSlug);
    await writeText(`${popupBase}.html`, popup.html);
    await writeJson(`${popupBase}.json`, {
      sourceUrl: pageData.url,
      capturedAt: new Date().toISOString(),
      text: popup.text,
      selector: popup.selector,
    });
    popupRecords.push({ slug: popupSlug, sourceUrl: pageData.url, selector: popup.selector, folder: '09_popup' });
  }

  return { currentUrl, folder, slug, pageData, popupRecords };
}

function addBlocked(url, reason) {
  state.manifest.blocked.push({ url, reason });
}

function enqueue(url, ctx = {}) {
  const normalized = normalizeUrl(url);
  if (!normalized) {
    addBlocked(url, 'normalized-to-null-or-blocked');
    return;
  }
  if (state.seen.has(normalized) || state.queued.has(normalized)) return;
  state.queue.push({ url: normalized, ctx });
  state.queued.add(normalized);
}

function deriveCandidates(pageData) {
  const candidates = [];
  const addCandidate = (candidateUrl, extra = {}) => {
    const normalized = normalizeUrl(candidateUrl);
    if (!normalized) return;
    candidates.push({ url: normalized, ctx: extra });
  };

  for (const link of pageData.links || []) {
    if (!link.visible && !link.href) continue;
    addCandidate(link.href, {
      isPopup: link.target === '_blank' || /popup/i.test(link.className) || /popup/i.test(link.href) || /popup/i.test(link.onclick),
      source: 'link',
    });
    for (const extracted of extractUrlsFromText(`${link.href || ''} ${link.onclick || ''}`)) {
      addCandidate(extracted, {
        isPopup: /popup|window\.open/i.test(`${link.href || ''} ${link.onclick || ''}`),
        source: 'link-js',
      });
    }
  }

  for (const button of pageData.buttons || []) {
    if (!button.visible) continue;
    const sourceText = `${button.text || ''} ${button.className || ''} ${button.id || ''} ${button.onclick || ''}`;
    for (const extracted of extractUrlsFromText(`${button.href || ''} ${button.onclick || ''}`)) {
      addCandidate(extracted, {
        isPopup: /popup|window\.open/i.test(sourceText),
        source: 'button-js',
      });
    }
    if (SAFE_BUTTON_TEXT.test(sourceText) && button.href) {
      addCandidate(button.href, {
        isPopup: /popup/i.test(sourceText),
        source: 'button-href',
      });
    }
  }

  for (const form of pageData.forms || []) {
    if (form.action) {
      addCandidate(form.action, { source: 'form-action' });
    }
  }

  return candidates;
}

async function loginAndCaptureAuth() {
  const page = await openBlankPage();
  try {
    console.log(`[auth] navigating to ${LOGIN_URL}`);
    await page.navigate(LOGIN_URL);
    const authCapture = await capturePageArtifacts(page, LOGIN_URL, { isPopup: false });
    console.log('[auth] captured login page');
    state.manifest.pages.push({ url: authCapture.pageData.url, folder: authCapture.folder, slug: authCapture.slug, title: authCapture.pageData.title });

    console.log('[auth] submitting credentials');
    await page.evaluate(`(async () => {
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
      } else {
        document.querySelector('form[name="loginForm"]')?.submit();
      }
      return true;
    })()`);

    const start = Date.now();
    while (Date.now() - start < 20000) {
      const current = await page.evaluate('location.href').catch(() => '');
      if (current && !/SecurityLoginForm\.do/.test(current)) break;
      await sleep(500);
    }
    await page.waitForStable({ timeoutMs: 20000, idleMs: 2000 });
    const current = await page.evaluate('location.href');
    if (/SecurityLoginForm\.do/.test(current)) {
      const details = await page.evaluate(`(() => ({ url: location.href, text: document.body.innerText.slice(0, 1200) }))()`);
      throw new Error(`Login did not leave the login page: ${JSON.stringify(details)}`);
    }
    return page;
  } catch (error) {
    await closeTarget(page).catch(() => {});
    throw error;
  }
}

async function crawl() {
  await ensureDir(OUTPUT_ROOT);
  for (const folder of ['00_main','01_auth','02_dashboard','03_lecture','04_quest','05_survey','06_community','07_notice','08_profile','09_popup']) {
    await ensureDir(path.join(OUTPUT_ROOT, folder));
  }

  const authPage = await loginAndCaptureAuth();
  try {
    await authPage.waitForStable({ timeoutMs: 20000, idleMs: 2000 });
    console.log('[crawl] capturing main page');
    const mainCapture = await capturePageArtifacts(authPage, MAIN_URL, { isPopup: false });
    console.log('[crawl] captured main page');
    state.manifest.pages.push({ url: mainCapture.pageData.url, folder: mainCapture.folder, slug: mainCapture.slug, title: mainCapture.pageData.title });
    state.seen.add(normalizeUrl(mainCapture.pageData.url));

    for (const popupRecord of mainCapture.popupRecords) {
      state.manifest.popups.push(popupRecord);
    }

    state.queue = [];
    for (const candidate of deriveCandidates(mainCapture.pageData)) {
      enqueue(candidate.url, candidate.ctx);
    }
    enqueue(`${BASE_ORIGIN}/edu/general/user/userPwdCheckForm.do`, { source: 'seed' });
    enqueue(`${BASE_ORIGIN}/edu/board/notice/list.do`, { source: 'seed' });
    enqueue(`${BASE_ORIGIN}/edu/board/free/list.do`, { source: 'seed' });
    enqueue(`${BASE_ORIGIN}/edu/lectureroom/curriculumn/curriculumnWeeklyList.do`, { source: 'seed' });
    enqueue(`${BASE_ORIGIN}/edu/lectureroom/questevaluation/questEvaluationList.do`, { source: 'seed' });
    enqueue(`${BASE_ORIGIN}/edu/lectureroom/survey/surveyList.do`, { source: 'seed' });
    enqueue(`${BASE_ORIGIN}/edu/community/search/searchStudentList.do`, { source: 'seed' });

    while (state.queue.length && state.manifest.pages.length < MAX_PAGES) {
      const job = state.queue.shift();
      state.queued.delete(job.url);
      if (state.seen.has(job.url)) continue;
      console.log(`[crawl] ${state.manifest.pages.length + 1}/${MAX_PAGES} ${job.url}`);
      const page = await openBlankPage();
      try {
        await page.navigate(job.url);
        const currentUrl = await page.evaluate('location.href');
        const normalizedCurrent = normalizeUrl(currentUrl);
        if (!normalizedCurrent) {
          addBlocked(currentUrl || job.url, 'redirected-to-blocked-url');
          await closeTarget(page);
          continue;
        }
        if (/SecurityLoginForm\.do/.test(normalizedCurrent)) {
          addBlocked(job.url, 'redirected-to-login');
          await closeTarget(page);
          continue;
        }
        if (state.seen.has(normalizedCurrent)) {
          await closeTarget(page);
          continue;
        }

        const capture = await capturePageArtifacts(page, job.url, job.ctx);
        state.manifest.pages.push({ url: capture.pageData.url, folder: capture.folder, slug: capture.slug, title: capture.pageData.title });
        state.seen.add(normalizedCurrent);
        for (const popupRecord of capture.popupRecords) {
          state.manifest.popups.push(popupRecord);
        }
        for (const candidate of deriveCandidates(capture.pageData)) {
          enqueue(candidate.url, candidate.ctx);
        }
      } catch (error) {
        addBlocked(job.url, `capture-failed:${error.message}`);
      } finally {
        await closeTarget(page).catch(() => {});
      }
    }
  } finally {
    await closeTarget(authPage).catch(() => {});
  }

  await writeJson(path.join(OUTPUT_ROOT, 'capture-manifest.json'), state.manifest);
  console.log(JSON.stringify({ pages: state.manifest.pages.length, popups: state.manifest.popups.length, blocked: state.manifest.blocked.length }, null, 2));
}

crawl().catch(async error => {
  console.error(error);
  await writeJson(path.join(OUTPUT_ROOT, 'capture-manifest.partial.json'), state.manifest).catch(() => {});
  process.exit(1);
});
