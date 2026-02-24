"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/Button";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface PlanItem {
  id: string;
  planCode: string;
  planName: string;
  priceMonthly: number;
  tokenLimit: number | null;
  description: string | null;
  features: string[] | null;
  isActive: boolean;
  sortOrder: number;
}

export default function AdminPlansPage() {
  useRequireAdminAuth();
  const [list, setList] = useState<PlanItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [togglingId, setTogglingId] = useState<string | null>(null);

  const fetchList = async () => {
    try {
      setLoading(true);
      const { data } = await adminApi.get<PlanItem[]>(
        "/api/admin/plans"
      );
      setList(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error("플랜 목록 로드 실패:", e);
      setList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, []);

  const handleToggleActive = async (p: PlanItem) => {
    setTogglingId(p.id);
    try {
      await adminApi.put(`/api/admin/plans/${p.id}`, { isActive: !p.isActive });
      fetchList();
    } catch (e) {
      console.error("노출 설정 변경 실패:", e);
      alert("변경에 실패했습니다.");
    } finally {
      setTogglingId(null);
    }
  };

  return (
    <div className="w-full max-w-[1200px] px-8 py-10 space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black tracking-tighter italic">
            PLAN MANAGEMENT
          </h1>
          <p className="text-slate-500 text-sm mt-2 font-bold uppercase tracking-widest">
            요금제 설정 · 등록 시 Studio 결제 화면에 즉시 반영
          </p>
        </div>
        <Link href="/plans/new">
          <Button variant="primary" className="whitespace-nowrap">
            + 새 플랜 등록
          </Button>
        </Link>
      </div>

      {loading && list.length === 0 ? (
        <div className="py-20 text-center text-slate-400 font-bold">로딩 중...</div>
      ) : list.length === 0 ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center">
          <p className="text-slate-500 font-bold">등록된 플랜이 없습니다.</p>
          <p className="text-slate-400 text-sm mt-2">
            새 플랜을 등록하면 Studio 결제 화면에서 선택할 수 있습니다.
          </p>
          <Link href="/plans/new" className="inline-block mt-4">
            <Button variant="primary">+ 새 플랜 등록</Button>
          </Link>
        </div>
      ) : (
        <div className="rounded-2xl border border-slate-200 bg-white overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                  코드
                </th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                  이름
                </th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                  월 요금
                </th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                  토큰
                </th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                  노출
                </th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider w-32">
                  작업
                </th>
              </tr>
            </thead>
            <tbody>
              {list.map((p) => (
                <tr
                  key={p.id}
                  className="border-b border-slate-50 hover:bg-slate-50/50"
                >
                  <td className="px-4 py-3">
                    <span className="text-[10px] font-black bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full uppercase">
                      {p.planCode}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-bold">{p.planName}</td>
                  <td className="px-4 py-3 font-bold">
                    ₩{(p.priceMonthly ?? 0).toLocaleString()}
                  </td>
                  <td className="px-4 py-3 text-slate-600">
                    {p.tokenLimit != null ? p.tokenLimit : "—"}
                  </td>
                  <td className="px-4 py-3">
                    <button
                      type="button"
                      onClick={() => handleToggleActive(p)}
                      disabled={togglingId === p.id}
                      className={`text-[10px] font-black px-2 py-0.5 rounded-full uppercase transition-colors disabled:opacity-50 ${
                        p.isActive ? "bg-emerald-100 text-emerald-600 hover:bg-emerald-200" : "bg-slate-200 text-slate-500 hover:bg-slate-300"
                      }`}
                      title={p.isActive ? "클릭 시 미노출" : "클릭 시 노출"}
                    >
                      {togglingId === p.id ? "..." : p.isActive ? "노출" : "미노출"}
                    </button>
                  </td>
                  <td className="px-4 py-3">
                    <Link
                      href={`/plans/${p.id}/edit`}
                      className="text-indigo-600 font-bold hover:underline text-xs uppercase"
                    >
                      편집
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
