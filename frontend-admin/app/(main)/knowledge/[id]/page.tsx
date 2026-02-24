"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import axios from "axios";
import { adminApi } from "@/lib/adminApi";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface Knowledge {
  id: string;
  category: string;
  title: string;
  content: string;
  articleBody?: string; // 법령 조문 본문 (있으면 상세에 우선 표시)
  sourceUrl: string;
  updatedAt: string;
  createdAt?: string;
}

export default function KnowledgeDetailPage() {
  useRequireAdminAuth();
  const params = useParams();
  const id = params?.id as string | undefined;
  const [knowledge, setKnowledge] = useState<Knowledge | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      setLoading(false);
      setError("ID가 없습니다.");
      return;
    }
    const fetchDetail = async () => {
      try {
        setLoading(true);
        setError(null);
        const { data } = await adminApi.get<Knowledge>(
          `/api/admin/knowledge/${id}`
        );
        setKnowledge(data);
      } catch (e) {
        if (axios.isAxiosError(e) && e.response?.data) {
          const msg = (e.response.data as { error?: string }).error;
          const status = e.response.status;
          if (status === 404) {
            setError(msg || "해당 지식을 찾을 수 없습니다.");
          } else if (status === 401) {
            setError("로그인이 필요합니다. 다시 로그인해 주세요.");
          } else if (status === 403) {
            setError(msg || "관리자 권한이 필요합니다.");
          } else {
            setError(msg || `불러오기에 실패했습니다. (${status})`);
          }
        } else {
          setError("네트워크 오류이거나 서버에 연결할 수 없습니다. Admin WAS(8081)를 확인하세요.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  if (loading) {
    return (
      <div className="p-10 min-h-[60vh] flex items-center justify-center">
        <p className="text-slate-400 font-bold">로딩 중...</p>
      </div>
    );
  }

  if (error || !knowledge) {
    return (
      <div className="p-10 min-h-[60vh] flex flex-col items-center justify-center gap-4">
        <p className="text-red-600 font-bold">{error || "데이터 없음"}</p>
        <Link href="/knowledge" className="text-indigo-600 font-bold hover:underline">
          ← 목록으로
        </Link>
      </div>
    );
  }

  // 법령은 articleBody 우선, 없으면 content 사용
  const displayContent = knowledge.articleBody || knowledge.content || "";
  // 본문을 문단/줄 단위로 나눠 상세 리스트로 표시 (빈 줄은 구분자)
  const contentBlocks = displayContent
    .split(/\n\n+/)
    .map((block) => block.trim())
    .filter(Boolean);
  const hasList = contentBlocks.length > 1;

  return (
    <div className="w-full max-w-[1200px] px-8 py-10 space-y-8">
      <div className="flex items-center gap-4">
        <Link
          href="/knowledge"
          className="text-slate-500 hover:text-slate-900 font-bold text-sm uppercase tracking-widest"
        >
          ← 목록
        </Link>
      </div>

      <article className="bg-white border border-slate-200 rounded-[2rem] shadow-sm overflow-hidden">
        <div className="p-8 md:p-10 border-b border-slate-100">
          <span className="text-[9px] font-black bg-blue-100 text-blue-600 px-2 py-0.5 rounded-full uppercase tracking-widest">
            {knowledge.category}
          </span>
          <h1 className="text-2xl md:text-3xl font-black mt-3 tracking-tight">
            {knowledge.title}
          </h1>
          <div className="mt-4 flex flex-wrap items-center gap-4 text-[10px] text-slate-400 font-bold">
            {knowledge.updatedAt && (
              <span>수정일: {new Date(knowledge.updatedAt).toLocaleDateString("ko-KR")}</span>
            )}
            {knowledge.createdAt && (
              <span>등록일: {new Date(knowledge.createdAt).toLocaleDateString("ko-KR")}</span>
            )}
          </div>
        </div>

        <div className="p-8 md:p-10">
          <h2 className="text-sm font-black text-slate-400 uppercase tracking-widest mb-4">
            상세 목록
          </h2>
          {hasList ? (
            <ol className="space-y-4 list-none pl-0">
              {contentBlocks.map((block, index) => (
                <li
                  key={index}
                  className="flex gap-4 p-4 rounded-xl bg-slate-50 border border-slate-100"
                >
                  <span className="shrink-0 w-8 h-8 flex items-center justify-center rounded-lg bg-slate-200 text-slate-600 text-xs font-black">
                    {index + 1}
                  </span>
                  <p className="text-slate-700 leading-relaxed flex-1 min-w-0 whitespace-pre-wrap font-medium">
                    {block}
                  </p>
                </li>
              ))}
            </ol>
          ) : (
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 text-slate-700 leading-relaxed whitespace-pre-wrap font-medium">
              {displayContent || "내용 없음"}
            </div>
          )}
        </div>

        {knowledge.sourceUrl && (
          <div className="px-8 md:px-10 pb-8">
            <a
              href={knowledge.sourceUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-sm font-bold text-indigo-600 hover:underline break-all"
            >
              출처: {knowledge.sourceUrl}
            </a>
          </div>
        )}
      </article>
    </div>
  );
}
