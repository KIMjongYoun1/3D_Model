"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { checkAdminAuth } from "@/lib/adminApi";

export default function AdminRootPage() {
  const router = useRouter();

  useEffect(() => {
    checkAdminAuth().then((admin) => {
      if (admin) {
        router.replace("/knowledge");
      } else {
        router.replace("/login");
      }
    });
  }, [router]);

  return (
    <div className="min-h-[50vh] flex items-center justify-center">
      <p className="text-slate-400 font-bold">이동 중...</p>
    </div>
  );
}
