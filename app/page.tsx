"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    // 현재는 메인 기능인 'studio'로 바로 이동하도록 설정합니다.
    // 기존 'universe'에서 'studio'로 테마 명칭 변경 (Apple-Clean Standard)
    router.push("/studio");
  }, [router]);

  return (
    <div className="flex h-screen w-screen items-center justify-center bg-white">
      <div className="animate-pulse font-mono text-slate-300 tracking-[1em]">
        LOADING_STUDIO...
      </div>
    </div>
  );
}
