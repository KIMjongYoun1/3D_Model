"use client";

import React, { Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { getSafeRedirect } from "@/lib/authRedirect";
import { adminApi } from "@/lib/adminApi";

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectTo = getSafeRedirect(searchParams.get("redirect"), "/knowledge");

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await adminApi.post<{
        adminId: string;
        email: string;
        name: string;
        role: string;
      }>("/api/admin/auth/login", { email, password });
      window.dispatchEvent(new Event("storage"));
      router.push(redirectTo);
      router.refresh();
    } catch (err: unknown) {
      const res = err && typeof err === "object" && "response" in err ? (err as { response?: { data?: { error?: string } } }).response : null;
      setError(res?.data?.error || "로그인에 실패했습니다. 이메일과 비밀번호를 확인하세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[70vh] flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white rounded-[2rem] border border-slate-200 shadow-lg p-10">
        <h1 className="text-2xl font-black tracking-tighter italic text-slate-900">
          ADMIN <span className="text-indigo-600">LOGIN</span>
        </h1>
        <p className="text-slate-500 text-sm mt-1 font-bold uppercase tracking-widest">관리자 전용</p>
        <p className="text-slate-400 text-[11px] mt-2">
          로그인 후 왼쪽 사이드바에서 회원·거래·구독·대시보드·플랜·약관 등을 관리할 수 있습니다.
        </p>

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          <Input
            type="email"
            label="Email"
            placeholder="admin@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <Input
            type="password"
            label="Password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          {error && (
            <p className="text-sm text-red-600 font-bold">{error}</p>
          )}
          <Button type="submit" variant="primary" className="w-full py-4" disabled={loading}>
            {loading ? "로그인 중..." : "로그인"}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          계정이 없으신가요?{" "}
          <Link href="/register" className="font-bold text-indigo-600 hover:underline">
            가입하기
          </Link>
        </p>
      </div>
    </div>
  );
}

export default function AdminLoginPage() {
  return (
    <Suspense fallback={<div className="min-h-[70vh] flex items-center justify-center"><p className="text-slate-400 font-bold">로딩 중...</p></div>}>
      <LoginForm />
    </Suspense>
  );
}
