/**
 * Java 인증 API 클라이언트 (HttpOnly 쿠키 사용)
 * - withCredentials: true 필수 (쿠키 전송/수신)
 * - 토큰은 localStorage에 저장하지 않음
 */
import axios, { AxiosInstance } from 'axios';

const SERVICE_API = process.env.NEXT_PUBLIC_SERVICE_API_URL || 'http://localhost:8080';

export const authApi: AxiosInstance = axios.create({
  baseURL: SERVICE_API,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

export interface AuthUser {
  id: string;
  email: string;
  name: string;
  profileImage?: string;
  subscription?: string;
  subscriptionPlanName?: string;
  subscriptionExpiresAt?: string;
  /** 구독 상태: active(정상), cancelled(해지신청됨), null(무료/없음) */
  subscriptionStatus?: string | null;
  createdAt?: string;
  provider?: string;
}

export async function checkAuth(): Promise<AuthUser | null> {
  try {
    const { data } = await authApi.get('/api/v1/auth/me');
    return data;
  } catch {
    return null;
  }
}

export async function logout(): Promise<void> {
  await authApi.post('/api/v1/auth/logout');
}
