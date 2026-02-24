"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

export default function NewTermsPage() {
  useRequireAdminAuth();
  const router = useRouter();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState({
    type: "TERMS_OF_SERVICE",
    version: "1.0",
    title: "",
    content: "",
    effectiveAt: new Date().toISOString().slice(0, 16),
    category: "SIGNUP" as "SIGNUP" | "PAYMENT",
    required: true,
    isActive: true,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!form.title.trim()) {
      setError("제목을 입력하세요.");
      return;
    }
    setSubmitting(true);
    try {
        await adminApi.post(
        "/api/admin/terms",
        {
          type: form.type,
          version: form.version,
          title: form.title.trim(),
          content: form.content,
          effectiveAt: form.effectiveAt ? form.effectiveAt : null,
          category: form.category,
          required: form.required,
          isActive: form.isActive,
        }
      );
      router.push("/terms");
    } catch (e) {
      const msg = axios.isAxiosError(e) && e.response?.data?.error
        ? String(e.response.data.error)
        : "저장 실패";
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="w-full max-w-[900px] px-8 py-10 space-y-8">
      <div className="flex items-center gap-4">
        <Link
          href="/terms"
          className="text-slate-500 hover:text-slate-900 font-bold text-sm uppercase tracking-widest"
        >
          ← 목록
        </Link>
      </div>

      <h1 className="text-2xl font-black italic">새 약관 등록</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">유형</label>
            <select
              value={form.type}
              onChange={(e) => setForm({ ...form, type: e.target.value })}
              className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-bold focus:outline-none focus:border-indigo-500"
            >
              <option value="TERMS_OF_SERVICE">TERMS_OF_SERVICE (이용약관)</option>
              <option value="PRIVACY_POLICY">PRIVACY_POLICY (개인정보처리방침)</option>
              <option value="SUBSCRIPTION_TERMS">SUBSCRIPTION_TERMS (구독·결제 약관)</option>
              <option value="REFUND_POLICY">REFUND_POLICY (환불 정책)</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">적용 화면</label>
            <select
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value as "SIGNUP" | "PAYMENT" })}
              className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-bold focus:outline-none focus:border-indigo-500"
            >
              <option value="SIGNUP">SIGNUP (가입)</option>
              <option value="PAYMENT">PAYMENT (결제)</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">필수/선택</label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={form.required}
                onChange={(e) => setForm({ ...form, required: e.target.checked })}
                className="h-4 w-4 rounded border-slate-300 text-indigo-600"
              />
              <span className="text-sm font-bold">필수 동의</span>
            </label>
          </div>
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">노출</label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={form.isActive}
                onChange={(e) => setForm({ ...form, isActive: e.target.checked })}
                className="h-4 w-4 rounded border-slate-300 text-indigo-600"
              />
              <span className="text-sm font-bold">Studio에 노출</span>
            </label>
          </div>
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">버전</label>
            <Input
              value={form.version}
              onChange={(e) => setForm({ ...form, version: e.target.value })}
              placeholder="1.0"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-black text-slate-500 uppercase mb-2">제목</label>
          <Input
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            placeholder="예: 이용약관 제1조"
            required
          />
        </div>

        <div>
          <label className="block text-xs font-black text-slate-500 uppercase mb-2">시행일</label>
          <Input
            type="datetime-local"
            value={form.effectiveAt}
            onChange={(e) => setForm({ ...form, effectiveAt: e.target.value })}
          />
        </div>

        <div>
          <label className="block text-xs font-black text-slate-500 uppercase mb-2">
            본문 (HTML 사용 가능)
          </label>
          <textarea
            value={form.content}
            onChange={(e) => setForm({ ...form, content: e.target.value })}
            className="w-full h-64 bg-white border border-slate-200 rounded-2xl px-4 py-3 text-sm font-medium focus:outline-none focus:border-indigo-500 font-mono"
            placeholder="<p>내용을 입력하세요. HTML 태그 사용 가능.</p>"
          />
        </div>

        {error && <p className="text-red-600 text-sm font-bold">{error}</p>}

        <div className="flex items-center gap-4">
          <Button type="submit" variant="primary" disabled={submitting}>
            {submitting ? "저장 중..." : "저장"}
          </Button>
          <Link href="/terms">
            <Button type="button" variant="ghost">취소</Button>
          </Link>
        </div>
      </form>
    </div>
  );
}
