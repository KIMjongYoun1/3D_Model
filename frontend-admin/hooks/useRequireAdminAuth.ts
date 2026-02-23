"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";

/**
 * 관리자 전용 페이지에서 토큰이 없으면 /login으로 리다이렉트합니다.
 * 로그인 후 돌아올 URL은 쿼리 redirect로 전달됩니다.
 */
export function useRequireAdminAuth() {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    const token = typeof window !== "undefined" ? localStorage.getItem("adminToken") : null;
    if (!token) {
      const q = pathname ? `?redirect=${encodeURIComponent(pathname)}` : "";
      router.replace(`/login${q}`);
    }
  }, [router, pathname]);
}
