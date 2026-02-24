"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface PaymentItem {
  id: string;
  userId: string;
  subscriptionId: string | null;
  planId: string | null;
  paymentMethod: string;
  amount: number;
  status: string;
  pgProvider: string;
  pgTransactionId: string | null;
  createdAt: string;
  completedAt: string | null;
  cancelledAt: string | null;
}

interface PageResponse {
  content: PaymentItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export default function AdminTransactionsPage() {
  useRequireAdminAuth();
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState<string | null>(null);
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");

  const fetchList = async () => {
    try {
      setLoading(true);
      let url = `/api/admin/payments?page=${page}&size=20`;
      if (fromDate) url += `&fromDate=${fromDate}`;
      if (toDate) url += `&toDate=${toDate}`;
      const { data: res } = await adminApi.get<PageResponse>(url);
      setData(res);
    } catch (e) {
      console.error("거래 목록 로드 실패:", e);
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, [page, fromDate, toDate]);

  useEffect(() => {
    setPage(0);
  }, [fromDate, toDate]);

  const handleCancel = async (id: string) => {
    if (!confirm("이 결제를 취소하시겠습니까?")) return;
    setActionId(id);
    try {
      await adminApi.post(
        `/api/admin/payments/${id}/cancel`,
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
      completed: "bg-emerald-100 text-emerald-600",
      pending: "bg-amber-100 text-amber-600",
      cancelled: "bg-slate-200 text-slate-600",
      failed: "bg-red-100 text-red-600",
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
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black tracking-tighter italic">
            TRANSACTION MANAGEMENT
          </h1>
          <p className="text-slate-500 text-sm mt-2 font-bold uppercase tracking-widest">
            결제 목록 · 날짜별 조회 · 취소 처리
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            className="px-3 py-2 text-sm border border-slate-200 rounded-xl font-medium"
          />
          <span className="text-slate-400">~</span>
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            className="px-3 py-2 text-sm border border-slate-200 rounded-xl font-medium"
          />
          <button
            type="button"
            onClick={() => { setFromDate(""); setToDate(""); setPage(0); }}
            className="px-3 py-2 text-sm font-bold text-slate-600 border border-slate-200 rounded-xl hover:bg-slate-50"
          >
            초기화
          </button>
        </div>
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
                    금액
                  </th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                    상태
                  </th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                    결제일
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
                    <td className="px-4 py-3 font-mono text-xs text-slate-500">
                      {p.id.slice(0, 8)}...
                    </td>
                    <td className="px-4 py-3 font-mono text-xs">
                      <Link
                        href={`/members/${p.userId}`}
                        className="text-indigo-600 hover:underline"
                      >
                        {p.userId.slice(0, 8)}...
                      </Link>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-[10px] font-black bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full uppercase">
                        {p.planId || "—"}
                      </span>
                    </td>
                    <td className="px-4 py-3 font-bold">
                      ₩{(p.amount ?? 0).toLocaleString()}
                    </td>
                    <td className="px-4 py-3">{statusBadge(p.status)}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {p.completedAt
                        ? new Date(p.completedAt).toLocaleString("ko-KR")
                        : p.createdAt
                          ? new Date(p.createdAt).toLocaleString("ko-KR")
                          : "—"}
                    </td>
                    <td className="px-4 py-3">
                      {p.status === "completed" && (
                        <button
                          type="button"
                          onClick={() => handleCancel(p.id)}
                          disabled={actionId === p.id}
                          className="text-red-600 font-bold hover:underline text-xs uppercase disabled:opacity-50"
                        >
                          {actionId === p.id ? "처리중" : "취소"}
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
