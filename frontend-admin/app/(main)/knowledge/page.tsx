"use client";

import React, { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import axios from 'axios';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { useRequireAdminAuth } from '@/hooks/useRequireAdminAuth';

interface Knowledge {
  id: string;
  category: string;
  title: string;
  content: string;
  sourceUrl: string;
  updatedAt: string;
}

/** 수집 히스토리: 어디서·무엇을·언제 받아왔는지 상세 로우 */
interface FetchHistoryRow {
  id: string;
  sourceType: string;
  status: string;
  itemCount: number;
  errorMessage: string | null;
  paramsJson: string | null;
  fetchedAt: string;
}

/** Admin API 호출 시 인증 헤더 (관리자 로그인 시에만) */
function getAdminAuthHeaders(): Record<string, string> {
  if (typeof window === 'undefined') return {};
  const token = localStorage.getItem('adminToken');
  if (!token) return {};
  return { Authorization: `Bearer ${token}` };
}

export default function AdminKnowledgePage() {
  useRequireAdminAuth();
  const [knowledgeList, setKnowledgeList] = useState<Knowledge[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(24);
  const [searchInput, setSearchInput] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategories, setSelectedCategories] = useState<Set<string>>(new Set());
  const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [lawSearchTerm, setLawSearchTerm] = useState("");
  /** 마지막 수집 결과: 가져온 값 확인용 */
  const [lastCollectResult, setLastCollectResult] = useState<{ source: string; count: number; titles: string[]; error?: string } | null>(null);
  /** 수집 히스토리 상세 로우 (어디서 무엇을 언제 받아왔는지) */
  const [fetchHistory, setFetchHistory] = useState<FetchHistoryRow[]>([]);

  // 직접 입력 폼 상태
  const [formData, setFormData] = useState({
    category: 'FINANCE_TAX',
    title: '',
    content: '',
    sourceUrl: ''
  });

  const fetchKnowledge = async (page = 0) => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      params.set("page", String(page));
      params.set("size", String(pageSize));
      if (searchQuery.trim()) params.set("q", searchQuery.trim());
      selectedCategories.forEach((c) => params.append("category", c));
      const response = await axios.get(
        `${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge?${params}`,
        { headers: getAdminAuthHeaders() }
      );
      const data = response.data;
      setKnowledgeList(data.content ?? []);
      setTotalElements(data.totalElements ?? 0);
      setTotalPages(data.totalPages ?? 0);
    } catch (error) {
      console.error("Failed to fetch knowledge:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const { data } = await axios.get(`${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge/categories`, { headers: getAdminAuthHeaders() });
      setCategoryOptions(Array.isArray(data) ? data : []);
    } catch {
      setCategoryOptions([]);
    }
  };

  const fetchHistoryList = async () => {
    try {
      const { data } = await axios.get<FetchHistoryRow[]>(
        `${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge/fetch-history`,
        { headers: getAdminAuthHeaders() }
      );
      setFetchHistory(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error("Failed to fetch history:", e);
      setFetchHistory([]);
    }
  };

  const refreshAfterCollect = useCallback(() => {
    fetchKnowledge(0);
    fetchHistoryList();
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchCategories();
    fetchHistoryList();
  }, []);
  useEffect(() => {
    const t = setTimeout(() => setSearchQuery(searchInput), 400);
    return () => clearTimeout(t);
  }, [searchInput]);
  useEffect(() => {
    setPage(0);
  }, [searchQuery, selectedCategories]);
  useEffect(() => {
    fetchKnowledge(page);
  }, [page, searchQuery, selectedCategories]);

  const handleAddKnowledge = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await axios.post(`${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge`, formData, { headers: getAdminAuthHeaders() });
      alert("지식이 성공적으로 추가되었습니다.");
      setFormData({ category: 'FINANCE_TAX', title: '', content: '', sourceUrl: '' });
      refreshAfterCollect();
    } catch (error) {
      alert("추가 실패");
    } finally {
      setLoading(false);
    }
  };

  const handleFetchLaw = async () => {
    const query = lawSearchTerm.trim();
    const url = query
      ? `${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge/fetch-law?lawName=${encodeURIComponent(query)}`
      : `${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge/fetch-law`;
    setLastCollectResult(null);
    try {
      setLoading(true);
      const { data } = await axios.post<Knowledge[]>(url, null, { headers: getAdminAuthHeaders() });
      const list = Array.isArray(data) ? data : [];
      setLastCollectResult({ source: "법령", count: list.length, titles: list.map((k) => k.title) });
      setLawSearchTerm("");
      refreshAfterCollect();
      if (list.length === 0) {
        alert(
          query
            ? `"${query}" 검색 결과가 없습니다. .env에 LAW_API_OC가 설정되어 있는지 확인하세요.`
            : "전체 법령 수집 결과가 없습니다. LAW_API_OC(국가법령정보센터 API 키)를 확인하세요."
        );
      } else {
        const mode = query ? `"${query}" ` : "전체 ";
        alert(`법령 ${mode}${list.length}건 수집됨: ${list.map((k) => k.title).slice(0, 5).join(", ")}${list.length > 5 ? " 외 " + (list.length - 5) + "건" : ""}`);
      }
    } catch (err: unknown) {
      const msg = axios.isAxiosError(err) && err.response?.data?.error ? String(err.response.data.error) : "법령 수집 실패";
      setLastCollectResult({ source: "법령", count: 0, titles: [], error: msg });
      alert(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleFetchAll = async () => {
    const base = process.env.NEXT_PUBLIC_ADMIN_API_URL;
    const headers = getAdminAuthHeaders();
    const results = { dart: false, bok: false, law: false };
    let lawError: string | null = null;
    setLastCollectResult(null);
    try {
      setLoading(true);
      try {
        await axios.post(`${base}/api/admin/knowledge/fetch-dart`, null, { headers });
        results.dart = true;
      } catch (e) {
        console.error("DART 수집 실패", e);
      }
      try {
        await axios.post(`${base}/api/admin/knowledge/fetch-bok`, null, { headers });
        results.bok = true;
      } catch (e) {
        console.error("BOK 수집 실패", e);
      }
      try {
        const lawUrl = lawSearchTerm.trim()
          ? `${base}/api/admin/knowledge/fetch-law?lawName=${encodeURIComponent(lawSearchTerm.trim())}`
          : `${base}/api/admin/knowledge/fetch-law`;
        const { data } = await axios.post<Knowledge[]>(lawUrl, null, { headers });
        results.law = true;
        const list = Array.isArray(data) ? data : [];
        setLastCollectResult({ source: "법령", count: list.length, titles: list.map((k) => k.title) });
        setLawSearchTerm("");
      } catch (e) {
        lawError = axios.isAxiosError(e) && e.response?.data?.error ? String(e.response.data.error) : "법령 수집 실패";
        setLastCollectResult({ source: "법령", count: 0, titles: [], error: lawError });
        console.error("법령 수집 실패", e);
      }
      refreshAfterCollect();
      const done = [results.dart && "DART", results.bok && "한국은행(ECOS)", results.law && "법령"].filter(Boolean);
      const skipped = "";
      if (done.length === 0) {
        alert("전체 수집 실패. 관리자 로그인 후 다시 시도하세요. Admin WAS(8081)가 기동 중인지 확인하세요.");
      } else {
        alert(`전체 수집 완료: ${done.join(", ")}${skipped}${lawError ? "\n\n법령: " + lawError : ""}`);
      }
    } catch (e) {
      alert("전체 수집 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    try {
      await axios.delete(`${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge/${id}`, { headers: getAdminAuthHeaders() });
      refreshAfterCollect();
    } catch (error) {
      alert("삭제 실패");
    }
  };

  return (
    <div className="w-full max-w-[1920px] px-8 py-10 space-y-10">
      <div className="flex flex-col lg:flex-row lg:justify-between lg:items-end gap-6">
        <div>
          <h1 className="text-3xl font-black tracking-tighter italic">KNOWLEDGE MANAGEMENT</h1>
          <p className="text-slate-500 text-sm mt-2 font-bold uppercase tracking-widest">Admin Control Panel</p>
        </div>
        <div className="flex flex-wrap items-center gap-4">
          {/* 1. 호출 버튼 3개 */}
          <div className="flex items-center gap-2 flex-shrink-0 rounded-lg border border-slate-200 bg-slate-50/50 px-3 py-2">
            <Button onClick={async () => {
              setLastCollectResult(null);
              try {
                setLoading(true);
                const { data } = await axios.post<Knowledge[]>(`${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge/fetch-dart`, null, { headers: getAdminAuthHeaders() });
                const list = Array.isArray(data) ? data : [];
                setLastCollectResult({ source: "DART", count: list.length, titles: list.map((k) => k.title) });
                refreshAfterCollect();
                alert(list.length ? `DART ${list.length}건 수집됨` : "DART 수집 완료 (저장된 항목 없음)");
              } catch (e) {
                const msg = axios.isAxiosError(e) && e.response?.data?.error ? String(e.response.data.error) : "수집 실패";
                setLastCollectResult({ source: "DART", count: 0, titles: [], error: msg });
                alert(msg);
              } finally { setLoading(false); }
            }} disabled={loading} variant="ghost" className="border-green-200 text-green-600 text-sm">DART 수집</Button>
            <Button onClick={async () => {
              setLastCollectResult(null);
              try {
                setLoading(true);
                const { data } = await axios.post<Knowledge[]>(`${process.env.NEXT_PUBLIC_ADMIN_API_URL}/api/admin/knowledge/fetch-bok`, null, { headers: getAdminAuthHeaders() });
                const list = Array.isArray(data) ? data : [];
                setLastCollectResult({ source: "한국은행(ECOS)", count: list.length, titles: list.map((k) => k.title) });
                refreshAfterCollect();
                alert(list.length ? `한국은행 ${list.length}건 수집됨` : "한국은행 수집 완료 (저장된 항목 없음)");
              } catch (e) {
                const msg = axios.isAxiosError(e) && e.response?.data?.error ? String(e.response.data.error) : "수집 실패";
                setLastCollectResult({ source: "한국은행(ECOS)", count: 0, titles: [], error: msg });
                alert(msg);
              } finally { setLoading(false); }
            }} disabled={loading} variant="ghost" className="border-blue-200 text-blue-600 text-sm">한국은행 수집</Button>
            <Button onClick={handleFetchLaw} disabled={loading} variant="ghost" className="border-amber-200 text-amber-700 text-sm" title="빈칸: 전체 수집, 입력: 해당 법령만 검색">법령 수집</Button>
          </div>
          <span className="text-slate-200 font-bold flex-shrink-0 hidden sm:inline">|</span>
          {/* 2. 전체 수집 */}
          <div className="flex-shrink-0">
            <Button onClick={handleFetchAll} disabled={loading} variant="primary" className="border-indigo-300 bg-indigo-600 text-white">전체 수집</Button>
          </div>
          <span className="text-slate-200 font-bold flex-shrink-0 hidden sm:inline">|</span>
          {/* 3. 검색 입력 (법령명 - 법령 수집 시 사용) */}
          <div className="flex-shrink-0 min-w-[200px]">
            <Input
              placeholder="법령명 (비워두면 전체 수집)"
              value={lawSearchTerm}
              onChange={(e) => setLawSearchTerm(e.target.value)}
              className="w-full min-w-0"
            />
          </div>
        </div>
      </div>

      {/* 마지막 수집 결과: 가져온 값 확인용 */}
      {lastCollectResult && (
        <div className={`rounded-2xl border p-5 ${lastCollectResult.error ? "border-red-200 bg-red-50" : "border-emerald-200 bg-emerald-50"}`}>
          <h3 className="text-xs font-black uppercase tracking-widest text-slate-500 mb-2">마지막 수집 결과</h3>
          {lastCollectResult.error ? (
            <p className="text-sm font-bold text-red-700">{lastCollectResult.error}</p>
          ) : (
            <>
              <p className="text-sm font-bold text-slate-800">
                {lastCollectResult.source} · <span className="text-emerald-600">{lastCollectResult.count}건</span> 저장됨
              </p>
              {lastCollectResult.titles.length > 0 && (
                <ul className="mt-2 text-sm text-slate-600 list-disc list-inside space-y-0.5">
                  {lastCollectResult.titles.slice(0, 10).map((t, i) => (
                    <li key={i}>{t}</li>
                  ))}
                  {lastCollectResult.titles.length > 10 && (
                    <li className="text-slate-400">… 외 {lastCollectResult.titles.length - 10}건</li>
                  )}
                </ul>
              )}
            </>
          )}
        </div>
      )}

      {/* 수집 히스토리: 어디서·무엇을·언제 받아왔는지 상세 로우 */}
      <div className="rounded-2xl border border-slate-200 bg-white overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <h3 className="text-sm font-black uppercase tracking-widest text-slate-600">수집 히스토리 (상세 로우)</h3>
          <button type="button" onClick={fetchHistoryList} className="text-xs font-bold text-indigo-600 hover:underline">새로고침</button>
        </div>
        {fetchHistory.length === 0 ? (
          <div className="px-6 py-8 text-center text-slate-400 text-sm">
            수집 히스토리가 없습니다. DART/한국은행/법령 수집을 실행하면 여기에 로우가 쌓입니다.
            <br />
            <span className="text-[10px] mt-2 block">테이블이 없다면 quantum-api-service를 한 번 실행해 DB 마이그레이션(V9)을 적용하세요.</span>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-100 bg-slate-50">
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">출처</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">상태</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">항목 수</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">요청 파라미터</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">수집 시각</th>
                  <th className="px-4 py-3 font-black text-slate-500 uppercase tracking-wider">오류</th>
                </tr>
              </thead>
              <tbody>
                {fetchHistory.map((row) => (
                  <tr key={row.id} className="border-b border-slate-50 hover:bg-slate-50/50">
                    <td className="px-4 py-3 font-bold">{row.sourceType}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                        row.status === "SUCCESS" ? "bg-emerald-100 text-emerald-700" :
                        row.status === "FAILED" ? "bg-red-100 text-red-700" : "bg-amber-100 text-amber-700"
                      }`}>
                        {row.status}
                      </span>
                    </td>
                    <td className="px-4 py-3">{row.itemCount ?? 0}</td>
                    <td className="px-4 py-3 text-slate-600 max-w-[200px] truncate" title={row.paramsJson ?? ""}>{row.paramsJson ?? "—"}</td>
                    <td className="px-4 py-3 text-slate-600">{row.fetchedAt ? new Date(row.fetchedAt).toLocaleString("ko-KR") : "—"}</td>
                    <td className="px-4 py-3 text-red-600 max-w-[180px] truncate" title={row.errorMessage ?? ""}>{row.errorMessage ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
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

        {/* 지식 목록: 카테고리 필터 + 검색 + 페이징 */}
        <div className="md:col-span-2 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <h2 className="text-xl font-black italic">ACTIVE KNOWLEDGE BASE ({totalElements.toLocaleString()})</h2>
            <div className="flex flex-wrap items-center gap-3">
              <Input
                placeholder="제목·내용 검색 (400ms 디바운스)"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                className="w-48 min-w-0"
              />
            </div>
          </div>
          {/* 카테고리 필터 */}
          {categoryOptions.length > 0 && (
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-xs font-black text-slate-500 uppercase">카테고리</span>
              {categoryOptions.map((cat) => {
                const checked = selectedCategories.has(cat);
                return (
                  <label
                    key={cat}
                    className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold cursor-pointer border transition-all ${
                      checked ? "bg-indigo-100 border-indigo-300 text-indigo-700" : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
                    }`}
                  >
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => {
                        setSelectedCategories((prev) => {
                          const next = new Set(prev);
                          if (next.has(cat)) next.delete(cat);
                          else next.add(cat);
                          return next;
                        });
                      }}
                      className="sr-only"
                    />
                    {cat}
                  </label>
                );
              })}
              {selectedCategories.size > 0 && (
                <button
                  type="button"
                  onClick={() => setSelectedCategories(new Set())}
                  className="text-xs font-bold text-slate-500 hover:text-indigo-600"
                >
                  전체 해제
                </button>
              )}
            </div>
          )}
          {knowledgeList.length === 0 && !loading && (
            <div className="text-center py-20 text-slate-300 font-black italic tracking-widest">NO_KNOWLEDGE_FOUND</div>
          )}
          {loading && knowledgeList.length === 0 && (
            <div className="text-center py-20 text-slate-400 text-sm font-bold">로딩 중...</div>
          )}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {knowledgeList.map((k) => (
              <div key={k.id} className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-all group relative">
                <Link href={`/knowledge/${k.id}`} className="block p-5">
                  <span className="text-[9px] font-black bg-blue-100 text-blue-600 px-2 py-0.5 rounded-full uppercase tracking-widest">{k.category}</span>
                  <h3 className="text-base font-black mt-2 line-clamp-2 hover:text-indigo-600 transition-colors">{k.title}</h3>
                  <p className="text-sm text-slate-600 mt-2 line-clamp-2 leading-relaxed">{k.content}</p>
                </Link>
                    <button
                      type="button"
                      onClick={(e) => { e.preventDefault(); e.stopPropagation(); handleDelete(k.id); }}
                      className="absolute top-3 right-3 w-8 h-8 flex items-center justify-center rounded-full text-slate-300 hover:text-red-500 hover:bg-red-50 transition-colors"
                      aria-label="삭제"
                    >
                      ✕
                    </button>
              </div>
            ))}
          </div>
          {/* 페이징 */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-6">
              <button
                type="button"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0 || loading}
                className="px-4 py-2 rounded-xl text-sm font-bold border border-slate-200 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50"
              >
                이전
              </button>
              <span className="px-4 py-2 text-sm font-bold text-slate-600">
                {page + 1} / {totalPages}
              </span>
              <button
                type="button"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1 || loading}
                className="px-4 py-2 rounded-xl text-sm font-bold border border-slate-200 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50"
              >
                다음
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
