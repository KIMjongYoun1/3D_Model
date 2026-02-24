"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { checkAuth, AuthUser } from "@/lib/authApi";
import { getMyPayments, cancelMySubscription, PaymentHistoryItem } from "@/lib/paymentApi";
import { fetchPlans } from "@/lib/plansApi";

export default function MyPage() {
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState<boolean | null>(null);
  const [user, setUser] = useState<AuthUser | null>(null);
  const [payments, setPayments] = useState<PaymentHistoryItem[]>([]);
  const [cancelling, setCancelling] = useState(false);
  const [planFeatures, setPlanFeatures] = useState<string[]>([]);

  useEffect(() => {
    checkAuth().then(u => {
      if (u) {
        setIsLoggedIn(true);
        setUser(u);
      } else {
        setIsLoggedIn(false);
      }
    });
  }, []);

  useEffect(() => {
    if (isLoggedIn && user?.id) {
      getMyPayments().then(setPayments).catch(() => setPayments([]));
    }
  }, [isLoggedIn, user?.id]);

  useEffect(() => {
    if (!user?.subscription) {
      setPlanFeatures(["기본 3D 시각화", "AI 분석 월 10회", "데이터 7일 보관"]);
      return;
    }
    fetchPlans()
      .then((plans) => {
        const plan = plans.find((p) => p.planCode.toLowerCase() === user.subscription?.toLowerCase());
        setPlanFeatures(plan?.features?.length ? plan.features : ["기본 3D 시각화", "AI 분석 월 10회"]);
      })
      .catch(() => setPlanFeatures(["기본 3D 시각화", "AI 분석 월 10회"]));
  }, [user?.subscription]);

  const handleCancelSubscription = async () => {
    if (!confirm("구독을 해지하시겠습니까? 당월 말일까지 이용 가능하며, 이후 자동 갱신이 중단됩니다.")) return;
    setCancelling(true);
    try {
      await cancelMySubscription();
      alert("구독 해지가 완료되었습니다. 당월 말일까지 이용 가능합니다.");
      checkAuth().then(setUser);
    } catch (e: unknown) {
      const res = e && typeof e === "object" && "response" in e ? (e as { response?: { data?: Record<string, string> } }).response?.data : null;
      const msg = res?.message || res?.error || null;
      alert(msg || "구독 해지에 실패했습니다. 활성 구독이 있는지 확인해 주세요.");
    } finally {
      setCancelling(false);
    }
  };

  const handleNaverLogin = () => {
    const state = Math.random().toString(36).substring(7);
    if (typeof window !== 'undefined') {
      sessionStorage.setItem('auth_redirect', '/mypage');
      localStorage.setItem('naver_auth_state', state);
    }
    const clientId = process.env.NEXT_PUBLIC_NAVER_CLIENT_ID;
    const redirectUri = encodeURIComponent('http://localhost:3000/api/auth/callback/naver');
    const naverAuthUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
    if (typeof window !== 'undefined') {
      window.location.href = naverAuthUrl;
    }
  };

  const renderProviderBadge = () => {
    const p = user?.provider?.toUpperCase() || 'LOCAL';
    if (p === 'NAVER') {
      return (
        <div className="flex items-center gap-2 px-4 py-2 bg-[#03C75A] text-white text-[10px] font-black rounded-xl shadow-lg shadow-green-900/10 border border-white/20">
          <span className="w-4 h-4 bg-white text-[#03C75A] rounded-sm flex items-center justify-center text-[10px] font-black">N</span>
          NAVER LOGIN
        </div>
      );
    }
    if (p === 'KAKAO') {
      return (
        <div className="flex items-center gap-2 px-4 py-2 bg-[#FEE500] text-[#191919] text-[10px] font-black rounded-xl shadow-lg shadow-yellow-900/10 border border-white/20">
          <span className="w-4 h-4 bg-[#3C1E1E] text-[#FEE500] rounded-sm flex items-center justify-center text-[10px] font-black">K</span>
          KAKAO LOGIN
        </div>
      );
    }
    return (
      <div className="flex items-center gap-2 px-4 py-2 bg-slate-900 text-white text-[10px] font-black rounded-xl shadow-lg shadow-black/10 border border-white/20">
        <span className="w-4 h-4 bg-white/20 rounded-sm flex items-center justify-center text-[10px] font-black">Q</span>
        EMAIL LOGIN
      </div>
    );
  };

  // 1. 로딩 중일 때 (Hydration 대응)
  if (isLoggedIn === null) return null;

  // 2. 로그인하지 않았을 때 (로그인 유도 화면)
  if (isLoggedIn === false) {
    return (
      <div className="min-h-screen bg-slate-50/50 flex items-center justify-center p-8">
        <Card variant="bento" className="max-w-md w-full p-12 text-center space-y-8">
          <div className="space-y-2">
            <div className="w-16 h-16 bg-blue-100 rounded-[1.5rem] flex items-center justify-center mx-auto mb-6">
              <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <h2 className="text-3xl font-black italic tracking-tighter text-slate-900 uppercase">Login Required</h2>
            <p className="text-slate-500 text-sm font-medium leading-relaxed">
              마이페이지를 이용하시려면 로그인이 필요합니다.<br/>소셜 계정으로 간편하게 시작하세요.
            </p>
          </div>

          <div className="space-y-3 pt-4">
            <Button 
              variant="naver" 
              className="w-full py-4 flex items-center justify-center gap-3 text-[11px]"
              onClick={handleNaverLogin}
            >
              <span className="w-5 h-5 bg-white text-[#03C75A] rounded-md flex items-center justify-center font-black">N</span>
              CONTINUE WITH NAVER
            </Button>
            <Button 
              variant="kakao" 
              className="w-full py-4 flex items-center justify-center gap-3 text-[11px]"
              onClick={() => alert('카카오 로그인은 준비 중입니다.')}
            >
              <span className="w-5 h-5 bg-[#3C1E1E] text-[#FEE500] rounded-md flex items-center justify-center font-black">K</span>
              CONTINUE WITH KAKAO
            </Button>
          </div>

          <button 
            onClick={() => router.push('/studio')}
            className="text-[10px] font-black text-slate-400 hover:text-slate-900 transition-colors uppercase tracking-[0.2em]"
          >
            ← Back to Studio
          </button>
        </Card>
      </div>
    );
  }

  // 3. 로그인했을 때 (기존 마이페이지 내용)
  return (
    <div className="min-h-screen bg-slate-50/50 p-8 sm:p-20">
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Header Section */}
        <div className="space-y-2">
          <h1 className="text-4xl font-black italic tracking-tighter text-slate-900 uppercase">
            My <span className="text-blue-600">Profile</span>
          </h1>
          <p className="text-slate-500 font-medium uppercase tracking-widest text-[10px]">
            계정 설정 및 이용 현황을 관리하세요
          </p>
        </div>

        {/* Unified Profile & Subscription Card */}
        <Card variant="bento" className="p-12">
          <div className="flex flex-col md:flex-row gap-12 items-start">
            {/* Left: User Info */}
            <div className="flex-1 space-y-8 w-full">
              <div className="flex items-center gap-6">
                <div className="w-24 h-24 bg-blue-100 rounded-[2.5rem] flex items-center justify-center text-4xl font-black text-blue-600 italic border-4 border-white shadow-xl">
                  {user?.name?.charAt(0) || 'Q'}
                </div>
                <div className="space-y-1">
                  <h2 className="text-3xl font-black text-slate-900 tracking-tighter uppercase italic">User <span className="text-blue-600">Account</span></h2>
                  <p className="text-slate-400 font-bold text-sm tracking-tight">{user?.email || ''}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 pt-4">
                <div className="space-y-2">
                  <span className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] px-1">Name</span>
                  <div className="px-6 py-4 rounded-2xl bg-slate-50 border border-slate-100 text-sm font-bold text-slate-700 shadow-inner">
                    {user?.name || '사용자'}
                  </div>
                </div>
                <div className="space-y-2">
                  <span className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] px-1">Linked Account</span>
                  <div className="flex items-center h-[54px]">
                    {renderProviderBadge()}
                  </div>
                </div>
              </div>
            </div>

            {/* Vertical Divider for Desktop */}
            <div className="hidden md:block w-px h-64 bg-slate-100 self-center" />

            {/* Right: Subscription Status */}
            <div className="w-full md:w-[320px] space-y-8">
              <div className="bg-blue-50/50 p-8 rounded-[3rem] border border-blue-100 text-center relative overflow-hidden group">
                <div className="absolute top-0 left-0 w-full h-1 bg-blue-500" />
                <span className="text-[10px] font-black text-blue-500 uppercase tracking-[0.2em]">Active Plan</span>
                <h3 className="text-4xl font-black text-blue-900 mt-2 italic tracking-tighter">
                  {user?.subscriptionPlanName || "Free Plan"}
                </h3>
                {user?.subscriptionStatus === "active" && (
                  <span className="inline-block mt-2 px-4 py-1.5 text-[9px] font-black rounded-full bg-emerald-50 text-emerald-600 border border-emerald-100 tracking-widest uppercase">
                    정상
                  </span>
                )}
                {user?.subscriptionStatus === "cancelled" && (
                  <span className="inline-block mt-2 px-4 py-1.5 text-[9px] font-black rounded-full bg-amber-50 text-amber-600 border border-amber-100 tracking-widest">
                    해지 신청됨 (만료일까지 이용 가능)
                  </span>
                )}
                {user?.subscriptionExpiresAt && (
                  <p className="text-xs font-bold text-blue-600 mt-2">
                    ~ {new Date(user.subscriptionExpiresAt).toLocaleDateString("ko-KR")} 까지
                  </p>
                )}
              </div>
              
              <div className="space-y-6">
                <ul className="space-y-4 px-2">
                  {planFeatures.map((f, i) => (
                    <li key={i} className="flex items-center gap-3 text-[13px] font-semibold text-slate-600">
                      <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
                      {f}
                    </li>
                  ))}
                </ul>
              <div className="space-y-3">
                <Button 
                  variant="primary" 
                  className="w-full py-4 text-[11px] shadow-blue-200"
                  onClick={() => router.push('/payment')}
                >
                  플랜 업그레이드
                </Button>
                {(user?.subscription && user.subscription !== "free" && user?.subscriptionStatus !== "cancelled") && (
                  <Button 
                    variant="outline" 
                    className="w-full py-3 text-[11px] text-slate-600 border-slate-200"
                    onClick={handleCancelSubscription}
                    disabled={cancelling}
                  >
                    {cancelling ? "처리 중..." : "구독 해지 신청"}
                  </Button>
                )}
              </div>
            </div>
          </div>
        </div>
      </Card>

        {/* 결제 이력 Section */}
        <Card variant="glass" title="결제 이력" subtitle="최근 결제 내역" className="p-10">
          <div className="mt-8 overflow-hidden rounded-[2rem] border border-slate-100 bg-white/30">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50/50 text-[10px] font-black text-slate-400 uppercase tracking-widest border-b border-slate-100">
                <tr>
                  <th className="px-6 py-4">플랜</th>
                  <th className="px-6 py-4">금액</th>
                  <th className="px-6 py-4">결제일</th>
                  <th className="px-6 py-4">상태</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {payments.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-6 py-12 text-center text-slate-500 font-medium">
                      결제 이력이 없습니다.
                    </td>
                  </tr>
                ) : (
                  payments.map((p) => (
                    <tr key={p.id} className="hover:bg-slate-50/30 transition-colors">
                      <td className="px-6 py-5 font-black text-slate-800 italic tracking-tight">{p.planId || "-"}</td>
                      <td className="px-6 py-5 text-slate-600 font-bold">{p.amount?.toLocaleString()}원</td>
                      <td className="px-6 py-5 text-slate-400 font-medium font-mono text-[11px]">
                        {p.completedAt ? new Date(p.completedAt).toLocaleDateString("ko-KR") : p.createdAt ? new Date(p.createdAt).toLocaleDateString("ko-KR") : "-"}
                      </td>
                      <td className="px-6 py-5">
                        <span className={`px-4 py-1.5 text-[9px] font-black rounded-full border tracking-widest ${
                          p.status === "completed" ? "bg-emerald-50 text-emerald-600 border-emerald-100" :
                          p.status === "failed" ? "bg-red-50 text-red-600 border-red-100" :
                          "bg-slate-50 text-slate-600 border-slate-100"
                        }`}>
                          {p.status?.toUpperCase() || "PENDING"}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </div>
  );
}
