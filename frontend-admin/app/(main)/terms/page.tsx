"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface TermsItem {
  id: string;
  type: string;
  title: string;
  version: string;
  content?: string;
  effectiveAt: string;
}

function getAdminAuthHeaders(): Record<string, string> {
  if (typeof window === "undefined") return {};
  const token = localStorage.getItem("adminToken");
  if (!token) return {};
  return { Authorization: `Bearer ${token}` };
}

export default function AdminTermsListPage() {
  useRequireAdminAuth();
  const [list, setList] = useState<TermsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchList = async () => {
    try {
      setLoading(true);
      const { data } = await axios.get<TermsItem[]>(
        `${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/terms`,
        { headers: getAdminAuthHeaders() }
      );
      setList(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error("약관 목록 로드 실패:", e);
      setList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm("정말 삭제하시겠습니까? Studio 동의 화면에서 이 약관이 사라집니다.")) return;
    setDeletingId(id);
    try {
      await axios.delete(
        `${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/terms/${id}`,
        { headers: getAdminAuthHeaders() }
      );
      fetchList();
    } catch (e) {
      const msg = axios.isAxiosError(e) && e.response?.data?.error
        ? String(e.response.data.error)
        : "삭제 실패";
      alert(msg);
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="w-full max-w-[1200px] px-8 py-10 space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black tracking-tighter italic">TERMS MANAGEMENT</h1>
          <p className="text-slate-500 text-sm mt-2 font-bold uppercase tracking-widest">
            수정 후 저장하면 Studio 동의 화면에 즉시 반영됩니다 (재배포 불필요)
          </p>
        </div>
        <Link href="/terms/new">
          <Button variant="primary" className="whitespace-nowrap">
            + 새 약관 등록
          </Button>
        </Link>
      </div>

      {loading && list.length === 0 ? (
        <div className="py-20 text-center text-slate-400 font-bold">로딩 중...</div>
      ) : list.length === 0 ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center">
          <p className="text-slate-500 font-bold">등록된 약관이 없습니다.</p>
          <p className="text-slate-400 text-sm mt-2">
            새 약관을 등록하면 Studio 동의 화면에서 사용됩니다.
          </p>
          <Link href="/terms/new" className="inline-block mt-4">
            <Button variant="primary">+ 새 약관 등록</Button>
          </Link>
        </div>
      ) : (
        <div className="rounded-2xl border border-slate-200 bg-white overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">유형</th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">제목</th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">버전</th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">시행일</th>
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider w-32">작업</th>
              </tr>
            </thead>
            <tbody>
              {list.map((t) => (
                <tr key={t.id} className="border-b border-slate-50 hover:bg-slate-50/50">
                  <td className="px-4 py-3">
                    <span className="text-[10px] font-black bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full uppercase">
                      {t.type}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-bold">{t.title}</td>
                  <td className="px-4 py-3 text-slate-600">{t.version}</td>
                  <td className="px-4 py-3 text-slate-600">
                    {t.effectiveAt ? new Date(t.effectiveAt).toLocaleDateString("ko-KR") : "—"}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <Link
                        href={`/terms/${t.id}/edit`}
                        className="text-indigo-600 font-bold hover:underline text-xs uppercase"
                      >
                        편집
                      </Link>
                      <button
                        type="button"
                        onClick={() => handleDelete(t.id)}
                        disabled={deletingId === t.id}
                        className="text-red-600 font-bold hover:underline text-xs uppercase disabled:opacity-50"
                      >
                        {deletingId === t.id ? "삭제중" : "삭제"}
                      </button>
                    </div>
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
