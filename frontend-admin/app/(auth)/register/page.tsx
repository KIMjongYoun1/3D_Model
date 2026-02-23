"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";

const API_URL = process.env.NEXT_PUBLIC_ADMIN_API_URL || "http://localhost:8081";

const ROLES = [
  { value: "ADMIN", label: "ADMIN" },
  { value: "SUPER_ADMIN", label: "SUPER_ADMIN" },
  { value: "OPERATOR", label: "OPERATOR" },
];

export default function AdminRegisterPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState("ADMIN");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await axios.post(`${API_URL}/api/admin/auth/register`, {
        email,
        password,
        name: name || email,
        role,
      });
      router.push("/login?redirect=/knowledge");
      router.refresh();
    } catch (err: unknown) {
      const res = err && typeof err === "object" && "response" in err ? (err as { response?: { data?: { error?: string } } }).response : null;
      setError(res?.data?.error || "가입에 실패했습니다. 이메일 중복 여부를 확인하세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[70vh] flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white rounded-[2rem] border border-slate-200 shadow-lg p-10">
        <h1 className="text-2xl font-black tracking-tighter italic text-slate-900">
          ADMIN <span className="text-indigo-600">가입</span>
        </h1>
        <p className="text-slate-500 text-sm mt-1 font-bold uppercase tracking-widest">관리자 계정 생성</p>

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
          <Input
            type="text"
            label="이름"
            placeholder="홍길동"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <div className="space-y-2">
            <label className="block text-[10px] font-black text-slate-500 uppercase tracking-widest px-1">역할</label>
            <select
              value={role}
              onChange={(e) => setRole(e.target.value)}
              className="w-full px-6 py-4 rounded-2xl bg-slate-50 border border-slate-200 text-sm font-bold focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-400"
            >
              {ROLES.map((r) => (
                <option key={r.value} value={r.value}>{r.label}</option>
              ))}
            </select>
          </div>
          {error && (
            <p className="text-sm text-red-600 font-bold">{error}</p>
          )}
          <Button type="submit" variant="primary" className="w-full py-4" disabled={loading}>
            {loading ? "가입 중..." : "가입하기"}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          이미 계정이 있으신가요?{" "}
          <Link href="/login" className="font-bold text-indigo-600 hover:underline">
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
}
