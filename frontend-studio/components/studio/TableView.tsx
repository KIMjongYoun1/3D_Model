"use client";

import React, { useMemo } from "react";

interface TableViewProps {
  data: { nodes: any[]; links?: any[]; render_type?: string };
  openNodes: any[];
  topNodeId: string | null;
  onRowClick: (node: any) => void;
  onBackToMain: () => void;
}

function flattenNodeToRow(node: any): Record<string, string | number> {
  const row: Record<string, string | number> = { id: node.id, label: String(node.label || node.id || "") };
  const v = node.value;
  if (typeof v === "string" || typeof v === "number") {
    row.value = v;
  } else if (v && typeof v === "object" && !Array.isArray(v)) {
    for (const [k, val] of Object.entries(v)) {
      row[String(k)] = typeof val === "object" ? JSON.stringify(val) : String(val ?? "");
    }
  }
  return row;
}

export default function TableView({
  data,
  openNodes,
  topNodeId,
  onRowClick,
  onBackToMain,
}: TableViewProps) {
  const nodes = data?.nodes || [];
  const hasDetail = openNodes.length > 0;

  const { rows, columns } = useMemo(() => {
    const dataNodes = nodes.filter((n: any) => n.type !== "root");
    const rows = dataNodes.map((n: any) => ({ ...flattenNodeToRow(n), _node: n }));
    const allKeys = new Set<string>();
    rows.forEach((r: any) => Object.keys(r).filter((k) => k !== "_node").forEach((k) => allKeys.add(k)));
    const columns = Array.from(allKeys).filter((k) => k !== "id").slice(0, 8);
    return { rows, columns };
  }, [nodes]);

  return (
    <div className="w-full h-full flex flex-col bg-white">
      <div className="flex-shrink-0 flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/30">
        <button
          onClick={onBackToMain}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-bold transition-all ${
            hasDetail ? "bg-blue-600 text-white shadow-lg shadow-blue-600/20 hover:bg-blue-700" : "bg-slate-200/60 text-slate-500 hover:bg-slate-200"
          }`}
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 17l-5-5m0 0l5-5m-5 5h12" />
          </svg>
          {hasDetail ? "전체 보기" : "테이블"}
        </button>
        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">{rows.length}행</span>
      </div>

      <div className="flex-1 min-h-0 overflow-auto p-6">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="border-b border-slate-200">
              {columns.map((col) => (
                <th key={col} className="text-left py-3 px-4 font-bold text-slate-600 uppercase tracking-wider text-[10px]">
                  {col}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row: any) => {
              const isSelected = topNodeId === row._node?.id || openNodes.some((n) => n.id === row._node?.id);
              return (
                <tr
                  key={row.id}
                  onClick={() => onRowClick(row._node)}
                  className={`border-b border-slate-100 cursor-pointer transition-colors ${
                    isSelected ? "bg-blue-50" : "hover:bg-slate-50"
                  }`}
                >
                  {columns.map((col) => (
                    <td key={col} className="py-3 px-4 text-slate-700 max-w-[200px] truncate" title={String(row[col] ?? "")}>
                      {String(row[col] ?? "-")}
                    </td>
                  ))}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <div className="flex-shrink-0 px-6 pb-4">
        <p className="text-[10px] text-slate-400 font-medium">행을 클릭하면 상세 정보가 표시됩니다.</p>
      </div>
    </div>
  );
}
