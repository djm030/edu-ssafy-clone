import type { ApiErrorPayload } from '../types';

export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;

  constructor(status: number, message: string, code?: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

export const AUTH_REQUIRED_EVENT = 'edussafy:auth-required';
export const FORBIDDEN_EVENT = 'edussafy:forbidden';

interface FetchJsonOptions<T> extends RequestInit {
  fallback?: () => T | Promise<T>;
}

type ViteEnv = Record<string, string | boolean | undefined>;

function isTruthyEnv(value: string | boolean | undefined): boolean {
  return value === true || value === 'true' || value === '1' || value === 'yes';
}

function ciEnvEnabled(): boolean {
  if (typeof process === 'undefined') return false;
  return isTruthyEnv(process.env.CI);
}

function apiFallbackDisabled(): boolean {
  const env = (import.meta as ImportMeta & { env?: ViteEnv }).env || {};
  const fallbackMode = env.VITE_API_FALLBACK;
  const fallbackModeEnabled = fallbackMode === 'enabled' || fallbackMode === 'on' || isTruthyEnv(fallbackMode);
  const fallbackModeDisabled = fallbackMode === 'disabled' || fallbackMode === 'off';

  if (isTruthyEnv(env.VITE_DISABLE_API_FALLBACK) || fallbackModeDisabled) return true;
  if (isTruthyEnv(env.CI) || ciEnvEnabled() || env.PROD === true || env.MODE === 'production') return true;

  // Dev 기본값은 실제 API 호출 우선. 데모 fallback은 명시적으로 켠 경우에만 허용한다.
  return !fallbackModeEnabled;
}

function shouldUseFallback(error: unknown): boolean {
  if (apiFallbackDisabled()) return false;
  if (error instanceof ApiError) {
    if (error.status === 401 || error.status === 403) return false;
    return error.status >= 500;
  }
  return true;
}

function dispatchAccessEvent(error: ApiError): void {
  if (typeof window === 'undefined') return;
  if (error.status === 401) {
    window.dispatchEvent(new CustomEvent(AUTH_REQUIRED_EVENT, { detail: { message: error.message } }));
  }
  if (error.status === 403) {
    window.dispatchEvent(new CustomEvent(FORBIDDEN_EVENT, { detail: { message: error.message } }));
  }
}

function warnFallback(url: string, error: unknown): void {
  const reason = error instanceof Error ? error.message : 'unknown error';
  console.warn(`[api:fallback] Using local demo data for ${url}: ${reason}`);
}

async function readJson<T>(response: Response): Promise<T> {
  const contentType = response.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) return {} as T;

  return response.json() as Promise<T>;
}

function mergeHeaders(headers?: HeadersInit): HeadersInit {
  return {
    Accept: 'application/json',
    ...(headers || {}),
  };
}

export async function fetchJson<T>(url: string, options: FetchJsonOptions<T> = {}): Promise<T> {
  const { fallback, headers, ...init } = options;

  try {
    const response = await fetch(url, {
      credentials: 'include',
      ...init,
      headers: mergeHeaders(headers),
    });

    if (!response.ok) {
      const payload = await readJson<ApiErrorPayload>(response);
      const message = payload.error?.message || fallbackMessage(response.status);
      const apiError = new ApiError(response.status, message, payload.error?.code);
      dispatchAccessEvent(apiError);
      throw apiError;
    }

    return readJson<T>(response);
  } catch (error) {
    if (fallback && shouldUseFallback(error)) {
      warnFallback(url, error);
      return fallback();
    }
    throw error;
  }
}

export function buildQuery(params: Record<string, string | number | undefined>): string {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      searchParams.set(key, String(value));
    }
  });

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function fallbackMessage(status: number): string {
  if (status === 400) return '요청 조건을 확인해 주세요.';
  if (status === 401) return '로그인이 필요한 화면입니다.';
  if (status === 403) return '접근 권한이 없습니다.';
  if (status === 404) return '요청한 데이터를 찾을 수 없습니다.';
  if (status >= 500) return '서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.';
  return '요청을 처리하지 못했습니다.';
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof Error) return error.message;
  return '알 수 없는 오류가 발생했습니다.';
}
