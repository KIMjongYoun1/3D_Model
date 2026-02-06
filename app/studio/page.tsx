"use client";

import React, { useEffect, useState, useCallback, useRef, useMemo } from "react";
import axios from "axios";
import QuantumCanvas from "@/components/QuantumCanvas";
import { DraggableWindow } from "@/components/shared/DraggableWindow";
import ERDDiagram from "@/components/ERDDiagram";
import Onboarding from "@/components/studio/Onboarding";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Modal } from "@/components/ui/Modal";

export default function QuantumStudioPage() {
  const [vizData, setVizData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditorOpen, setIsEditorOpen] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  const [openNodes, setOpenNodes] = useState<any[]>([]);
  const [topNodeId, setTopNodeId] = useState<string | null>(null);
  const [showDiagram, setShowDiagram] = useState(true);
  const [autoFocus, setAutoFocus] = useState(true);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [renderType, setRenderType] = useState<string>("auto");
  const [searchTerm, setSearchTerm] = useState("");
  
  // 온보딩 상태 관리 (비회원일 때 항상 표시)
  const [showOnboarding, setShowOnboarding] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const checkLoginStatus = useCallback(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      setIsLoggedIn(true);
      setShowOnboarding(false);
    } else {
      setIsLoggedIn(true); // 임시로 true로 설정되어 있던 것을 false로 수정
      setIsLoggedIn(false);
      setShowOnboarding(true);
    }
  }, []);

  useEffect(() => {
    checkLoginStatus();
    
    // 다른 탭에서의 로그인/로그아웃 동기화
    window.addEventListener('storage', checkLoginStatus);
    return () => window.removeEventListener('storage', checkLoginStatus);
  }, [checkLoginStatus]);
  
  const [jsonInput, setJsonInput] = useState<string>(JSON.stringify({
    "project_name": "Quantum System",
    "nodes": [
      {"id": "user_db", "name": "User DB", "type": "database", "status": "active"},
      {"id": "auth_svc", "name": "Auth Service", "type": "service", "status": "online"},
      {"id": "api_gw", "name": "API Gateway", "type": "network", "status": "active"},
      {"id": "cache_redis", "name": "Redis Cache", "type": "storage", "status": "active"}
    ],
    "links": [
      {"source": "api_gw", "target": "auth_svc"},
      {"source": "auth_svc", "target": "user_db"},
      {"source": "api_gw", "target": "cache_redis"}
    ]
  }, null, 2));

  const filteredNodes = useMemo(() => {
    if (!vizData?.mapping_data?.nodes) return [];
    if (!searchTerm) return vizData.mapping_data.nodes;
    const term = searchTerm.toLowerCase();
    return vizData.mapping_data.nodes.filter((node: any) => {
      const labelMatch = String(node.label || "").toLowerCase().includes(term);
      const nodeValue = typeof node.value === 'object' ? JSON.stringify(node.value) : String(node.value || "");
      const valueMatch = nodeValue.toLowerCase().includes(term);
      return labelMatch || valueMatch;
    });
  }, [vizData, searchTerm]);

  const fetchLatestVisualization = async () => {
    // 비회원이면 데이터를 불러오지 않고 상태 초기화
    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
    if (!token) {
      setVizData(null);
      setOpenNodes([]);
      setTopNodeId(null);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      const response = await axios.get("http://localhost:8000/api/v1/mapping");
      if (response.data && response.data.length > 0) {
        setVizData(response.data[response.data.length - 1]);
      }
    } catch (err) {
      console.error("Loading failed:", err);
      setError("서버 연결에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      setLoading(true);
      let payloadData;
      try { payloadData = JSON.parse(jsonInput); } catch (e) { payloadData = jsonInput; }
      const response = await axios.post("http://localhost:8000/api/v1/mapping", {
        data_type: typeof payloadData === 'object' ? "structured_json" : "raw_text",
        raw_data: payloadData,
        options: { render_type: renderType }
      });
      setVizData(response.data);
      setIsEditorOpen(false);
      setOpenNodes([]);
      setTopNodeId(null);
    } catch (err: any) {
      setError("전송 에러가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) setSelectedFile(file);
  };

  const processFileUpload = async () => {
    if (!selectedFile) return;
    try {
      setLoading(true);
      const formData = new FormData();
      formData.append("file", selectedFile);
      const response = await axios.post("http://localhost:8000/api/v1/mapping/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
        params: { render_type: renderType }
      });
      setVizData(response.data);
      setIsEditorOpen(false);
      setOpenNodes([]);
      setTopNodeId(null);
      setSelectedFile(null);
      alert("문서 분석 및 시각화가 완료되었습니다!");
    } catch (err: any) {
      console.error("File upload failed:", err);
      setError(err.response?.data?.detail || "파일 업로드 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleNodeSelect = useCallback((node: any, screenPos?: { x: number, y: number }) => {
    if (!node) return;
    setOpenNodes(prev => {
      if (prev.find(n => n.id === node.id)) return prev;
      return [...prev, { ...node, initialX: screenPos?.x, initialY: screenPos?.y }];
    });
    setTopNodeId(node.id);
  }, []);

  const handleDiagramNodeSelect = useCallback((node: any) => {
    if (!node) return;
    setOpenNodes(prev => {
      if (prev.find(n => n.id === node.id)) return prev;
      return [...prev, { ...node }];
    });
    setTopNodeId(node.id);
  }, []);

  const handleNodeClose = useCallback((id: string) => {
    setOpenNodes(prev => {
      const filtered = prev.filter(n => n.id !== id);
      if (topNodeId === id) {
        setTopNodeId(filtered.length > 0 ? filtered[filtered.length - 1].id : null);
      }
      return filtered;
    });
  }, [topNodeId]);

  useEffect(() => {
    fetchLatestVisualization();
  }, [isLoggedIn]); // 로그인 상태가 변경될 때마다 다시 확인

  return (
    <div className="flex-1 flex flex-col min-h-0 relative overflow-hidden">
      {/* Studio 전용 컨트롤 바 (서브 헤더) */}
      <div className="h-12 bg-white/50 backdrop-blur-md border-b border-slate-200 px-8 flex items-center justify-end gap-4 z-40">
        <div className="flex items-center gap-3 bg-slate-100/50 px-3 py-1 rounded-full border border-slate-200">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-widest">Auto Focus</span>
          <button 
            onClick={() => setAutoFocus(!autoFocus)} 
            className={`relative w-9 h-5 rounded-full transition-colors duration-300 focus:outline-none ${autoFocus ? 'bg-blue-600' : 'bg-slate-300'}`}
          >
            <div className={`absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full transition-transform duration-300 transform ${autoFocus ? 'translate-x-4' : 'translate-x-0'}`} />
          </button>
        </div>
        <div className="flex items-center gap-3 bg-slate-100/50 px-3 py-1 rounded-full border border-slate-200">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-widest">Diagram</span>
          <button 
            onClick={() => setShowDiagram(!showDiagram)} 
            className={`relative w-9 h-5 rounded-full transition-colors duration-300 focus:outline-none ${showDiagram ? 'bg-blue-600' : 'bg-slate-300'}`}
          >
            <div className={`absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full transition-transform duration-300 transform ${showDiagram ? 'translate-x-4' : 'translate-x-0'}`} />
          </button>
        </div>
        <Button 
          variant="primary"
          onClick={() => setIsEditorOpen(true)} 
          className="px-4 py-1.5 text-[10px]"
        >
          + NEW MAPPING
        </Button>
      </div>

      <div className="flex-1 flex w-full overflow-hidden relative bg-white">
        {/* [Layer 1] 3D 캔버스 - 전체 화면 고정 */}
        <div className="absolute inset-0 z-10">
          {vizData ? (
            <QuantumCanvas 
              data={vizData.mapping_data} 
              openNodes={openNodes} 
              topNodeId={topNodeId} 
              showPopups={false}
              autoFocus={autoFocus}
              onNodeSelect={handleNodeSelect} 
              onNodeClose={handleNodeClose} 
              onNodeFocus={setTopNodeId}
              centerOffset={[0, 0, 0]} 
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center font-mono text-slate-200 tracking-[1em] animate-pulse text-sm">INITIALIZING_CANVAS...</div>
          )}
        </div>

        {/* [Layer 2] 하단 다이어그램 바 - 높이 최적화 버전 (Bottom Sheet) */}
        <div 
          className={`absolute bottom-0 left-0 right-0 h-[380px] z-30 transition-all duration-1000 transform-gpu [transition-timing-function:cubic-bezier(0.16,1,0.3,1)]
            ${showDiagram && vizData ? 'translate-y-0' : 'translate-y-full'}`}
        >
          <div className="w-full h-full bg-white/40 backdrop-blur-3xl border-t border-slate-200/50 shadow-[0_-40px:100px_rgba(0,0,0,0.05)] relative flex flex-col rounded-t-[3.5rem]">
            <div className="h-8 flex-shrink-0 flex items-center justify-center cursor-pointer" onClick={() => setShowDiagram(false)}>
              <div className="w-16 h-1 bg-slate-300/60 rounded-full" />
            </div>
            <div className="h-12 border-b border-slate-100/50 flex items-center px-12 justify-between bg-white/10">
              <div className="flex items-center gap-6 flex-1">
                <div className="relative w-64">
                  <Input 
                    type="text" 
                    placeholder="Search nodes..." 
                    value={searchTerm} 
                    onChange={(e) => setSearchTerm(e.target.value)} 
                    className="!space-y-0"
                  />
                  <div className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 text-[10px]">🔍</div>
                </div>
                <div className="flex gap-2 overflow-x-auto max-w-[500px] no-scrollbar py-1">
                  {searchTerm && filteredNodes.map((node: any) => (
                    <button key={node.id} onClick={() => handleNodeSelect(node)} className="flex-shrink-0 px-3 py-1 bg-blue-600 text-white text-[9px] font-black rounded-lg shadow-lg shadow-blue-600/20 transition-all">{node.label}</button>
                  ))}
                </div>
              </div>
              <div className="flex justify-end items-center gap-4">
                <span className="text-[9px] font-black text-slate-500 uppercase tracking-widest">{filteredNodes.length} NODES_SYNCED</span>
              </div>
            </div>
            <div className="flex-1 overflow-y-auto custom-scrollbar pt-6 pb-10">
              {vizData && <ERDDiagram data={{...vizData.mapping_data, nodes: filteredNodes}} openNodes={openNodes} topNodeId={topNodeId} onNodeClick={handleDiagramNodeSelect} showDiagram={showDiagram} />}
            </div>
          </div>
        </div>

        {/* [Layer 3] 글로벌 팝업 레이어 */}
        <div className="absolute inset-0 z-50 pointer-events-none overflow-hidden">
          {openNodes.map((node) => (
            <DraggableWindow key={node.id} node={node} onClose={handleNodeClose} onFocus={setTopNodeId} isTop={topNodeId === node.id} zIndex={topNodeId === node.id ? 100 : 10} />
          ))}
        </div>
      </div>

      {/* 온보딩 가이드 레이어 */}
      {showOnboarding && (
        <Onboarding onComplete={() => setShowOnboarding(false)} />
      )}

      {/* AI 에이전트 입력창 */}
      <div className={`absolute top-0 right-0 h-full w-full sm:w-[500px] bg-white/95 backdrop-blur-3xl border-l border-slate-200 z-[60] shadow-[-20px_0_100px_rgba(0,0,0,0.05)] transition-all duration-500 transform ${isEditorOpen ? 'translate-x-0' : 'translate-x-full'}`}>
        <div className="flex flex-col h-full text-slate-900">
          {/* Header - Padding maintained for title */}
          <div className="flex justify-between items-center p-10 pb-6">
            <h2 className="text-2xl font-black italic tracking-tighter">NEURAL INPUT</h2>
            <button onClick={() => setIsEditorOpen(false)} className="text-slate-400 hover:text-slate-900 transition-colors text-xl">✕</button>
          </div>

          <div className="flex-1 flex flex-col px-6 pb-10 space-y-6 overflow-y-auto custom-scrollbar">
            {/* Render Mode Toggle */}
            <div className="flex gap-2 bg-slate-100 p-1.5 rounded-2xl border border-slate-200">
              {[{ id: 'auto', label: 'AUTO', icon: '✨' }, { id: 'diagram', label: 'DIAGRAM', icon: '🕸️' }, { id: 'settlement', label: 'TABLE/BAR', icon: '📊' }].map((mode) => (
                <button key={mode.id} onClick={() => setRenderType(mode.id)} className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-xl text-[10px] font-black transition-all ${renderType === mode.id ? 'bg-white text-blue-600 shadow-md border border-slate-200' : 'text-slate-500 hover:bg-white/50'}`}><span>{mode.icon}</span>{mode.label}</button>
              ))}
            </div>

            {/* File Upload Area */}
            <div className="space-y-4">
              <div className={`p-8 border-2 border-dashed rounded-[2rem] text-center group transition-all cursor-pointer ${selectedFile ? 'border-blue-500 bg-blue-50' : 'border-slate-200 bg-slate-50 hover:border-blue-400'}`} onClick={() => fileInputRef.current?.click()}>
                <input type="file" ref={fileInputRef} onChange={handleFileChange} className="hidden" accept=".pdf,.txt,.xlsx,.xls,.csv" />
                <div className={`w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4 transition-transform ${selectedFile ? 'bg-blue-600 scale-110' : 'bg-slate-200 group-hover:scale-110'}`}>{selectedFile ? <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg> : <svg className="w-6 h-6 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" /></svg>}</div>
                {selectedFile ? <div className="space-y-1"><p className="text-sm font-black text-blue-600 truncate px-4">{selectedFile.name}</p><p className="text-[10px] text-slate-500 uppercase tracking-widest font-bold">Ready to process</p></div> : <><p className="text-sm font-bold text-slate-700">Drop your data files here</p><p className="text-[10px] text-slate-400 mt-1 uppercase tracking-widest font-bold">PDF, TXT, Excel, CSV supported</p></>}
              </div>
              {selectedFile && (
                <div className="flex gap-2">
                  <Button variant="primary" onClick={processFileUpload} disabled={loading} className="flex-1 py-3 text-[11px]">{loading ? "ANALYZING..." : "START AI ANALYSIS"}</Button>
                  <Button variant="ghost" onClick={() => setSelectedFile(null)} className="px-6 py-3 text-[11px]">CANCEL</Button>
                </div>
              )}
            </div>

            {/* Text Input Area - Optimized for Maximum Height */}
            <div className="flex-[2] flex flex-col min-h-0 space-y-4">
              <div className="relative flex items-center"><div className="flex-1 h-px bg-slate-100"></div><span className="px-4 text-[10px] text-slate-400 font-black uppercase tracking-widest">OR ENTER TEXT</span><div className="flex-1 h-px bg-slate-100"></div></div>
              <Input 
                type="textarea"
                value={jsonInput} 
                onChange={(e) => setJsonInput(e.target.value)} 
                className="flex-1 !space-y-0"
                placeholder="Paste your raw data or logic here..."
              />
            </div>

            <Button 
              variant="primary"
              onClick={handleSubmit} 
              disabled={loading} 
              className="w-full py-4 text-[13px] bg-slate-900 hover:bg-black shadow-2xl shadow-black/10 flex-shrink-0"
            >
              {loading ? "PROCESSING..." : "EXECUTE 3D ENGINE"}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
