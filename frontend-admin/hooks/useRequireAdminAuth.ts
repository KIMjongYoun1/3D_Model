"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { checkAdminAuth } from "@/lib/adminApi";

/**
 * 관리자 전용 페이지에서 인증이 없으면 /login으로 리다이렉트합니다.
 * HttpOnly 쿠키 기반으로 /api/admin/auth/me 호출해 인증 여부 확인.
 */
export function useRequireAdminAuth() {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    let cancelled = false;
    checkAdminAuth().then((admin) => {
      if (!cancelled && !admin) {
        const q = pathname ? `?redirect=${encodeURIComponent(pathname)}` : "";
        router.replace(`/login${q}`);
      }
    });
    return () => { cancelled = true; };
  }, [router, pathname]);
}
