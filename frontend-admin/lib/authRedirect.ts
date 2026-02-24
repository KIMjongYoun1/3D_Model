/**
 * Open Redirect 방지를 위한 redirect 경로 검증
 * - 동일 출처 경로만 허용 (/, /knowledge, /members 등)
 * - 외부 URL, javascript:, data: 등 차단
 */
const MAX_PATH_LENGTH = 256;

export function isValidRedirect(path: string | null | undefined): boolean {
  if (!path || typeof path !== 'string') return false;
  const trimmed = path.trim();
  if (trimmed.length === 0 || trimmed.length > MAX_PATH_LENGTH) return false;
  if (!trimmed.startsWith('/')) return false;
  if (trimmed.startsWith('//')) return false; // protocol-relative (//evil.com)
  if (trimmed.includes('://')) return false;   // https://, javascript:
  if (/javascript\s*:/i.test(trimmed)) return false;
  if (/data\s*:/i.test(trimmed)) return false;
  if (trimmed.includes('..')) return false;    // path traversal
  if (/[\s\\<>"']/.test(trimmed)) return false; // 위험 문자
  return true;
}

/**
 * 검증된 redirect 경로 반환. 유효하지 않으면 fallback 사용
 */
export function getSafeRedirect(path: string | null | undefined, fallback: string): string {
  const trimmed = (path ?? '').trim();
  return isValidRedirect(trimmed) ? trimmed : fallback;
}
