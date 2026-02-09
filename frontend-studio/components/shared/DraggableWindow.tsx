"use client";

import React, { useRef, useState, useEffect } from 'react';

interface DraggableWindowProps {
  node: any;
  onClose: (id: string) => void;
  onFocus: (id: string) => void;
  zIndex: number;
  isTop: boolean;
}

export function DraggableWindow({ node, onClose, zIndex, onFocus, isTop }: DraggableWindowProps) {
  const windowRef = useRef<HTMLDivElement>(null);
  
  // 전달받은 초기 위치가 있으면 사용 (중앙 보정), 없으면 랜덤 배치
  const initialX = node.initialX ? node.initialX - 160 : 100 + Math.random() * 100;
  const initialY = node.initialY ? node.initialY - 100 : 150 + Math.random() * 100;
  
  const [pos, setPos] = useState({ x: initialX, y: initialY });
  const isDragging = useRef(false);
  const dragOffset = useRef({ x: 0, y: 0 });

  const handleMouseDown = (e: React.MouseEvent) => {
    isDragging.current = true;
    onFocus(node.id);
    dragOffset.current = { x: e.clientX - pos.x, y: e.clientY - pos.y };
    if (windowRef.current) windowRef.current.style.transition = 'none';
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isDragging.current || !windowRef.current) return;
      const newX = e.clientX - dragOffset.current.x;
      const newY = e.clientY - dragOffset.current.y;
      windowRef.current.style.transform = `translate3d(${newX}px, ${newY}px, 0)`;
      pos.x = newX;
      pos.y = newY;
    };

    const handleMouseUp = () => {
      if (isDragging.current) {
        isDragging.current = false;
        setPos({ ...pos });
        if (windowRef.current) windowRef.current.style.transition = 'all 0.4s cubic-bezier(0.16, 1, 0.3, 1)';
      }
    };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);
    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };
  }, [pos]);

  return (
    <div 
      ref={windowRef}
      onMouseDown={() => onFocus(node.id)}
      style={{ 
        transform: `translate3d(${pos.x}px, ${pos.y}px, 0)`,
        zIndex 
      }}
      className={`absolute top-0 left-0 w-[400px] bg-white/90 backdrop-blur-3xl border-2 ${isTop ? 'border-blue-500 shadow-[0_30px_80px_rgba(37,99,235,0.2)]' : 'border-slate-200 shadow-2xl'} rounded-[2.5rem] select-none overflow-hidden pointer-events-auto transition-all duration-400 transform-gpu`}
    >
      <div onMouseDown={handleMouseDown} className={`p-6 flex justify-between items-center cursor-move border-b ${isTop ? 'bg-blue-50/50 border-blue-100' : 'bg-slate-50/50 border-slate-100'}`}>
        <div className="flex items-center gap-3">
          <div className={`w-2.5 h-2.5 rounded-full ${isTop ? 'bg-blue-600' : 'bg-slate-400'} animate-pulse`} />
          <span className="text-[11px] font-black tracking-[0.15em] text-slate-800 uppercase truncate w-64">{node.label}</span>
        </div>
        <button onClick={(e) => { e.stopPropagation(); onClose(node.id); }} className="text-slate-400 hover:text-slate-900 transition-colors p-1">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
        </button>
      </div>
      
      <div className="p-8 space-y-6 select-text overflow-y-auto max-h-[600px] custom-scrollbar">
        {/* 1. Raw Data 섹션 */}
        <div className="space-y-2">
          <div className="flex items-center gap-2 px-1">
            <div className="w-1 h-3 bg-blue-500 rounded-full" />
            <span className="text-[10px] font-black text-blue-600 uppercase tracking-widest">Raw_Data</span>
          </div>
          <div className="bg-slate-50/80 p-5 rounded-2xl border border-slate-100 shadow-inner max-h-40 overflow-y-auto custom-scrollbar">
            <p className="text-[12px] font-medium text-slate-700 leading-relaxed break-words">
              {typeof node.value === 'string' && node.value.includes('● 데이터:') 
                ? node.value.split('● 데이터:')[1].split('● 해석:')[0].trim()
                : (typeof node.value === 'string' ? node.value : JSON.stringify(node.value, null, 2))}
            </p>
          </div>
        </div>

        {/* 2. AI 해석/요약 섹션 */}
        {(typeof node.value === 'string' && node.value.includes('● 해석:')) && (
          <div className="space-y-2">
            <div className="flex items-center gap-2 px-1">
              <div className="w-1 h-3 bg-emerald-500 rounded-full" />
              <span className="text-[10px] font-black text-emerald-600 uppercase tracking-widest">AI_Interpretation</span>
            </div>
            <div className="bg-emerald-50/30 p-5 rounded-2xl border border-emerald-100 shadow-inner">
              <p className="text-[12px] font-bold text-slate-800 leading-relaxed break-words">
                {node.value.split('● 해석:')[1].trim()}
              </p>
            </div>
          </div>
        )}

        {/* 3. 참고 자료 및 근거 섹션 */}
        {node.references && node.references.length > 0 && (
          <div className="space-y-3">
            <div className="flex items-center gap-2 px-1">
              <div className="w-1 h-3 bg-slate-400 rounded-full" />
              <span className="text-[10px] font-black text-slate-500 uppercase tracking-widest">References & Evidence</span>
            </div>
            <div className="space-y-2">
              {node.references.map((ref: any, idx: number) => (
                <div key={idx} className="bg-slate-100/50 p-4 rounded-2xl border border-slate-200 group/ref transition-all hover:bg-white hover:shadow-md">
                  <div className="flex justify-between items-start mb-1">
                    <span className="text-[10px] font-black text-slate-700 truncate w-48">{ref.title}</span>
                    <a href={ref.url} target="_blank" className="text-blue-600 hover:text-blue-800 transition-colors">
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" /></svg>
                    </a>
                  </div>
                  {ref.snippet && <p className="text-[9px] text-slate-500 leading-normal italic line-clamp-2">"{ref.snippet}"</p>}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
