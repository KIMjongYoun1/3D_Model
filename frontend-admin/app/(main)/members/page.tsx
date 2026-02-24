"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface MemberItem {
  id: string;
  email: string;
  name: string;
  provider: string;
  subscription: string;
  createdAt: string;
  suspendedAt: string | null;
  paymentCount?: number;
  subscriptionCount?: number;
}

interface PageResponse {
  content: MemberItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export default function AdminMembersPage() {
  useRequireAdminAuth();
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState<string | null>(null);

  const fetchList = async () => {
    try {
      setLoading(true);
      const { data: res } = await adminApi.get<PageResponse>(
        `/api/admin/members?page=${page}&size=20`
      );
      setData(res);
    } catch (e) {
      console.error("회원 목록 로드 실패:", e);
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, [page]);

  const handleSuspend = async (id: string) => {
    if (!confirm("해당 회원을 정지하시겠습니까? 로그인이 제한됩니다.")) return;
    setActionId(id);
    try {
      await adminApi.post(
        `/api/admin/members/${id}/suspend`,
        {}
      );
      fetchList();
    } catch (e) {
      const msg = axios.isAxiosError(e) && e.response?.data?.error
        ? String(e.response.data.error)
        : "정지 실패";
      alert(msg);
    } finally {
      setActionId(null);
    }
  };

  const handleUnsuspend = async (id: string) => {
    if (!confirm("정지 해제하시겠습니까?")) return;
    setActionId(id);
    try {
      await adminApi.post(
        `/api/admin/members/${id}/unsuspend`,
        {}
      );
      fetchList();
    } catch (e) {
      const msg = axios.isAxiosError(e) && e.response?.data?.error
        ? String(e.response.data.error)
        : "해제 실패";
      alert(msg);
    } finally {
      setActionId(null);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("회원 탈퇴 처리(소프트 삭제)를 진행하시겠습니까? 복구할 수 없습니다.")) return;
    setActionId(id);
    try {
      await adminApi.delete(
        `/api/admin/members/${id}`
      );
      fetchList();
    } catch (e) {
      const msg = axios.isAxiosError(e) && e.response?.data?.error
        ? String(e.response.data.error)
        : "탈퇴 처리 실패";
      alert(msg);
    } finally {
      setActionId(null);
    }
  };

  const list = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <div className="w-full max-w-[1200px] px-8 py-10 space-y-8">
      <div>
        <h1 className="text-3xl font-black tracking-tighter italic">MEMBER MANAGEMENT</h1>
        <p className="text-slate-500 text-sm mt-2 font-bold uppercase tracking-widest">
          회원 목록 · 정지/해제/탈퇴 처리
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
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">이메일</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">이름</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">가입일</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">구독</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">상태</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider w-48">작업</th>
                </tr>
              </thead>
              <tbody>
                {list.map((m) => (
                  <tr key={m.id} className="border-b border-slate-50 hover:bg-slate-50/50">
                    <td className="px-4 py-3 font-bold">{m.email}</td>
                    <td className="px-4 py-3">{m.name || "—"}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {m.createdAt ? new Date(m.createdAt).toLocaleDateString("ko-KR") : "—"}
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-[10px] font-black bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full uppercase">
                        {m.subscription || "free"}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      {m.suspendedAt ? (
                        <span className="text-amber-600 font-bold text-xs">정지</span>
                      ) : (
                        <span className="text-emerald-600 font-bold text-xs">활성</span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        <Link
                          href={`/members/${m.id}`}
                          className="text-indigo-600 font-bold hover:underline text-xs uppercase"
                        >
                          상세
                        </Link>
                        {m.suspendedAt ? (
                          <button
                            type="button"
                            onClick={() => handleUnsuspend(m.id)}
                            disabled={actionId === m.id}
                            className="text-blue-600 font-bold hover:underline text-xs uppercase disabled:opacity-50"
                          >
                            해제
                          </button>
                        ) : (
                          <button
                            type="button"
                            onClick={() => handleSuspend(m.id)}
                            disabled={actionId === m.id}
                            className="text-amber-600 font-bold hover:underline text-xs uppercase disabled:opacity-50"
                          >
                            정지
                          </button>
                        )}
                        <button
                          type="button"
                          onClick={() => handleDelete(m.id)}
                          disabled={actionId === m.id}
                          className="text-red-600 font-bold hover:underline text-xs uppercase disabled:opacity-50"
                        >
                          탈퇴
                        </button>
                      </div>
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
