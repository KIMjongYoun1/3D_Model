"use client";

import { useEffect, useState, useCallback, useRef } from "react";
import axios from "axios";
import QuantumUniverse from "@/components/QuantumUniverse";
import ERDDiagram from "@/components/ERDDiagram";

export default function QuantumUniversePage() {
  const [vizData, setVizData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditorOpen, setIsEditorOpen] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  const [openNodes, setOpenNodes] = useState<any[]>([]);
  const [topNodeId, setTopNodeId] = useState<string | null>(null);
  const [show3DPopups, setShow3DPopups] = useState(true);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  
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
        raw_data: payloadData
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

  // 파일 선택 핸들러
  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedFile(file);
    }
  };

  // 실제 파일 업로드 및 분석 실행
  const processFileUpload = async () => {
    if (!selectedFile) return;

    try {
      setLoading(true);
      const formData = new FormData();
      formData.append("file", selectedFile);

      const response = await axios.post("http://localhost:8000/api/v1/mapping/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" }
      });

      setVizData(response.data);
      setIsEditorOpen(false);
      setOpenNodes([]);
      setTopNodeId(null);
      setSelectedFile(null); // 초기화
      alert("문서 분석 및 시각화가 완료되었습니다!");
    } catch (err: any) {
      console.error("File upload failed:", err);
      setError(err.response?.data?.detail || "파일 업로드 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleNodeSelect = useCallback((node: any) => {
    if (!node) return;
    setOpenNodes(prev => {
      if (prev.find(n => n.id === node.id)) return prev;
      return [...prev, node];
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
    <main className="relative h-screen w-screen bg-[#010409] overflow-hidden flex flex-col font-sans text-white">
      <header className="h-16 flex justify-between items-center z-40 border-b border-white/5 bg-black/40 backdrop-blur-xl px-8">
        <div className="flex items-center gap-4">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center shadow-[0_0_20px_rgba(37,99,235,0.4)]">
            <span className="font-black text-lg italic">Q</span>
          </div>
          <h1 className="text-xl font-black italic tracking-tighter">QUANTUM<span className="text-blue-500">VIZ</span></h1>
        </div>

        <div className="flex items-center gap-6">
          <div className="flex items-center gap-3 bg-white/5 px-4 py-2 rounded-full border border-white/10">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">3D Popups</span>
            <button onClick={() => setShow3DPopups(!show3DPopups)} className={`relative w-10 h-5 rounded-full transition-colors duration-300 ${show3DPopups ? 'bg-blue-600' : 'bg-slate-700'}`}>
              <div className={`absolute top-1 w-3 h-3 bg-white rounded-full transition-transform duration-300 ${show3DPopups ? 'left-6' : 'left-1'}`} />
            </button>
          </div>
          <button onClick={() => setIsEditorOpen(true)} className="px-6 py-2 bg-blue-600 hover:bg-blue-500 text-xs font-black rounded-full transition-all border border-white/10">+ NEW MAPPING</button>
        </div>
      </header>

      <div className="flex-1 flex w-full overflow-hidden">
        <div className="w-1/2 h-full border-r border-white/5 relative">
          {vizData ? (
            <QuantumUniverse data={vizData.mapping_data} openNodes={openNodes} topNodeId={topNodeId} showPopups={show3DPopups} onNodeSelect={handleNodeSelect} onNodeClose={handleNodeClose} onNodeFocus={setTopNodeId} />
          ) : (
            <div className="w-full h-full flex items-center justify-center font-mono text-slate-700">AWAITING_CORE_SYNC...</div>
          )}
        </div>
        <div className="w-1/2 h-full bg-black/20 relative shadow-[inset_20px_0_40px_rgba(0,0,0,0.4)]">
          {vizData ? (
            <ERDDiagram data={vizData.mapping_data} openNodes={openNodes} topNodeId={topNodeId} onNodeClick={handleNodeSelect} />
          ) : (
            <div className="w-full h-full flex items-center justify-center font-mono text-slate-700">AWAITING_SCHEMATIC...</div>
          )}
        </div>
      </div>

      {/* AI 에이전트 입력창 */}
      <div className={`absolute top-0 right-0 h-full w-full sm:w-[550px] bg-slate-900/98 backdrop-blur-3xl border-l border-white/10 z-50 shadow-[-50px_0_100px_rgba(0,0,0,0.5)] transition-all duration-500 transform ${isEditorOpen ? 'translate-x-0' : 'translate-x-full'}`}>
        <div className="flex flex-col h-full p-10 text-white">
          <div className="flex justify-between items-center mb-10">
            <h2 className="text-2xl font-black italic">NEURAL INPUT</h2>
            <button onClick={() => setIsEditorOpen(false)} className="text-slate-400">✕</button>
          </div>
          
          {/* 파일 업로드 섹션 */}
          <div className="mb-8">
            <div 
              className={`p-6 border-2 border-dashed rounded-3xl text-center group transition-all cursor-pointer 
                ${selectedFile ? 'border-blue-500 bg-blue-500/10' : 'border-blue-500/20 bg-blue-500/5 hover:border-blue-500/40'}`} 
              onClick={() => fileInputRef.current?.click()}
            >
              <input type="file" ref={fileInputRef} onChange={handleFileChange} className="hidden" accept=".pdf,.txt,.xlsx,.xls,.csv" />
              <div className={`w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4 transition-transform ${selectedFile ? 'bg-blue-500 scale-110' : 'bg-blue-500/10 group-hover:scale-110'}`}>
                {selectedFile ? (
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
                ) : (
                  <svg className="w-6 h-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" /></svg>
                )}
              </div>
              
              {selectedFile ? (
                <div className="space-y-1">
                  <p className="text-sm font-black text-blue-400 truncate px-4">{selectedFile.name}</p>
                  <p className="text-[10px] text-slate-500">파일이 선택되었습니다. 아래 실행 버튼을 눌러주세요.</p>
                </div>
              ) : (
                <>
                  <p className="text-sm font-bold text-blue-300">문서 파일 업로드 (PDF, TXT, Excel)</p>
                  <p className="text-[10px] text-slate-500 mt-1 uppercase tracking-widest">AI가 자동으로 분석하여 시각화합니다</p>
                </>
              )}
            </div>

            {selectedFile && (
              <div className="mt-4 flex gap-2">
                <button 
                  onClick={processFileUpload} 
                  disabled={loading}
                  className="flex-1 py-3 bg-blue-600 hover:bg-blue-500 text-[10px] font-black rounded-xl transition-all shadow-lg shadow-blue-500/20"
                >
                  {loading ? "ANALYZING..." : "START AI ANALYSIS"}
                </button>
                <button 
                  onClick={() => setSelectedFile(null)} 
                  className="px-4 py-3 bg-white/5 hover:bg-white/10 text-[10px] font-bold rounded-xl transition-all border border-white/10"
                >
                  CANCEL
                </button>
              </div>
            )}
          </div>

          <div className="relative flex items-center mb-6">
            <div className="flex-1 h-px bg-white/5"></div>
            <span className="px-4 text-[10px] text-slate-600 font-bold uppercase tracking-widest">OR ENTER TEXT</span>
            <div className="flex-1 h-px bg-white/5"></div>
          </div>

          <textarea value={jsonInput} onChange={(e) => setJsonInput(e.target.value)} className="flex-1 w-full bg-black/60 border border-white/5 rounded-3xl p-8 font-mono text-sm text-blue-300 focus:outline-none focus:border-blue-500/30 transition-all resize-none shadow-inner" placeholder="Enter JSON or plain text here..." spellCheck={false} />
          <button onClick={handleSubmit} disabled={loading} className="mt-8 w-full py-5 bg-blue-600 hover:bg-blue-500 text-white font-black rounded-2xl transition-all shadow-2xl shadow-blue-500/20">{loading ? "PROCESSING..." : "EXECUTE 3D ENGINE"}</button>
        </div>
      </div>
    </main>
  );
}
