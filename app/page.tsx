"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function RootLandingPage() {
  const router = useRouter();

  // 현재는 메인 기능인 'universe'로 바로 이동하도록 설정합니다.
  // 추후에 여기서 서비스 소개(Landing) 페이지를 보여줄 수 있습니다.
  useEffect(() => {
    router.push("/universe");
  }, [router]);

  return (
    <div className="min-h-screen bg-black flex items-center justify-center">
      <div className="text-center space-y-4">
        <div className="w-16 h-16 border-t-2 border-blue-500 rounded-full animate-spin mx-auto" />
        <p className="text-blue-400 font-mono tracking-widest animate-pulse">CONNECTING TO QUANTUM CORE...</p>
      </div>
    </div>
  );
}
