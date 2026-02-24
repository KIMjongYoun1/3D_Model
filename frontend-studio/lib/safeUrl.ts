/**
 * 외부 링크용 URL 검증 (References, Evidence 등)
 * - http/https만 허용
 * - javascript:, data:, file: 등 차단
 * - MITM으로 조작된 응답에서 악성 URL이 와도 클릭 시 안전
 */
const MAX_URL_LENGTH = 2048;

export function isSafeExternalUrl(url: string | null | undefined): boolean {
  if (!url || typeof url !== 'string') return false;
  const trimmed = url.trim();
  if (trimmed.length === 0 || trimmed.length > MAX_URL_LENGTH) return false;
  try {
    const parsed = new URL(trimmed);
    return parsed.protocol === 'http:' || parsed.protocol === 'https:';
  } catch {
    return false;
  }
}

/**
 * 검증된 URL 반환. 유효하지 않으면 null (링크 비활성화)
 */
export function getSafeExternalUrl(url: string | null | undefined): string | null {
  const trimmed = (url ?? '').trim();
  return isSafeExternalUrl(trimmed) ? trimmed : null;
}
