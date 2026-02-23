"use client";

import React from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from "recharts";

interface ChartBarPanelProps {
  data: { nodes: any[]; links?: any[]; render_type?: string };
  openNodes: any[];
  topNodeId: string | null;
  onBarClick: (node: any) => void;
  onBackToMain: () => void;
}

function extractChartData(nodes: any[]): { label: string; value: number; node: any }[] {
  const result: { label: string; value: number; node: any }[] = [];
  const amountKeys = ["금액", "amount", "price", "value", "매출", "매입", "합계"];

  for (const node of nodes) {
    if (node.id === "total" || node.type === "root") continue;

    let value = 0;

    if (typeof node.value === "number") {
      value = node.value;
    } else if (typeof node.value === "string") {
      const parsed = parseFloat(node.value.replace(/[^0-9.-]/g, ""));
      if (!isNaN(parsed)) value = parsed;
    } else if (typeof node.value === "object" && node.value !== null) {
      for (const [k, v] of Object.entries(node.value)) {
        if (amountKeys.some((key) => String(k).toLowerCase().includes(key))) {
          const n = Number(v);
          if (!isNaN(n)) {
            value = n;
            break;
          }
        }
      }
    }

    if (Array.isArray(node.pos) && node.pos[1] !== undefined && value === 0) {
      const h = Number(node.pos[1]);
      if (!isNaN(h) && h > 0) value = h;
    }

    result.push({
      label: String(node.label || node.id || "항목").slice(0, 20),
      value: value > 0 ? value : 1,
      node: node,
    });
  }

  return result.length > 0 ? result : [{ label: "데이터 없음", value: 0, node: {} }];
}

export default function ChartBarPanel({
  data,
  openNodes,
  topNodeId,
  onBarClick,
  onBackToMain,
}: ChartBarPanelProps) {
  const chartData = extractChartData(data?.nodes || []);
  const hasDetail = openNodes.length > 0;

  return (
    <div className="w-full h-full flex flex-col bg-white">
      {/* 헤더: 메인 복귀 + 단계 표시 (최소화) */}
      <div className="flex-shrink-0 flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/30">
        <button
          onClick={onBackToMain}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-bold transition-all ${
            hasDetail
              ? "bg-blue-600 text-white shadow-lg shadow-blue-600/20 hover:bg-blue-700"
              : "bg-slate-200/60 text-slate-500 hover:bg-slate-200"
          }`}
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 17l-5-5m0 0l5-5m-5 5h12" />
          </svg>
          {hasDetail ? "전체 보기" : "차트"}
        </button>
        {hasDetail && (
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
            {openNodes.length}개 상세
          </span>
        )}
      </div>

      {/* 차트 영역 */}
      <div className="flex-1 min-h-0 p-6">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart
            data={chartData}
            margin={{ top: 20, right: 20, left: 10, bottom: 60 }}
            layout="vertical"
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" horizontal={false} />
            <XAxis type="number" tick={{ fontSize: 11, fill: "#64748b" }} stroke="#94a3b8" />
            <YAxis
              type="category"
              dataKey="label"
              width={100}
              tick={{ fontSize: 11, fill: "#475569" }}
              stroke="#94a3b8"
            />
            <Tooltip
              cursor={{ fill: "#f1f5f9" }}
              contentStyle={{
                borderRadius: 12,
                border: "1px solid #e2e8f0",
                boxShadow: "0 4px 20px rgba(0,0,0,0.08)",
              }}
              formatter={(value: number, name: string, props: any) => [
                `${Number(value).toLocaleString()}`,
                props.payload.label,
              ]}
            />
            <Bar
              dataKey="value"
              radius={[0, 6, 6, 0]}
              maxBarSize={36}
              onClick={(payload: any) => payload?.node?.id && onBarClick(payload.node)}
              style={{ cursor: "pointer" }}
            >
              {chartData.map((entry, index) => {
                const isSelected = topNodeId === entry.node?.id || openNodes.some((n) => n.id === entry.node?.id);
                return (
                  <Cell
                    key={`cell-${index}`}
                    fill={isSelected ? "#2563eb" : "#38bdf8"}
                    opacity={isSelected ? 1 : 0.85}
                    stroke={isSelected ? "#1d4ed8" : "transparent"}
                    strokeWidth={2}
                  />
                );
              })}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* 하단 힌트 */}
      <div className="flex-shrink-0 px-6 pb-4">
        <p className="text-[10px] text-slate-400 font-medium">
          막대를 클릭하면 상세 정보가 표시됩니다.
        </p>
      </div>
    </div>
  );
}
