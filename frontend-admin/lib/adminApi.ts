/**
 * Admin API 클라이언트 (HttpOnly 쿠키 사용)
 * - withCredentials: true 필수 (쿠키 전송/수신)
 * - 토큰은 localStorage에 저장하지 않음
 */
import axios, { AxiosInstance } from "axios";

const ADMIN_API = process.env.NEXT_PUBLIC_ADMIN_API_URL || "http://localhost:8081";

export const adminApi: AxiosInstance = axios.create({
  baseURL: ADMIN_API,
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});

export interface AdminUser {
  adminId: string;
  email: string;
  name: string;
  role: string;
  isActive?: boolean;
  lastLoginAt?: string;
  createdAt?: string;
}

export async function checkAdminAuth(): Promise<AdminUser | null> {
  try {
    const { data } = await adminApi.get<AdminUser>("/api/admin/auth/me");
    return data;
  } catch {
    return null;
  }
}

export async function adminLogout(): Promise<void> {
  await adminApi.post("/api/admin/auth/logout");
}
