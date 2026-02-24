"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

export default function NewPlanPage() {
  useRequireAdminAuth();
  const router = useRouter();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState({
    planCode: "",
    planName: "",
    priceMonthly: 0,
    tokenLimit: "" as string | number,
    description: "",
    featuresText: "",
    isActive: true,
    sortOrder: 0,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!form.planCode.trim()) {
      setError("플랜 코드를 입력하세요. (예: pro, premium)");
      return;
    }
    if (!form.planName.trim()) {
      setError("플랜 이름을 입력하세요.");
      return;
    }
    setSubmitting(true);
    try {
      const features = form.featuresText
        .split("\n")
        .map((s) => s.trim())
        .filter(Boolean);
      await adminApi.post("/api/admin/plans", {
        planCode: form.planCode.trim().toLowerCase(),
        planName: form.planName.trim(),
        priceMonthly: form.priceMonthly,
        tokenLimit:
          form.tokenLimit === "" || form.tokenLimit === null
            ? null
            : Number(form.tokenLimit),
        description: form.description || null,
        features: features.length > 0 ? features : null,
        isActive: form.isActive,
        sortOrder: form.sortOrder,
      });
      router.push("/plans");
    } catch (e) {
      const msg =
        axios.isAxiosError(e) && e.response?.data?.error
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
          href="/plans"
          className="text-slate-500 hover:text-slate-900 font-bold text-sm uppercase tracking-widest"
        >
          ← 목록
        </Link>
      </div>

      <h1 className="text-2xl font-black italic">새 플랜 등록</h1>
      <p className="text-slate-500 text-sm">
        등록한 플랜은 Studio 결제 화면에 즉시 노출됩니다. (활성 플랜만)
      </p>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">
              플랜 코드 (영문, 소문자)
            </label>
            <Input
              value={form.planCode}
              onChange={(e) =>
                setForm({ ...form, planCode: e.target.value.replace(/\s/g, "").toLowerCase() })
              }
              placeholder="pro, premium, enterprise"
              required
            />
            <p className="text-[10px] text-slate-400 mt-1">
              결제 시 planId로 사용됩니다. 등록 후 변경 불가.
            </p>
          </div>
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">
              플랜 이름
            </label>
            <Input
              value={form.planName}
              onChange={(e) => setForm({ ...form, planName: e.target.value })}
              placeholder="Pro Plan"
              required
            />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">
              월 요금 (원)
            </label>
            <Input
              type="number"
              value={String(form.priceMonthly)}
              onChange={(e) =>
                setForm({ ...form, priceMonthly: Number(e.target.value) || 0 })
              }
              placeholder="29000"
            />
          </div>
          <div>
            <label className="block text-xs font-black text-slate-500 uppercase mb-2">
              토큰 한도 (비워두면 무제한)
            </label>
            <Input
              type="number"
              value={String(form.tokenLimit)}
              onChange={(e) =>
                setForm({
                  ...form,
                  tokenLimit: e.target.value === "" ? "" : Number(e.target.value),
                })
              }
              placeholder="100"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-black text-slate-500 uppercase mb-2">
            정렬 순서
          </label>
          <Input
            type="number"
            value={String(form.sortOrder)}
            onChange={(e) =>
              setForm({ ...form, sortOrder: Number(e.target.value) || 0 })
            }
          />
        </div>

        <div>
          <label className="block text-xs font-black text-slate-500 uppercase mb-2">
            설명
          </label>
          <textarea
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            className="w-full h-20 bg-white border border-slate-200 rounded-2xl px-4 py-3 text-sm font-medium focus:outline-none focus:border-indigo-500"
            placeholder="플랜 설명"
          />
        </div>

        <div>
          <label className="block text-xs font-black text-slate-500 uppercase mb-2">
            기능 목록 (한 줄에 하나씩)
          </label>
          <textarea
            value={form.featuresText}
            onChange={(e) => setForm({ ...form, featuresText: e.target.value })}
            className="w-full h-32 bg-white border border-slate-200 rounded-2xl px-4 py-3 text-sm font-medium focus:outline-none focus:border-indigo-500 font-mono"
            placeholder="무제한 3D 시각화&#10;AI 분석 무제한&#10;데이터 영구 보관"
          />
        </div>

        <div>
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={form.isActive}
              onChange={(e) => setForm({ ...form, isActive: e.target.checked })}
              className="h-4 w-4 rounded border-slate-300 text-indigo-600"
            />
            <span className="text-sm font-bold">활성 (Studio 결제 화면에 노출)</span>
          </label>
        </div>

        {error && <p className="text-red-600 text-sm font-bold">{error}</p>}

        <div className="flex items-center gap-4">
          <Button type="submit" variant="primary" disabled={submitting}>
            {submitting ? "저장 중..." : "저장"}
          </Button>
          <Link href="/plans">
            <Button type="button" variant="ghost">
              취소
            </Button>
          </Link>
        </div>
      </form>
    </div>
  );
}
