"use client";

import React, { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface TermsDetail {
  id: string;
  type: string;
  title: string;
  version: string;
  content: string;
  effectiveAt: string;
  category?: string;
  required?: boolean;
  isActive?: boolean;
}

export default function EditTermsPage() {
  useRequireAdminAuth();
  const router = useRouter();
  const params = useParams();
  const id = params?.id as string | undefined;

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [form, setForm] = useState({
    type: "TERMS_OF_SERVICE",
    version: "",
    title: "",
    content: "",
    effectiveAt: "",
    category: "SIGNUP" as "SIGNUP" | "PAYMENT",
    required: true,
    isActive: true,
  });

  useEffect(() => {
    if (!id) {
      setLoading(false);
      return;
    }
    const fetchDetail = async () => {
      try {
        setLoading(true);
        const { data } = await adminApi.get<TermsDetail>(
          `/api/admin/terms/${id}`
        );
        setForm({
          type: data.type || "TERMS_OF_SERVICE",
          version: data.version || "",
          title: data.title || "",
          content: data.content || "",
          effectiveAt: data.effectiveAt
            ? new Date(data.effectiveAt).toISOString().slice(0, 16)
            : new Date().toISOString().slice(0, 16),
          category: (data.category === "PAYMENT" ? "PAYMENT" : "SIGNUP") as "SIGNUP" | "PAYMENT",
          required: data.required !== false,
          isActive: data.isActive !== false,
        });
      } catch (e) {
        setLoadError(axios.isAxiosError(e) && e.response?.data?.error
          ? String(e.response.data.error)
          : "로드 실패");
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;
    setSubmitError(null);
    if (!form.title.trim()) {
      setSubmitError("제목을 입력하세요.");
      return;
    }
    setSubmitting(true);
    try {
        await adminApi.put(
        `/api/admin/terms/${id}`,
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
      setSubmitError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="p-10 min-h-[60vh] flex items-center justify-center">
        <p className="text-slate-400 font-bold">로딩 중...</p>
      </div>
    );
  }

  if (!id || loadError) {
    return (
      <div className="p-10 min-h-[60vh] flex flex-col items-center justify-center gap-4">
        <p className="text-red-600 font-bold">{loadError || "잘못된 요청"}</p>
        <Link href="/terms" className="text-indigo-600 font-bold hover:underline">
          ← 목록으로
        </Link>
      </div>
    );
  }

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

      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black italic">약관 편집</h1>
          <p className="text-slate-500 text-sm">
            저장하면 Studio 동의 화면에 즉시 반영됩니다. (재배포 불필요)
          </p>
        </div>
        <button
          type="button"
          onClick={async () => {
            const v = prompt("새 버전 번호 (예: 2.0):");
            if (!v?.trim()) return;
            try {
              await adminApi.post(`/api/admin/terms/${id}/new-version`, {
                version: v.trim(),
                effectiveAt: new Date().toISOString().slice(0, 16),
              });
              alert("새 버전이 등록되었습니다. 목록에서 확인하세요.");
              router.push("/terms");
            } catch (e: unknown) {
              const msg = axios.isAxiosError(e) && e.response?.data?.error
                ? String(e.response.data.error)
                : "등록 실패";
              alert(msg);
            }
          }}
          className="px-4 py-2 text-sm font-bold border border-indigo-300 text-indigo-600 rounded-xl hover:bg-indigo-50"
        >
          + 새 버전 등록
        </button>
      </div>

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

        {submitError && <p className="text-red-600 text-sm font-bold">{submitError}</p>}

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
