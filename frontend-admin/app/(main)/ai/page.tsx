"use client";

import React, { useState, useRef, useEffect } from "react";
import { useRequireAdminAuth } from "@/hooks/useRequireAdminAuth";

interface Message {
  id: string;
  role: "user" | "assistant";
  content: string;
  intent?: string;
  dataSummary?: string | null;
  modelUsed?: string;
  timestamp: Date;
}

const ADMIN_AI_URL = process.env.NEXT_PUBLIC_ADMIN_AI_URL || "http://localhost:8002";

const EXAMPLE_PROMPTS = [
  "이번 달 결제 현황 요약해줘",
  "신규 가입자 통계 알려줘",
  "지식 베이스 요약해줘",
  "부가가치세법 관련 정리해줘",
];

const INTENT_LABELS: Record<string, { label: string; color: string }> = {
  payment_analysis: { label: "결제 분석", color: "bg-emerald-100 text-emerald-700" },
  user_statistics: { label: "사용자 통계", color: "bg-blue-100 text-blue-700" },
  knowledge_query: { label: "지식 조회", color: "bg-amber-100 text-amber-700" },
  general: { label: "일반 질문", color: "bg-slate-100 text-slate-600" },
};

const CATEGORY_TABS: { id: string; label: string }[] = [
  { id: "knowledge_query", label: "지식/법령" },
  { id: "payment_analysis", label: "결제" },
  { id: "user_statistics", label: "사용자" },
  { id: "general", label: "일반" },
];

export default function AdminAIPage() {
  useRequireAdminAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [category, setCategory] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const sendMessage = async (prompt?: string, categoryOverride?: string) => {
    const text = prompt || input.trim();
    const cat = categoryOverride ?? category;
    if (!text || loading) return;
    if (!cat) {
      alert("카테고리를 선택해주세요.");
      return;
    }

    const userMessage: Message = {
      id: `user-${Date.now()}`,
      role: "user",
      content: text,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setLoading(true);

    try {
      const response = await fetch(`${ADMIN_AI_URL}/api/admin-ai/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt: text, category: cat }),
      });

      if (!response.ok) {
        throw new Error(`서버 오류: ${response.status}`);
      }

      const data = await response.json();

      const aiMessage: Message = {
        id: `ai-${Date.now()}`,
        role: "assistant",
        content: data.answer,
        intent: data.intent,
        dataSummary: data.data_summary,
        modelUsed: data.model_used,
        timestamp: new Date(),
      };

      setMessages((prev) => [...prev, aiMessage]);
    } catch (error) {
      const errorMessage: Message = {
        id: `error-${Date.now()}`,
        role: "assistant",
        content:
          "AI 서버에 연결할 수 없습니다.\n\n" +
          "다음을 확인해주세요:\n" +
          "1. Admin AI Server가 실행 중인지 확인 (포트 8002)\n" +
          "2. Ollama가 실행 중인지 확인: ollama serve\n" +
          "3. 또는 GEMINI_API_KEY 환경변수 설정",
        intent: "general",
        modelUsed: "error",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="flex flex-col flex-1 min-h-0 w-full">
      {/* Header */}
      <div className="px-6 sm:px-8 pt-6 sm:pt-8 pb-4 flex-shrink-0">
        <h1 className="text-3xl font-black tracking-tighter italic">
          ADMIN AI <span className="text-indigo-600">ASSISTANT</span>
        </h1>
        <p className="text-slate-500 text-sm mt-1 font-bold uppercase tracking-widest">
          자연어 프롬프트로 관리 업무를 지원합니다
        </p>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto overflow-x-hidden px-6 sm:px-8 space-y-4 pb-4 min-h-0">
        {messages.length === 0 && (
          <div className="flex flex-col items-center justify-center h-full space-y-8">
            <div className="w-20 h-20 bg-indigo-100 rounded-3xl flex items-center justify-center">
              <span className="text-4xl font-black italic text-indigo-600">Q</span>
            </div>
            <div className="text-center space-y-2">
              <p className="text-slate-400 font-bold text-sm uppercase tracking-widest">
                프롬프트를 입력하거나 아래 예시를 클릭하세요
              </p>
            </div>
            <div className="flex flex-wrap gap-3 justify-center w-full max-w-2xl mx-auto px-2">
              {EXAMPLE_PROMPTS.map((prompt, i) => (
                <button
                  key={i}
                  onClick={() => {
                    const catMap = ["payment_analysis", "user_statistics", "knowledge_query", "knowledge_query"];
                    sendMessage(prompt, catMap[Math.min(i, 3)]);
                  }}
                  className="px-4 py-2.5 bg-slate-50 hover:bg-indigo-50 border border-slate-200 
                             hover:border-indigo-300 rounded-2xl text-sm font-bold text-slate-600 
                             hover:text-indigo-700 transition-all duration-200"
                >
                  {prompt}
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
          >
            <div
              className={`max-w-[85%] rounded-[1.5rem] px-6 py-4 ${
                msg.role === "user"
                  ? "bg-indigo-600 text-white"
                  : "bg-white border border-slate-200 shadow-sm"
              }`}
            >
              {/* Intent Badge (AI messages only) */}
              {msg.role === "assistant" && msg.intent && (
                <div className="flex items-center gap-2 mb-3">
                  <span
                    className={`text-[9px] font-black px-2 py-0.5 rounded-full uppercase tracking-widest ${
                      INTENT_LABELS[msg.intent]?.color || "bg-slate-100 text-slate-500"
                    }`}
                  >
                    {INTENT_LABELS[msg.intent]?.label || msg.intent}
                  </span>
                  {msg.modelUsed && msg.modelUsed !== "error" && (
                    <span className="text-[9px] font-bold text-slate-400 uppercase">
                      {msg.modelUsed}
                    </span>
                  )}
                </div>
              )}

              {/* Data Summary (collapsible) */}
              {msg.role === "assistant" && msg.dataSummary && (
                <details className="mb-3">
                  <summary className="text-[10px] font-black text-slate-400 uppercase tracking-widest cursor-pointer hover:text-slate-600 transition-colors">
                    DB 조회 결과 보기
                  </summary>
                  <pre className="mt-2 text-[11px] text-slate-500 bg-slate-50 rounded-xl p-3 whitespace-pre-wrap font-mono leading-relaxed overflow-x-auto">
                    {msg.dataSummary}
                  </pre>
                </details>
              )}

              {/* Message Content */}
              <div
                className={`text-sm leading-relaxed whitespace-pre-wrap ${
                  msg.role === "user" ? "font-bold" : "text-slate-700"
                }`}
              >
                {msg.content}
              </div>

              {/* Timestamp */}
              <div
                className={`mt-2 text-[9px] font-bold ${
                  msg.role === "user" ? "text-indigo-300" : "text-slate-300"
                }`}
              >
                {msg.timestamp.toLocaleTimeString("ko-KR", {
                  hour: "2-digit",
                  minute: "2-digit",
                })}
              </div>
            </div>
          </div>
        ))}

        {/* Loading Indicator */}
        {loading && (
          <div className="flex justify-start">
            <div className="bg-white border border-slate-200 rounded-[1.5rem] px-6 py-4 shadow-sm">
              <div className="flex items-center gap-2">
                <div className="flex gap-1">
                  <div className="w-2 h-2 bg-indigo-400 rounded-full animate-bounce" style={{ animationDelay: "0ms" }} />
                  <div className="w-2 h-2 bg-indigo-400 rounded-full animate-bounce" style={{ animationDelay: "150ms" }} />
                  <div className="w-2 h-2 bg-indigo-400 rounded-full animate-bounce" style={{ animationDelay: "300ms" }} />
                </div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                  분석 중...
                </span>
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="px-6 sm:px-8 pb-6 pt-2 flex-shrink-0 space-y-3">
        {/* Category Tabs - 필수 선택 */}
        <div className="flex gap-2 flex-wrap items-center">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-widest">질문 유형</span>
          {CATEGORY_TABS.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setCategory(tab.id)}
              className={`px-4 py-2 rounded-xl text-sm font-bold transition-all ${
                category === tab.id
                  ? INTENT_LABELS[tab.id]?.color || "bg-indigo-100 text-indigo-700"
                  : "bg-slate-50 text-slate-500 hover:bg-slate-100 hover:text-slate-700 border border-slate-200"
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
        <div className="flex gap-3 items-end bg-white border border-slate-200 rounded-[1.5rem] p-3 shadow-sm focus-within:border-indigo-300 focus-within:ring-2 focus-within:ring-indigo-500/10 transition-all">
          <textarea
            ref={inputRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={category ? "질문을 입력하세요... (Shift+Enter 줄바꿈)" : "카테고리를 먼저 선택해주세요"}
            className="flex-1 resize-none bg-transparent text-sm text-slate-900 placeholder-slate-400 focus:outline-none min-h-[44px] max-h-[120px] py-2 px-3"
            rows={1}
            disabled={loading}
          />
          <button
            onClick={() => sendMessage()}
            disabled={loading || !input.trim() || !category}
            className="flex-shrink-0 w-10 h-10 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-200 
                       text-white disabled:text-slate-400 rounded-xl flex items-center justify-center 
                       transition-all duration-200 font-black text-sm"
          >
            {loading ? "..." : "→"}
          </button>
        </div>
        <p className="text-center text-[9px] text-slate-400 font-bold uppercase tracking-widest mt-2">
          Ollama (Llama 3.2) · Gemini Fallback · quantum_service DB 읽기 전용
        </p>
      </div>
    </div>
  );
}
