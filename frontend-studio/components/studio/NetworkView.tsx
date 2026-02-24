"use client";

import React, { useMemo } from "react";

interface NetworkViewProps {
  data: { nodes: any[]; links?: any[] };
  openNodes: any[];
  topNodeId: string | null;
  onNodeClick: (node: any) => void;
  onBackToMain: () => void;
}

export default function NetworkView({
  data,
  openNodes,
  topNodeId,
  onNodeClick,
  onBackToMain,
}: NetworkViewProps) {
  const nodes = data?.nodes || [];
  const links = data?.links || [];
  const hasDetail = openNodes.length > 0;

  const { nodePositions, width, height } = useMemo(() => {
    const w = 800;
    const h = 500;
    const dataNodes = nodes.filter((n: any) => n.type !== "root");
    const rootNodes = nodes.filter((n: any) => n.type === "root");
    const positions: Record<string, { x: number; y: number }> = {};
    const centerX = w / 2;
    const centerY = h / 2;

    rootNodes.forEach((n: any, i: number) => {
      positions[n.id] = { x: centerX, y: 60 + i * 50 };
    });

    const others = dataNodes.filter((n: any) => !rootNodes.some((r) => r.id === n.id));
    const n = others.length;
    const radius = Math.min(180, Math.max(80, n * 8));
    others.forEach((node: any, i: number) => {
      const angle = (i / Math.max(1, n)) * Math.PI * 2 - Math.PI / 2;
      positions[node.id] = {
        x: centerX + Math.cos(angle) * radius,
        y: centerY + Math.sin(angle) * radius * 0.6,
      };
    });

    return { nodePositions: positions, width: w, height: h };
  }, [nodes]);

  const linkElements = useMemo(() => {
    const idToPos = nodePositions;
    return links
      .filter((l: any) => idToPos[l.source] && idToPos[l.target])
      .map((l: any, i: number) => {
        const from = idToPos[l.source];
        const to = idToPos[l.target];
        return (
          <line
            key={`link-${i}`}
            x1={from.x}
            y1={from.y}
            x2={to.x}
            y2={to.y}
            stroke="#94a3b8"
            strokeWidth={1}
            strokeOpacity={0.5}
          />
        );
      });
  }, [links, nodePositions]);

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
          {hasDetail ? "전체 보기" : "네트워크"}
        </button>
        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
          {nodes.length}노드 · {links.length}링크
        </span>
      </div>

      <div className="flex-1 min-h-0 overflow-auto flex items-center justify-center p-6">
        <svg width={width} height={height} className="border border-slate-100 rounded-xl bg-slate-50/30">
          {linkElements}
          {Object.entries(nodePositions).map(([id, pos]) => {
            const node = nodes.find((n: any) => n.id === id);
            if (!node) return null;
            const isSelected = topNodeId === id || openNodes.some((n) => n.id === id);
            return (
              <g key={id} onClick={() => onNodeClick(node)} style={{ cursor: "pointer" }}>
                <circle
                  cx={pos.x}
                  cy={pos.y}
                  r={isSelected ? 14 : 10}
                  fill={isSelected ? "#2563eb" : "#38bdf8"}
                  stroke={isSelected ? "#1d4ed8" : "#0ea5e9"}
                  strokeWidth={isSelected ? 2 : 1}
                />
                <text
                  x={pos.x}
                  y={pos.y + 24}
                  textAnchor="middle"
                  fontSize={10}
                  fill="#475569"
                  fontWeight={isSelected ? 700 : 600}
                >
                  {String(node.label || node.id || "").slice(0, 12)}
                </text>
              </g>
            );
          })}
        </svg>
      </div>

      <div className="flex-shrink-0 px-6 pb-4">
        <p className="text-[10px] text-slate-400 font-medium">노드를 클릭하면 상세 정보가 표시됩니다.</p>
      </div>
    </div>
  );
}
