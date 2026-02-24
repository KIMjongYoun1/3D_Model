"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import {
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ComposedChart,
  Line,
} from "recharts";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface RevenueByMonth {
  year: number;
  month: number;
  revenue: number;
}

interface PlanRevenueInPeriod {
  planId: string;
  revenue: number;
}

interface RevenueTimeSeriesItem {
  periodKey: string;
  label: string;
  revenue: number;
  previousRevenue: number;
  byPlan?: PlanRevenueInPeriod[];
}

interface RevenueByPlan {
  planId: string;
  revenue: number;
  count: number;
}

interface DashboardData {
  totalRevenue: number;
  previousTotalRevenue?: number;
  period?: string;
  revenueByMonth: RevenueByMonth[];
  revenueByPlan: RevenueByPlan[];
  timeSeries?: RevenueTimeSeriesItem[];
}

const PERIOD_OPTIONS = [
  { value: "week", label: "주별" },
  { value: "month", label: "월별" },
  { value: "quarter", label: "분기별" },
  { value: "half", label: "반기별" },
];

const PLAN_COLORS = ["#6366f1", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981"];

export default function AdminDashboardPage() {
  useRequireAdminAuth();
  const [period, setPeriod] = useState("month");
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const { data: res } = await adminApi.get<DashboardData>(
          `/api/admin/dashboard/revenue?period=${period}`
        );
        setData(res);
      } catch (e) {
        console.error("대시보드 로드 실패:", e);
        setData(null);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [period]);

  if (loading && !data) {
    return (
      <div className="w-full max-w-[1200px] px-8 py-10">
        <div className="py-20 text-center text-slate-400 font-bold">
          로딩 중...
        </div>
      </div>
    );
  }

  const total = data?.totalRevenue ?? 0;
  const prevTotal = data?.previousTotalRevenue ?? 0;
  const byMonth = data?.revenueByMonth ?? [];
  const byPlan = data?.revenueByPlan ?? [];
  const timeSeries = data?.timeSeries ?? [];
  const maxMonthRevenue = Math.max(...byMonth.map((m) => m.revenue), 1);

  const chartData = timeSeries.map((t) => ({
    name: t.label,
    당기: t.revenue,
    전기: t.previousRevenue,
    byPlan: t.byPlan,
  }));

  const CustomTooltip = ({ active, payload, label }: { active?: boolean; payload?: { name: string; value: number; color: string }[]; label?: string }) => {
    if (!active || !payload?.length || !label) return null;
    const item = chartData.find((d) => d.name === label);
    return (
      <div className="bg-white border border-slate-200 rounded-xl shadow-lg p-4 min-w-[180px]">
        <p className="font-black text-slate-800 mb-2">{label}</p>
        {payload.map((p) => (
          <p key={p.name} className="text-sm font-bold text-slate-600">
            {p.name}: ₩{Number(p.value || 0).toLocaleString()}
          </p>
        ))}
        {item?.byPlan && item.byPlan.length > 0 && (
          <div className="mt-2 pt-2 border-t border-slate-100">
            <p className="text-[10px] font-black text-slate-400 uppercase mb-1">구독별</p>
            {item.byPlan.map((bp) => (
              <p key={bp.planId} className="text-xs font-medium text-slate-600">
                {bp.planId}: ₩{bp.revenue.toLocaleString()}
              </p>
            ))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="w-full max-w-[1200px] px-8 py-10 space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black tracking-tighter italic">
            REVENUE DASHBOARD
          </h1>
          <p className="text-slate-500 text-sm mt-2 font-bold uppercase tracking-widest">
            매출 통계 · 전월/전기 대비 · 구독별
          </p>
        </div>
        <div className="flex gap-2">
          {PERIOD_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              onClick={() => setPeriod(opt.value)}
              className={`px-4 py-2 text-sm font-bold rounded-xl border transition-colors ${
                period === opt.value
                  ? "bg-indigo-600 text-white border-indigo-600"
                  : "bg-white text-slate-600 border-slate-200 hover:border-indigo-300"
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-slate-500 text-xs font-bold uppercase tracking-widest">
            총 매출
          </p>
          <p className="text-3xl font-black text-indigo-600 mt-2">
            ₩{total.toLocaleString()}
          </p>
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-slate-500 text-xs font-bold uppercase tracking-widest">
            전기 대비
          </p>
          <p className="text-3xl font-black text-slate-800 mt-2">
            ₩{prevTotal.toLocaleString()}
          </p>
          {prevTotal > 0 && (
            <p className="text-xs font-bold text-slate-500 mt-1">
              {total >= prevTotal ? "↑" : "↓"}{" "}
              {Math.abs(
                Math.round(((total - prevTotal) / prevTotal) * 100)
              )}
              %
            </p>
          )}
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-slate-500 text-xs font-bold uppercase tracking-widest">
            플랜 수
          </p>
          <p className="text-3xl font-black text-slate-800 mt-2">
            {byPlan.length}개
          </p>
        </div>
      </div>

      {/* 막대 + 선 차트 (당기 vs 전기) */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 overflow-hidden">
        <h2 className="text-lg font-black text-slate-800 mb-4">
          매출 추이 (막대: 당기 · 선: 전기)
        </h2>
        {chartData.length === 0 ? (
          <p className="text-slate-400 text-sm py-12 text-center">데이터 없음</p>
        ) : (
          <div className="h-[320px]">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} stroke="#64748b" />
                <YAxis tick={{ fontSize: 11 }} stroke="#64748b" tickFormatter={(v) => `₩${(v / 1000).toFixed(0)}k`} />
                <Tooltip content={<CustomTooltip />} />
                <Legend />
                <Bar dataKey="당기" fill="#6366f1" radius={[4, 4, 0, 0]} name="당기 매출" />
                <Line type="monotone" dataKey="전기" stroke="#94a3b8" strokeWidth={2} dot={{ r: 4 }} name="전기 매출" />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* 월별 매출 (기존) */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 overflow-hidden">
          <h2 className="text-lg font-black text-slate-800 mb-4">월별 매출</h2>
          {byMonth.length === 0 ? (
            <p className="text-slate-400 text-sm">데이터 없음</p>
          ) : (
            <div className="space-y-3">
              {byMonth.slice(0, 6).map((m) => (
                <div key={`${m.year}-${m.month}`} className="flex items-center gap-4">
                  <span className="text-sm font-bold text-slate-600 w-24">
                    {m.year}년 {m.month}월
                  </span>
                  <div className="flex-1 h-6 bg-slate-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-indigo-500 rounded-full transition-all"
                      style={{
                        width: `${(m.revenue / maxMonthRevenue) * 100}%`,
                      }}
                    />
                  </div>
                  <span className="text-sm font-black text-slate-800 w-24 text-right">
                    ₩{m.revenue.toLocaleString()}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 플랜별 매출 + 구독별 세부 */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 overflow-hidden">
          <h2 className="text-lg font-black text-slate-800 mb-4">구독별 매출</h2>
          {byPlan.length === 0 ? (
            <p className="text-slate-400 text-sm">데이터 없음</p>
          ) : (
            <div className="space-y-3">
              {byPlan.map((p, i) => (
                <div
                  key={p.planId}
                  className="flex items-center justify-between py-2 border-b border-slate-50 last:border-0"
                >
                  <span
                    className="text-[10px] font-black px-2 py-0.5 rounded-full uppercase"
                    style={{
                      backgroundColor: `${PLAN_COLORS[i % PLAN_COLORS.length]}20`,
                      color: PLAN_COLORS[i % PLAN_COLORS.length],
                    }}
                  >
                    {p.planId}
                  </span>
                  <span className="text-sm font-bold text-slate-600">{p.count}건</span>
                  <span className="text-sm font-black text-slate-800">
                    ₩{p.revenue.toLocaleString()}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-6">
        <h2 className="text-lg font-black text-slate-800 mb-4">상세 거래 내역</h2>
        <p className="text-slate-500 text-sm mb-4">
          날짜별로 필터링하여 거래 내역을 조회할 수 있습니다.
        </p>
        <Link
          href="/transactions"
          className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white text-sm font-bold rounded-xl hover:bg-indigo-700 transition-colors"
        >
          거래 관리 →
        </Link>
      </div>
    </div>
  );
}
