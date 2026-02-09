"use client";

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';

interface Knowledge {
  id: string;
  category: string;
  title: string;
  content: string;
  sourceUrl: string;
  updatedAt: string;
}

export default function AdminKnowledgePage() {
  const [knowledgeList, setKnowledgeList] = useState<Knowledge[]>([]);
  const [loading, setLoading] = useState(false);
  const [lawSearchTerm, setLawSearchTerm] = useState("");
  
  // 직접 입력 폼 상태
  const [formData, setFormData] = useState({
    category: 'FINANCE_TAX',
    title: '',
    content: '',
    sourceUrl: ''
  });

  const fetchKnowledge = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/api/admin/knowledge');
      setKnowledgeList(response.data);
    } catch (error) {
      console.error("Failed to fetch knowledge:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchKnowledge();
  }, []);

  const handleAddKnowledge = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await axios.post('http://localhost:8080/api/admin/knowledge', formData);
      alert("지식이 성공적으로 추가되었습니다.");
      setFormData({ category: 'FINANCE_TAX', title: '', content: '', sourceUrl: '' });
      fetchKnowledge();
    } catch (error) {
      alert("추가 실패");
    } finally {
      setLoading(false);
    }
  };

  const handleFetchLaw = async () => {
    if (!lawSearchTerm) return;
    try {
      setLoading(true);
      await axios.post(`http://localhost:8080/api/admin/knowledge/fetch-law?lawName=${lawSearchTerm}`);
      alert(`${lawSearchTerm} 관련 법령을 수집하여 저장했습니다.`);
      setLawSearchTerm("");
      fetchKnowledge();
    } catch (error) {
      alert("법령 수집 실패");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    try {
      await axios.delete(`http://localhost:8080/api/admin/knowledge/${id}`);
      fetchKnowledge();
    } catch (error) {
      alert("삭제 실패");
    }
  };

  return (
    <div className="p-10 max-w-6xl mx-auto space-y-10">
      <div className="flex justify-between items-end">
        <div>
          <h1 className="text-3xl font-black tracking-tighter italic">KNOWLEDGE MANAGEMENT</h1>
          <p className="text-slate-500 text-sm mt-2 font-bold uppercase tracking-widest">Admin Control Panel</p>
        </div>
        <div className="flex gap-2">
          <Button onClick={async () => {
            try {
              setLoading(true);
              await axios.post('http://localhost:8080/api/admin/knowledge/fetch-dart');
              alert("금감원 공시 정보를 수집했습니다.");
              fetchKnowledge();
            } catch (e) { alert("수집 실패"); } finally { setLoading(false); }
          }} disabled={loading} variant="ghost" className="border-green-200 text-green-600">DART 수집</Button>
          <Button onClick={async () => {
            try {
              setLoading(true);
              await axios.post('http://localhost:8080/api/admin/knowledge/fetch-bok');
              alert("한국은행 경제 지표를 수집했습니다.");
              fetchKnowledge();
            } catch (e) { alert("수집 실패"); } finally { setLoading(false); }
          }} disabled={loading} variant="ghost" className="border-blue-200 text-blue-600">BOK 수집</Button>
          <Input 
            placeholder="법령명 검색 (예: 부가가치세법)" 
            value={lawSearchTerm}
            onChange={(e) => setLawSearchTerm(e.target.value)}
            className="w-64"
          />
          <Button onClick={handleFetchLaw} disabled={loading} variant="primary">API 수집</Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
        {/* 직접 추가 폼 */}
        <div className="md:col-span-1 bg-slate-50 p-8 rounded-[2.5rem] border border-slate-200 space-y-6">
          <h2 className="text-xl font-black italic">ADD KNOWLEDGE</h2>
          <form onSubmit={handleAddKnowledge} className="space-y-4">
            <div className="space-y-1">
              <label className="text-[10px] font-black text-slate-400 uppercase ml-1">Category</label>
              <select 
                value={formData.category}
                onChange={(e) => setFormData({...formData, category: e.target.value})}
                className="w-full bg-white border border-slate-200 rounded-xl px-4 py-2 text-sm font-bold focus:outline-none focus:border-blue-500"
              >
                <option value="FINANCE_TAX">FINANCE_TAX</option>
                <option value="INFRA_ARCHITECTURE">INFRA_ARCHITECTURE</option>
                <option value="GENERAL_DOC">GENERAL_DOC</option>
              </select>
            </div>
            <Input 
              placeholder="Title" 
              value={formData.title}
              onChange={(e) => setFormData({...formData, title: e.target.value})}
              required
            />
            <textarea 
              placeholder="Content (Visual Rules, Laws, etc.)" 
              value={formData.content}
              onChange={(e) => setFormData({...formData, content: e.target.value})}
              className="w-full h-40 bg-white border border-slate-200 rounded-2xl px-4 py-3 text-sm font-bold focus:outline-none focus:border-blue-500"
              required
            />
            <Input 
              placeholder="Source URL" 
              value={formData.sourceUrl}
              onChange={(e) => setFormData({...formData, sourceUrl: e.target.value})}
            />
            <Button type="submit" variant="primary" className="w-full py-4" disabled={loading}>
              {loading ? "SAVING..." : "SAVE KNOWLEDGE"}
            </Button>
          </form>
        </div>

        {/* 지식 목록 */}
        <div className="md:col-span-2 space-y-6">
          <h2 className="text-xl font-black italic">ACTIVE KNOWLEDGE BASE ({knowledgeList.length})</h2>
          <div className="space-y-4">
            {knowledgeList.map((k) => (
              <div key={k.id} className="bg-white border border-slate-200 p-6 rounded-[2rem] shadow-sm hover:shadow-md transition-all group">
                <div className="flex justify-between items-start">
                  <div>
                    <span className="text-[9px] font-black bg-blue-100 text-blue-600 px-2 py-0.5 rounded-full uppercase tracking-widest">{k.category}</span>
                    <h3 className="text-lg font-black mt-2">{k.title}</h3>
                  </div>
                  <button onClick={() => handleDelete(k.id)} className="text-slate-300 hover:text-red-500 transition-colors">✕</button>
                </div>
                <p className="text-sm text-slate-600 mt-3 line-clamp-3 leading-relaxed">{k.content}</p>
                <div className="mt-4 flex justify-between items-center border-t border-slate-50 pt-4">
                  <span className="text-[10px] text-slate-400 font-bold italic">{k.sourceUrl || "No Source URL"}</span>
                  <span className="text-[10px] text-slate-400 font-bold">{new Date(k.updatedAt).toLocaleDateString()}</span>
                </div>
              </div>
            ))}
            {knowledgeList.length === 0 && !loading && (
              <div className="text-center py-20 text-slate-300 font-black italic tracking-widest">NO_KNOWLEDGE_FOUND</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
