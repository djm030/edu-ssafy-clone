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

interface FetchJsonOptions<T> extends RequestInit {
  fallback?: () => T | Promise<T>;
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
      throw new ApiError(response.status, message, payload.error?.code);
    }

    return readJson<T>(response);
  } catch (error) {
    if (fallback) return fallback();
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
