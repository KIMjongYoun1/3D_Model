"use client";

import { useState, useMemo, useEffect, useRef } from "react";

interface ERDDiagramProps {
  data: any;
  openNodes: any[];
  topNodeId: string | null;
  onNodeClick: (node: any) => void;
  showDiagram?: boolean; // 다이어그램 표시 상태 추가
}

export default function ERDDiagram({ data, openNodes, topNodeId, onNodeClick, showDiagram }: ERDDiagramProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  
  // 자동 스크롤: 다이어그램이 활성화된 상태일 때만 수행
  useEffect(() => {
    if (showDiagram && topNodeId && containerRef.current) {
      const selectedElement = document.getElementById(`erd-node-${topNodeId}`);
      if (selectedElement) {
        selectedElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }
  }, [topNodeId, showDiagram]);

  if (data.render_type === "monolith") {
    return (
      <div className="w-full h-full bg-white/20 p-10 flex flex-col items-center justify-center">
        <div className="w-full max-w-md bg-white border border-slate-200 rounded-3xl p-8 shadow-xl">
          <h2 className="text-xl font-black mb-6 italic uppercase text-slate-900 tracking-tighter">Raw Data Stream</h2>
          <div className="bg-slate-50 p-6 rounded-2xl border border-slate-100 font-mono text-xs text-slate-600 max-h-[400px] overflow-y-auto custom-scrollbar">
            {data.content}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div ref={containerRef} className="w-full h-full bg-transparent px-10 relative">
      {/* 4열 그리드 시스템: 한 행에 4개씩 배치 */}
      <div className="grid grid-cols-4 gap-x-10 gap-y-20 w-full items-start pt-20 pb-40">
        {data.nodes?.map((node: any) => {
          const isSelected = openNodes.some(n => n.id === node.id);
          const isTop = topNodeId === node.id;
          
          return (
            <div
              id={`erd-node-${node.id}`}
              key={node.id}
              onClick={() => onNodeClick(node)}
              className={`w-full h-[260px] bg-white border-2 rounded-[2.5rem] overflow-hidden cursor-pointer transition-all duration-500 flex flex-col
                ${isTop ? 'border-blue-600 shadow-[0_40px_80px_rgba(37,99,235,0.2)] scale-[1.02] -translate-y-8 z-50' : isSelected ? 'border-blue-400 opacity-100 z-40' : 'border-slate-100 opacity-80 hover:opacity-100 z-10 hover:border-slate-300'}`}
            >
              <div className={`px-6 py-5 text-[10px] font-black uppercase tracking-[0.2em] border-b flex items-center justify-between flex-shrink-0
                ${isTop ? 'bg-blue-50 text-blue-600 border-blue-100' : isSelected ? 'bg-blue-50/50 text-blue-500 border-blue-50' : 'bg-slate-50 text-slate-500 border-slate-100'}`}>
                <div className="flex items-center gap-2">
                  <div className={`w-2 h-2 rounded-full ${isTop ? 'bg-blue-600 animate-pulse' : isSelected ? 'bg-blue-400' : 'bg-slate-300'}`} />
                  <span className="truncate w-32">{node.label}</span>
                </div>
                {isSelected && (
                  <div className={`w-6 h-6 ${isTop ? 'bg-blue-600' : 'bg-blue-400'} rounded-full flex items-center justify-center shadow-lg`}>
                    <svg className="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={4} d="M5 13l4 4L19 7" /></svg>
                  </div>
                )}
              </div>
              
              <div className="p-6 flex-1 overflow-hidden flex flex-col gap-4">
                {/* 1. Raw Data 섹션 */}
                <div className="flex-1 overflow-hidden flex flex-col gap-1.5">
                  <div className="flex items-center gap-1.5 px-1">
                    <div className="w-0.5 h-2.5 bg-blue-500 rounded-full" />
                    <span className="text-[8px] font-black text-blue-600 uppercase tracking-widest">Raw_Data</span>
                  </div>
                  <div className="bg-slate-50/50 px-4 py-3 rounded-2xl border border-slate-100 shadow-inner overflow-y-auto custom-scrollbar">
                    <p className="text-[10px] font-medium text-slate-600 leading-relaxed break-words">
                      {typeof node.value === 'string' && node.value.includes('● 데이터:') 
                        ? node.value.split('● 데이터:')[1].split('● 해석:')[0].trim()
                        : (typeof node.value === 'string' ? node.value : JSON.stringify(node.value, null, 1))}
                    </p>
                  </div>
                </div>

                {/* 2. AI Interpretation 섹션 (데이터가 있을 때만 표시) */}
                {(typeof node.value === 'string' && node.value.includes('● 해석:')) && (
                  <div className="flex-shrink-0 flex flex-col gap-1.5">
                    <div className="flex items-center gap-1.5 px-1">
                      <div className="w-0.5 h-2.5 bg-emerald-500 rounded-full" />
                      <span className="text-[8px] font-black text-emerald-600 uppercase tracking-widest">AI_Interpretation</span>
                    </div>
                    <div className="bg-emerald-50/30 px-4 py-3 rounded-2xl border border-emerald-100 shadow-inner">
                      <p className="text-[10px] font-bold text-slate-800 leading-tight break-words line-clamp-3">
                        {node.value.split('● 해석:')[1].trim()}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
