"use client";

import { useEffect, useState, useCallback, useRef, useMemo } from "react";
import axios from "axios";
import QuantumUniverse, { DraggableWindow } from "@/components/QuantumUniverse";
import ERDDiagram from "@/components/ERDDiagram";

export default function QuantumUniversePage() {
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
  }, []);

  return (
    <main className="relative h-screen w-screen bg-[#f8f9fa] overflow-hidden flex flex-col font-sans text-slate-900">
      <header className="h-16 flex justify-between items-center z-40 border-b border-slate-200 bg-white/80 backdrop-blur-xl px-8">
        <div className="flex items-center gap-4">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center shadow-[0_0_20px_rgba(37,99,235,0.2)]">
            <span className="font-black text-lg italic text-white">Q</span>
          </div>
          <h1 className="text-xl font-black italic tracking-tighter text-slate-900">QUANTUM<span className="text-blue-600">VIZ</span></h1>
        </div>
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-3 bg-slate-100 px-4 py-2 rounded-full border border-slate-200">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Auto Focus</span>
            <button onClick={() => setAutoFocus(!autoFocus)} className={`relative w-10 h-5 rounded-full transition-colors duration-300 ${autoFocus ? 'bg-blue-600' : 'bg-slate-300'}`}>
              <div className={`absolute top-1 w-3 h-3 bg-white rounded-full transition-transform duration-300 ${autoFocus ? 'left-6' : 'left-1'}`} />
            </button>
          </div>
          <div className="flex items-center gap-3 bg-slate-100 px-4 py-2 rounded-full border border-slate-200">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Diagram</span>
            <button onClick={() => setShowDiagram(!showDiagram)} className={`relative w-10 h-5 rounded-full transition-colors duration-300 ${showDiagram ? 'bg-blue-600' : 'bg-slate-300'}`}>
              <div className={`absolute top-1 w-3 h-3 bg-white rounded-full transition-transform duration-300 ${showDiagram ? 'left-6' : 'left-1'}`} />
            </button>
          </div>
          <button onClick={() => setIsEditorOpen(true)} className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-black rounded-full transition-all shadow-lg shadow-blue-600/20">+ NEW MAPPING</button>
        </div>
      </header>

      <div className="flex-1 flex w-full overflow-hidden relative bg-white">
        {/* [Layer 1] 3D 유니버스 - 전체 배경 (고정 레이아웃) */}
        <div className="absolute inset-0 z-10">
          {vizData ? (
            <QuantumUniverse 
              data={vizData.mapping_data} 
              openNodes={openNodes} 
              topNodeId={topNodeId} 
              showPopups={false}
              autoFocus={autoFocus}
              onNodeSelect={handleNodeSelect} 
              onNodeClose={handleNodeClose} 
              onNodeFocus={setTopNodeId}
              // 유저 요청: 인풋 없는 레이아웃 조정 제거 (항상 중앙 고정)
              centerOffset={[0, 0, 0]} 
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center font-mono text-slate-200 tracking-[1em] animate-pulse">INITIALIZING_CANVAS...</div>
          )}
        </div>

        {/* [Layer 2] 하단 다이어그램 바 - 순수 오버레이 팝업 방식 (Bottom Sheet) */}
        <div 
          className={`absolute bottom-0 left-0 right-0 h-[480px] z-30 transition-all duration-1000 transform-gpu
            ${showDiagram && vizData ? 'translate-y-0' : 'translate-y-full'}`}
          style={{ transitionTimingFunction: 'cubic-bezier(0.16, 1, 0.3, 1)' }}
        >
          <div className="w-full h-full bg-white/40 backdrop-blur-3xl border-t border-slate-200/50 shadow-[0_-40px_100px_rgba(0,0,0,0.05)] relative flex flex-col rounded-t-[3.5rem]">
            <div className="h-14 flex-shrink-0 flex items-center justify-center cursor-pointer" onClick={() => setShowDiagram(false)}>
              <div className="w-20 h-1.5 bg-slate-300/60 rounded-full" />
            </div>
            <div className="h-16 border-b border-slate-100/50 flex items-center px-12 justify-between bg-white/10">
              <div className="flex items-center gap-6 flex-1">
                <div className="relative w-80">
                  <input type="text" placeholder="Search nodes or values..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} className="w-full bg-white/50 backdrop-blur-md border border-slate-200/50 rounded-2xl px-5 py-2.5 text-xs font-bold text-slate-700 focus:ring-2 focus:ring-blue-500/20 transition-all" />
                  <div className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400">🔍</div>
                </div>
                <div className="flex gap-2 overflow-x-auto max-w-[600px] no-scrollbar py-2">
                  {searchTerm && filteredNodes.map((node: any) => (
                    <button key={node.id} onClick={() => handleNodeSelect(node)} className="flex-shrink-0 px-4 py-2 bg-blue-600 text-white text-[10px] font-black rounded-xl shadow-lg shadow-blue-600/20 transition-all">{node.label}</button>
                  ))}
                </div>
              </div>
              <div className="flex justify-end items-center gap-4">
                <span className="text-[10px] font-black text-slate-500 uppercase tracking-widest">{filteredNodes.length} NODES_SYNCED</span>
              </div>
            </div>
            <div className="flex-1 overflow-y-auto custom-scrollbar pt-12 pb-20">
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

      <div className={`absolute top-0 right-0 h-full w-full sm:w-[550px] bg-white/95 backdrop-blur-3xl border-l border-slate-200 z-50 shadow-[-20px_0_100px_rgba(0,0,0,0.05)] transition-all duration-500 transform ${isEditorOpen ? 'translate-x-0' : 'translate-x-full'}`}>
        <div className="flex flex-col h-full p-10 text-slate-900">
          <div className="flex justify-between items-center mb-10">
            <h2 className="text-2xl font-black italic tracking-tighter">NEURAL INPUT</h2>
            <button onClick={() => setIsEditorOpen(false)} className="text-slate-400 hover:text-slate-900 transition-colors text-xl">✕</button>
          </div>
          <div className="flex gap-2 mb-8 bg-slate-100 p-1.5 rounded-2xl border border-slate-200">
            {[{ id: 'auto', label: 'AUTO', icon: '✨' }, { id: 'diagram', label: 'DIAGRAM', icon: '🕸️' }, { id: 'settlement', label: 'TABLE/BAR', icon: '📊' }].map((mode) => (
              <button key={mode.id} onClick={() => setRenderType(mode.id)} className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-xl text-[10px] font-black transition-all ${renderType === mode.id ? 'bg-white text-blue-600 shadow-md border border-slate-200' : 'text-slate-500 hover:bg-white/50'}`}><span>{mode.icon}</span>{mode.label}</button>
            ))}
          </div>
          <div className="mb-8">
            <div className={`p-6 border-2 border-dashed rounded-3xl text-center group transition-all cursor-pointer ${selectedFile ? 'border-blue-500 bg-blue-50' : 'border-slate-200 bg-slate-50 hover:border-blue-400'}`} onClick={() => fileInputRef.current?.click()}>
              <input type="file" ref={fileInputRef} onChange={handleFileChange} className="hidden" accept=".pdf,.txt,.xlsx,.xls,.csv" />
              <div className={`w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4 transition-transform ${selectedFile ? 'bg-blue-600 scale-110' : 'bg-slate-200 group-hover:scale-110'}`}>{selectedFile ? <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg> : <svg className="w-6 h-6 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" /></svg>}</div>
              {selectedFile ? <div className="space-y-1"><p className="text-sm font-black text-blue-600 truncate px-4">{selectedFile.name}</p><p className="text-[10px] text-slate-500 uppercase tracking-widest font-bold">Ready to process</p></div> : <><p className="text-sm font-bold text-slate-700">Drop your data files here</p><p className="text-[10px] text-slate-400 mt-1 uppercase tracking-widest font-bold">PDF, TXT, Excel, CSV supported</p></>}
            </div>
            {selectedFile && <div className="mt-4 flex gap-2"><button onClick={processFileUpload} disabled={loading} className="flex-1 py-4 bg-blue-600 hover:bg-blue-700 text-white text-[10px] font-black rounded-2xl transition-all shadow-lg shadow-blue-600/20">{loading ? "ANALYZING..." : "START AI ANALYSIS"}</button><button onClick={() => setSelectedFile(null)} className="px-6 py-4 bg-slate-100 hover:bg-slate-200 text-slate-600 text-[10px] font-black rounded-2xl transition-all border border-slate-200">CANCEL</button></div>}
          </div>
          <div className="relative flex items-center mb-6"><div className="flex-1 h-px bg-slate-100"></div><span className="px-4 text-[10px] text-slate-400 font-black uppercase tracking-widest">OR ENTER TEXT</span><div className="flex-1 h-px bg-slate-100"></div></div>
          <textarea value={jsonInput} onChange={(e) => setJsonInput(e.target.value)} className="flex-1 w-full bg-slate-50 border border-slate-200 rounded-3xl p-8 font-mono text-sm text-slate-700 focus:outline-none focus:border-blue-400 transition-all resize-none shadow-inner" placeholder="Paste your raw data or logic here..." spellCheck={false} />
          <button onClick={handleSubmit} disabled={loading} className="mt-8 w-full py-5 bg-slate-900 hover:bg-black text-white font-black rounded-2xl transition-all shadow-2xl shadow-black/10">{loading ? "PROCESSING..." : "EXECUTE 3D ENGINE"}</button>
        </div>
      </div>
    </main>
  );
}
