"use client";

import { useState, useMemo, useEffect, useRef } from "react";

interface ERDDiagramProps {
  data: any;
  openNodes: any[];
  topNodeId: string | null;
  onNodeClick: (node: any) => void;
}

export default function ERDDiagram({ data, openNodes, topNodeId, onNodeClick }: ERDDiagramProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  
  // [드래그 상태] 각 노드의 현재 위치 관리
  const [positions, setPositions] = useState<Record<string, { x: number, y: number }>>({});
  const [draggingNodeId, setDraggingNodeId] = useState<string | null>(null);
  const dragOffset = useRef({ x: 0, y: 0 });

  // 초기 위치 계산 (그리드 형태)
  useEffect(() => {
    if (!data.nodes) return;
    const initialPos: Record<string, { x: number, y: number }> = {};
    const cols = 2;
    data.nodes.forEach((node: any, i: number) => {
      initialPos[node.id] = {
        x: (i % cols) * 240 + 60,
        y: Math.floor(i / cols) * 220 + 80
      };
    });
    setPositions(initialPos);
  }, [data.nodes]);

  // 드래그 핸들러
  const handleMouseDown = (e: React.MouseEvent, id: string) => {
    const pos = positions[id];
    if (!pos) return;
    setDraggingNodeId(id);
    dragOffset.current = { x: e.clientX - pos.x, y: e.clientY - pos.y };
    onNodeClick(data.nodes.find((n: any) => n.id === id));
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!draggingNodeId) return;
      setPositions(prev => ({
        ...prev,
        [draggingNodeId]: {
          x: e.clientX - dragOffset.current.x,
          y: e.clientY - dragOffset.current.y
        }
      }));
    };
    const handleMouseUp = () => setDraggingNodeId(null);

    if (draggingNodeId) {
      window.addEventListener("mousemove", handleMouseMove);
      window.addEventListener("mouseup", handleMouseUp);
    }
    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };
  }, [draggingNodeId]);

  // 자동 스크롤
  useEffect(() => {
    if (topNodeId && containerRef.current) {
      const selectedElement = document.getElementById(`erd-node-${topNodeId}`);
      if (selectedElement) {
        selectedElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }
  }, [topNodeId]);

  if (data.render_type === "monolith") {
    return (
      <div className="w-full h-full bg-[#010409]/50 p-10 flex flex-col items-center justify-center text-white">
        <div className="w-full max-w-md bg-[#0d1117] border-2 border-indigo-500/50 rounded-3xl p-8 shadow-2xl">
          <h2 className="text-xl font-black mb-6 italic uppercase">Raw Data Stream</h2>
          <div className="bg-black/60 p-6 rounded-2xl border border-white/5 font-mono text-xs text-indigo-300 max-h-[400px] overflow-y-auto custom-scrollbar">
            {data.content}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div ref={containerRef} className="w-full h-full bg-[#010409]/50 overflow-auto p-8 custom-scrollbar relative">
      <div className="relative min-w-[600px] min-h-[2000px]">
        {/* 연결선 (SVG) - 실시간 좌표 반영 */}
        <svg className="absolute inset-0 w-full h-full pointer-events-none">
          {data.links?.map((link: any, i: number) => {
            const sourcePos = positions[link.source];
            const targetPos = positions[link.target];
            if (!sourcePos || !targetPos) return null;

            const isHighlighted = topNodeId === link.source || topNodeId === link.target;

            return (
              <path
                key={i}
                d={`M ${sourcePos.x + 100} ${sourcePos.y + 40} L ${targetPos.x + 100} ${targetPos.y + 40}`}
                stroke={isHighlighted ? "#fbbf24" : "#1e293b"}
                strokeWidth={isHighlighted ? 3 : 1}
                fill="none"
                strokeDasharray={isHighlighted ? "none" : "5 5"}
                className="transition-all duration-300"
              />
            );
          })}
        </svg>

        {/* 드래그 가능한 노드들 */}
        {data.nodes?.map((node: any) => {
          const isSelected = openNodes.some(n => n.id === node.id);
          const isTop = topNodeId === node.id;
          const pos = positions[node.id] || { x: 0, y: 0 };
          
          return (
            <div
              id={`erd-node-${node.id}`}
              key={node.id}
              onMouseDown={(e) => handleMouseDown(e, node.id)}
              style={{ left: pos.x, top: pos.y }}
              className={`absolute min-w-[200px] max-w-[320px] bg-[#0d1117] border-2 rounded-xl overflow-hidden cursor-grab active:cursor-grabbing transition-shadow duration-300
                ${isTop ? 'border-yellow-400 shadow-[0_0_40px_rgba(234,179,8,0.3)] z-30 scale-105' : isSelected ? 'border-blue-500 z-20' : 'border-white/10 z-10'}`}
            >
              {isSelected && (
                <div className={`absolute top-2 right-2 w-5 h-5 ${isTop ? 'bg-yellow-400' : 'bg-blue-500'} rounded-full flex items-center justify-center shadow-lg z-40`}>
                  <svg className="w-3 h-3 text-black" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={4} d="M5 13l4 4L19 7" /></svg>
                </div>
              )}

              <div className={`px-4 py-3 text-[11px] font-black uppercase tracking-widest border-b border-white/5 flex items-center gap-2
                ${isTop ? 'bg-yellow-500/20 text-yellow-400' : isSelected ? 'bg-blue-500/20 text-blue-400' : 'bg-blue-500/5 text-blue-400'}`}>
                <div className={`w-2 h-2 rounded-full ${isTop ? 'bg-yellow-400 animate-ping' : isSelected ? 'bg-blue-400' : 'bg-blue-500/40'}`} />
                <span className="truncate">{node.label}</span>
              </div>
              
              <div className="p-4 space-y-3">
                <div className="bg-black/40 px-3 py-2.5 rounded-lg border border-white/5 shadow-inner">
                  <p className="text-[10px] font-mono text-emerald-400 leading-normal break-words">
                    {typeof node.value === 'string' ? node.value : JSON.stringify(node.value, null, 1)}
                  </p>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
