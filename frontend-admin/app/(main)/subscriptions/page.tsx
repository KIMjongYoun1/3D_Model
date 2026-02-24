"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface SubscriptionItem {
  id: string;
  userId: string;
  planType: string;
  status: string;
  tryonLimit: number | null;
  tryonUsed: number | null;
  startedAt: string | null;
  expiresAt: string | null;
  paymentId: string | null;
  autoRenew: boolean;
  cancelledAt: string | null;
  createdAt: string;
}

interface PageResponse {
  content: SubscriptionItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export default function AdminSubscriptionsPage() {
  useRequireAdminAuth();
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState<string | null>(null);

  const fetchList = async () => {
    try {
      setLoading(true);
      const { data: res } = await adminApi.get<PageResponse>(
        `/api/admin/subscriptions?page=${page}&size=20`
      );
      setData(res);
    } catch (e) {
      console.error("구독 목록 로드 실패:", e);
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, [page]);

  const handleCancel = async (id: string) => {
    if (!confirm("이 구독을 취소하시겠습니까?")) return;
    setActionId(id);
    try {
      await adminApi.post(
        `/api/admin/subscriptions/${id}/cancel`,
        {}
      );
      fetchList();
    } catch (e) {
      const msg =
        axios.isAxiosError(e) && e.response?.data?.error
          ? String(e.response.data.error)
          : "취소 실패";
      alert(msg);
    } finally {
      setActionId(null);
    }
  };

  const list = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  const statusBadge = (status: string) => {
    const map: Record<string, string> = {
      active: "bg-emerald-100 text-emerald-600",
      cancelled: "bg-slate-200 text-slate-600",
      expired: "bg-amber-100 text-amber-600",
    };
    return (
      <span
        className={`text-[10px] font-black px-2 py-0.5 rounded-full uppercase ${
          map[status] ?? "bg-slate-100 text-slate-500"
        }`}
      >
        {status}
      </span>
    );
  };

  return (
    <div className="w-full max-w-[1200px] px-8 py-10 space-y-8">
      <div>
        <h1 className="text-3xl font-black tracking-tighter italic">
          SUBSCRIPTION MANAGEMENT
        </h1>
        <p className="text-slate-500 text-sm mt-2 font-bold uppercase tracking-widest">
          구독 목록 · 취소 처리
        </p>
      </div>

      {loading && list.length === 0 ? (
        <div className="py-20 text-center text-slate-400 font-bold">로딩 중...</div>
      ) : (
        <>
          <div className="rounded-2xl border border-slate-200 bg-white overflow-hidden">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-100 bg-slate-50">
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                    사용자
                  </th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                    플랜
                  </th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                    상태
                  </th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                    사용량
                  </th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                    만료일
                  </th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider w-32">
                    작업
                  </th>
                </tr>
              </thead>
              <tbody>
                {list.map((s) => (
                  <tr
                    key={s.id}
                    className="border-b border-slate-50 hover:bg-slate-50/50"
                  >
                    <td className="px-4 py-3 font-mono text-xs text-slate-500">
                      {s.id.slice(0, 8)}...
                    </td>
                    <td className="px-4 py-3 font-mono text-xs">
                      <Link
                        href={`/members/${s.userId}`}
                        className="text-indigo-600 hover:underline"
                      >
                        {s.userId.slice(0, 8)}...
                      </Link>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-[10px] font-black bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full uppercase">
                        {s.planType || "—"}
                      </span>
                    </td>
                    <td className="px-4 py-3">{statusBadge(s.status)}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {s.tryonLimit != null
                        ? `${s.tryonUsed ?? 0} / ${s.tryonLimit}`
                        : "—"}
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {s.expiresAt
                        ? new Date(s.expiresAt).toLocaleDateString("ko-KR")
                        : "—"}
                    </td>
                    <td className="px-4 py-3">
                      {s.status === "active" && (
                        <button
                          type="button"
                          onClick={() => handleCancel(s.id)}
                          disabled={actionId === s.id}
                          className="text-red-600 font-bold hover:underline text-xs uppercase disabled:opacity-50"
                        >
                          {actionId === s.id ? "처리중" : "취소"}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={page <= 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                이전
              </Button>
              <span className="px-4 py-2 text-sm font-bold text-slate-600">
                {page + 1} / {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              >
                다음
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
