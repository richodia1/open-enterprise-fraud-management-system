export const apiBaseUrl = normalizeBaseUrl(
  import.meta.env.VITE_OEFMS_API_BASE_URL ?? 'http://localhost:8080',
)

export class ApiError extends Error {
  readonly detail?: unknown
  readonly status: number

  constructor(message: string, status: number, detail?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.detail = detail
  }
}

export type RequestOptions = {
  body?: unknown
  method?: 'DELETE' | 'GET' | 'PATCH' | 'POST' | 'PUT'
  searchParams?: Record<string, boolean | number | string | undefined>
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const url = new URL(path, `${apiBaseUrl}/`)
  Object.entries(options.searchParams ?? {}).forEach(([key, value]) => {
    if (value !== undefined) {
      url.searchParams.set(key, String(value))
    }
  })

  const response = await fetch(url, {
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'X-Correlation-Id': correlationId(),
    },
    method: options.method ?? 'GET',
  })

  if (!response.ok) {
    throw await toApiError(response)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

function correlationId() {
  if (globalThis.crypto && 'randomUUID' in globalThis.crypto) {
    return globalThis.crypto.randomUUID()
  }
  return `web-console-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function normalizeBaseUrl(value: string) {
  return value.replace(/\/+$/, '')
}

async function toApiError(response: Response) {
  let detail: unknown
  try {
    detail = await response.json()
  } catch {
    detail = await response.text()
  }

  const fallback = `Request failed with status ${response.status}`
  const message =
    detail && typeof detail === 'object' && 'detail' in detail
      ? String((detail as { detail?: unknown }).detail ?? fallback)
      : fallback

  return new ApiError(message, response.status, detail)
}
