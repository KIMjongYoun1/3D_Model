"use client";

import React, { useState, useEffect } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import axios from "axios";
import { Button } from "@/components/ui/Button";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface MemberDetail {
  id: string;
  email: string;
  name: string;
  provider: string;
  subscription: string;
  createdAt: string;
  suspendedAt: string | null;
  deletedAt: string | null;
  paymentCount?: number;
  subscriptionCount?: number;
}

interface PaymentItem {
  id: string;
  userId: string;
  planId: string | null;
  amount: number;
  status: string;
  createdAt: string;
  completedAt: string | null;
  cancelledAt: string | null;
}

interface SubscriptionItem {
  id: string;
  userId: string;
  planType: string;
  status: string;
  tryonLimit: number | null;
  tryonUsed: number | null;
  startedAt: string | null;
  expiresAt: string | null;
  cancelledAt: string | null;
  createdAt: string;
}

interface PageResponse<T> {
  content: T[];
  totalElements: number;
}

export default function AdminMemberDetailPage() {
  useRequireAdminAuth();
  const params = useParams();
  const id = params?.id as string | undefined;

  const [member, setMember] = useState<MemberDetail | null>(null);
  const [payments, setPayments] = useState<PaymentItem[]>([]);
  const [subscriptions, setSubscriptions] = useState<SubscriptionItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionId, setActionId] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      setLoading(false);
      return;
    }
    const fetchAll = async () => {
      try {
        setLoading(true);
        const [memberRes, paymentsRes, subscriptionsRes] = await Promise.all([
          adminApi.get<MemberDetail>(`/api/admin/members/${id}`),
          adminApi.get<PageResponse<PaymentItem>>(`/api/admin/payments?userId=${id}&page=0&size=50`),
          adminApi.get<PageResponse<SubscriptionItem>>(`/api/admin/subscriptions?userId=${id}&page=0&size=50`),
        ]);
        setMember(memberRes.data);
        setPayments(paymentsRes.data.content ?? []);
        setSubscriptions(subscriptionsRes.data.content ?? []);
      } catch (e) {
        setLoadError(
          axios.isAxiosError(e) && e.response?.status === 404
            ? "회원을 찾을 수 없습니다."
            : "로드 실패"
        );
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [id]);

  const handleSuspend = async () => {
    if (!id || !confirm("해당 회원을 정지하시겠습니까?")) return;
    setActionId("suspend");
    try {
      await adminApi.post(`/api/admin/members/${id}/suspend`, {});
      const { data } = await adminApi.get<MemberDetail>(`/api/admin/members/${id}`);
      setMember(data);
    } catch (e) {
      const msg =
        axios.isAxiosError(e) && e.response?.data?.error
          ? String(e.response.data.error)
          : "정지 실패";
      alert(msg);
    } finally {
      setActionId(null);
    }
  };

  const handleUnsuspend = async () => {
    if (!id || !confirm("정지 해제하시겠습니까?")) return;
    setActionId("unsuspend");
    try {
      await adminApi.post(`/api/admin/members/${id}/unsuspend`, {});
      const { data } = await adminApi.get<MemberDetail>(`/api/admin/members/${id}`);
      setMember(data);
    } catch (e) {
      const msg =
        axios.isAxiosError(e) && e.response?.data?.error
          ? String(e.response.data.error)
          : "해제 실패";
      alert(msg);
    } finally {
      setActionId(null);
    }
  };

  const handleDelete = async () => {
    if (!id || !confirm("회원 탈퇴 처리(소프트 삭제)를 진행하시겠습니까? 복구할 수 없습니다."))
      return;
    setActionId("delete");
    try {
      await adminApi.delete(`/api/admin/members/${id}`);
      setMember((m) => (m ? { ...m, deletedAt: new Date().toISOString() } : null));
    } catch (e) {
      const msg =
        axios.isAxiosError(e) && e.response?.data?.error
          ? String(e.response.data.error)
          : "탈퇴 처리 실패";
      alert(msg);
    } finally {
      setActionId(null);
    }
  };

  const statusBadge = (status: string, type: "payment" | "subscription") => {
    const map: Record<string, string> =
      type === "payment"
        ? {
            completed: "bg-emerald-100 text-emerald-600",
            pending: "bg-amber-100 text-amber-600",
            cancelled: "bg-slate-200 text-slate-600",
            failed: "bg-red-100 text-red-600",
          }
        : {
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

  if (loading) {
    return (
      <div className="p-10 min-h-[60vh] flex items-center justify-center">
        <p className="text-slate-400 font-bold">로딩 중...</p>
      </div>
    );
  }

  if (!id || loadError || !member) {
    return (
      <div className="p-10 min-h-[60vh] flex flex-col items-center justify-center gap-4">
        <p className="text-red-600 font-bold">{loadError || "잘못된 요청"}</p>
        <Link
          href="/members"
          className="text-indigo-600 font-bold hover:underline"
        >
          ← 목록으로
        </Link>
      </div>
    );
  }

  return (
    <div className="w-full max-w-[1200px] px-8 py-10 space-y-8">
      <div className="flex items-center gap-4">
        <Link
          href="/members"
          className="text-slate-500 hover:text-slate-900 font-bold text-sm uppercase tracking-widest"
        >
          ← 목록
        </Link>
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-6">
        <h1 className="text-2xl font-black italic">회원 상세</h1>
        <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div>
            <p className="text-xs font-black text-slate-500 uppercase">이메일</p>
            <p className="mt-1 font-bold">{member.email}</p>
          </div>
          <div>
            <p className="text-xs font-black text-slate-500 uppercase">이름</p>
            <p className="mt-1 font-bold">{member.name || "—"}</p>
          </div>
          <div>
            <p className="text-xs font-black text-slate-500 uppercase">가입일</p>
            <p className="mt-1 font-bold">
              {member.createdAt
                ? new Date(member.createdAt).toLocaleString("ko-KR")
                : "—"}
            </p>
          </div>
          <div>
            <p className="text-xs font-black text-slate-500 uppercase">구독</p>
            <p className="mt-1">
              <span className="text-[10px] font-black bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full uppercase">
                {member.subscription || "free"}
              </span>
            </p>
          </div>
          <div>
            <p className="text-xs font-black text-slate-500 uppercase">상태</p>
            <p className="mt-1">
              {member.deletedAt ? (
                <span className="text-red-600 font-bold text-xs">탈퇴</span>
              ) : member.suspendedAt ? (
                <span className="text-amber-600 font-bold text-xs">정지</span>
              ) : (
                <span className="text-emerald-600 font-bold text-xs">활성</span>
              )}
            </p>
          </div>
        </div>

        {!member.deletedAt && (
          <div className="mt-6 flex items-center gap-2">
            {member.suspendedAt ? (
              <Button
                variant="outline"
                size="sm"
                onClick={handleUnsuspend}
                disabled={!!actionId}
              >
                정지 해제
              </Button>
            ) : (
              <Button
                variant="outline"
                size="sm"
                onClick={handleSuspend}
                disabled={!!actionId}
              >
                정지
              </Button>
            )}
            <Button
              variant="outline"
              size="sm"
              onClick={handleDelete}
              disabled={!!actionId}
              className="text-red-600 border-red-200 hover:bg-red-50"
            >
              탈퇴 처리
            </Button>
          </div>
        )}
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white overflow-hidden">
        <h2 className="px-6 py-4 text-lg font-black border-b border-slate-100">
          결제 이력 ({payments.length}건)
        </h2>
        {payments.length === 0 ? (
          <div className="p-8 text-center text-slate-400 text-sm">
            결제 이력이 없습니다.
          </div>
        ) : (
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                  ID
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
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr
                  key={p.id}
                  className="border-b border-slate-50 hover:bg-slate-50/50"
                >
                  <td className="px-4 py-3 font-mono text-xs text-slate-500">
                    {p.id.slice(0, 8)}...
                  </td>
                  <td className="px-4 py-3">
                    <span className="text-[10px] font-black bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full uppercase">
                      {p.planId || "—"}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-bold">
                    ₩{(p.amount ?? 0).toLocaleString()}
                  </td>
                  <td className="px-4 py-3">{statusBadge(p.status, "payment")}</td>
                  <td className="px-4 py-3 text-slate-600">
                    {p.completedAt
                      ? new Date(p.completedAt).toLocaleString("ko-KR")
                      : p.createdAt
                        ? new Date(p.createdAt).toLocaleString("ko-KR")
                        : "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white overflow-hidden">
        <h2 className="px-6 py-4 text-lg font-black border-b border-slate-100">
          구독 이력 ({subscriptions.length}건)
        </h2>
        {subscriptions.length === 0 ? (
          <div className="p-8 text-center text-slate-400 text-sm">
            구독 이력이 없습니다.
          </div>
        ) : (
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">
                  ID
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
              </tr>
            </thead>
            <tbody>
              {subscriptions.map((s) => (
                <tr
                  key={s.id}
                  className="border-b border-slate-50 hover:bg-slate-50/50"
                >
                  <td className="px-4 py-3 font-mono text-xs text-slate-500">
                    {s.id.slice(0, 8)}...
                  </td>
                  <td className="px-4 py-3">
                    <span className="text-[10px] font-black bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full uppercase">
                      {s.planType || "—"}
                    </span>
                  </td>
                  <td className="px-4 py-3">{statusBadge(s.status, "subscription")}</td>
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
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
