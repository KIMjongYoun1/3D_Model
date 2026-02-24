"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { checkAuth } from "@/lib/authApi";
import { createPaymentRequest, confirmPayment } from "@/lib/paymentApi";
import { fetchPlans, type Plan } from "@/lib/plansApi";
import { fetchPaymentTerms, type PaymentTermsItem, type TermsVersionItem } from "@/lib/termsApi";
import { TermsViewerModal } from "@/components/terms/TermsViewerModal";

type PlanDisplay = Plan & {
  priceDisplay: string;
  variant: "default" | "bento" | "glass";
  active: boolean;
  highlight: boolean;
};

function toDisplayPlan(plan: Plan, currentSubscription?: string): PlanDisplay {
  const isEnterprise = plan.planCode === "enterprise";
  const isCurrentPlan = (currentSubscription || "free").toLowerCase() === plan.planCode.toLowerCase();

  let priceDisplay = "0";
  if (plan.priceMonthly > 0) {
    priceDisplay = plan.priceMonthly.toLocaleString("ko-KR");
  } else if (isEnterprise) {
    priceDisplay = "Custom";
  }

  const variants: ("default" | "bento" | "glass")[] = ["default", "bento", "glass"];
  const variant = variants[Math.min(plan.sortOrder - 1, 2)] ?? "bento";

  return {
    ...plan,
    priceDisplay,
    variant,
    active: isCurrentPlan,
    highlight: plan.planCode === "pro",
  };
}

export default function PaymentPage() {
  const router = useRouter();
  const [plans, setPlans] = useState<PlanDisplay[]>([]);
  const [selectedPlan, setSelectedPlan] = useState<PlanDisplay | null>(null);
  const [plansLoading, setPlansLoading] = useState(true);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvc, setCvc] = useState("");
  const [cardPassword, setCardPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<"success" | "failed" | null>(null);
  const [paymentTerms, setPaymentTerms] = useState<PaymentTermsItem[]>([]);
  const [agreedTermIds, setAgreedTermIds] = useState<Set<string>>(new Set());
  const [termsViewerOpen, setTermsViewerOpen] = useState(false);
  const [termsViewerVersions, setTermsViewerVersions] = useState<TermsVersionItem[]>([]);
  const [termsViewerInitialId, setTermsViewerInitialId] = useState<string>("");

  useEffect(() => {
    async function load() {
      try {
        const [plansData, user] = await Promise.all([
          fetchPlans(),
          checkAuth(),
        ]);
        const currentSub = user?.subscription || "free";
        setPlans(plansData.map((p) => toDisplayPlan(p, currentSub)));
        const proPlan = plansData.find((p) => p.planCode === "pro");
        if (proPlan) {
          setSelectedPlan(toDisplayPlan(proPlan, currentSub));
        }
      } catch (e) {
        console.error("Failed to load plans:", e);
        setPlans([]);
      } finally {
        setPlansLoading(false);
      }
    }
    load();
  }, []);

  const formatCardNumber = (v: string) => {
    const digits = v.replace(/\D/g, "").slice(0, 16);
    return digits.replace(/(\d{4})(?=\d)/g, "$1-").trim();
  };

  const formatExpiry = (v: string) => {
    const digits = v.replace(/\D/g, "").slice(0, 4);
    if (digits.length >= 2) {
      return digits.slice(0, 2) + "/" + digits.slice(2);
    }
    return digits;
  };

  const handleCardNumberChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setCardNumber(formatCardNumber(e.target.value));
  };

  const handleExpiryChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setExpiry(formatExpiry(e.target.value));
  };

  const handleOpenPayment = async (plan: PlanDisplay) => {
    if (plan.active || plan.priceMonthly === 0 || plan.planCode === "enterprise")
      return;
    const user = await checkAuth();
    if (!user) {
      router.push("/login?redirect=/payment");
      return;
    }
    setSelectedPlan(plan);
    setCardNumber("");
    setExpiry("");
    setCvc("");
    setCardPassword("");
    setError(null);
    setResult(null);
    setAgreedTermIds(new Set());
    setShowPaymentModal(true);
    try {
      const terms = await fetchPaymentTerms();
      setPaymentTerms(terms);
    } catch (e) {
      console.error("결제 약관 로드 실패:", e);
      setPaymentTerms([]);
    }
  };

  const handleClosePayment = () => {
    if (!loading) {
      setShowPaymentModal(false);
      if (result === "success") {
        router.push("/mypage");
      }
    }
  };

  const handleSubmitPayment = async () => {
    if (!selectedPlan) return;
    const user = await checkAuth();
    if (!user) {
      router.push("/login?redirect=/payment");
      return;
    }

    if (selectedPlan.priceMonthly === 0) {
      setError("유료 플랜을 선택해주세요.");
      return;
    }

    const requiredIds = paymentTerms.filter((t) => t.required !== false).map((t) => t.id);
    const allRequiredAgreed = requiredIds.length === 0 || requiredIds.every((id) => agreedTermIds.has(id));
    if (!allRequiredAgreed) {
      setError("필수 결제 약관에 모두 동의해 주세요.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const reqRes = await createPaymentRequest({
        planId: selectedPlan.planCode,
        amount: selectedPlan.priceMonthly,
        paymentMethod: "card",
        agreedTermIds: Array.from(agreedTermIds).filter((id) => id !== "__fallback__"),
      });

      await new Promise((r) => setTimeout(r, 1500));

      const confirmRes = await confirmPayment({
        orderId: reqRes.orderId,
        paymentKey: reqRes.paymentKey,
        amount: reqRes.amount,
      });

      setResult(confirmRes.status === "completed" ? "success" : "failed");
    } catch (err: any) {
      const msg =
        err.response?.data?.message ||
        err.message ||
        "결제 처리 중 오류가 발생했습니다.";
      setError(msg);
      setResult("failed");
    } finally {
      setLoading(false);
    }
  };

  const requiredTermIds = paymentTerms.filter((t) => t.required !== false).map((t) => t.id);
  const allRequiredAgreed =
    paymentTerms.length === 0
      ? agreedTermIds.has("__fallback__")
      : requiredTermIds.every((id) => agreedTermIds.has(id));
  const isFormValid =
    cardNumber.replace(/-/g, "").length === 16 &&
    expiry.length === 5 &&
    cvc.length >= 3 &&
    cardPassword.length === 2 &&
    allRequiredAgreed;

  if (plansLoading) {
    return (
      <div className="min-h-screen bg-slate-50/50 flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50/50 p-8 sm:p-20">
      <div className="max-w-6xl mx-auto space-y-12">
        <div className="text-center space-y-4">
          <h1 className="text-5xl font-black italic tracking-tighter text-slate-900 uppercase">
            Upgrade Your <span className="text-blue-600">Vision</span>
          </h1>
          <p className="text-slate-500 font-medium max-w-2xl mx-auto leading-relaxed">
            Quantum Studio의 강력한 AI 엔진과 3D 시각화 기능을 제한 없이
            경험하세요.
            <br />
            비즈니스 규모에 맞는 최적의 플랜을 선택할 수 있습니다.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-stretch pt-12">
          {plans.map((plan) => (
            <Card
              key={plan.planCode}
              variant={plan.variant}
              className={`relative flex flex-col p-12 space-y-10 transition-all duration-500 ${
                plan.highlight
                  ? "border-2 border-blue-500 shadow-[0_40px_100px_rgba(37,99,235,0.15)] scale-105 z-10"
                  : "border-slate-100"
              }`}
            >
              {plan.highlight && (
                <div className="absolute -top-5 left-1/2 -translate-x-1/2 bg-blue-600 text-white text-[10px] font-black px-6 py-2.5 rounded-full uppercase tracking-[0.2em] shadow-xl z-20 whitespace-nowrap border-2 border-white">
                  Most Popular
                </div>
              )}

              <div className="space-y-1">
                <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em]">
                  {plan.planName}
                </h3>
                <div className="flex items-baseline gap-1">
                  <span className="text-3xl font-black text-slate-900 tracking-tighter">
                    <span className="text-xl mr-0.5">₩</span>
                    {plan.priceDisplay}
                  </span>
                  {plan.priceDisplay !== "Custom" && (
                    <span className="text-slate-400 font-bold text-xs">/mo</span>
                  )}
                </div>
              </div>

              <ul className="space-y-4 flex-1 py-4">
                {(plan.features || []).map((feature) => (
                  <li
                    key={feature}
                    className="flex items-center gap-3 text-[13px] font-semibold text-slate-600"
                  >
                    <div
                      className={`flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center ${
                        plan.highlight ? "bg-blue-50 text-blue-600" : "bg-slate-50 text-slate-400"
                      }`}
                    >
                      <svg
                        className="w-3 h-3"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={3}
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                    </div>
                    {feature}
                  </li>
                ))}
              </ul>

              <Button
                variant={plan.highlight ? "primary" : "outline"}
                size="md"
                className="w-full py-4 text-[11px]"
                disabled={plan.active || plan.planCode === "enterprise"}
                onClick={() => handleOpenPayment(plan)}
              >
                {plan.active
                  ? "Current Plan"
                  : plan.planCode === "enterprise"
                    ? "문의하기"
                    : "Get Started"}
              </Button>
            </Card>
          ))}
        </div>

        <div className="bg-white/50 backdrop-blur-sm border border-slate-100 rounded-[2.5rem] p-8 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-4 text-slate-400">
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
              />
            </svg>
            <span className="text-xs font-medium">
              모든 결제 정보는 256-bit SSL 암호화로 안전하게 보호됩니다.
            </span>
          </div>
          <div className="flex gap-4 text-slate-400">
            <span className="text-[10px] font-bold">CARD</span>
            <span className="text-[10px] font-bold">BANK</span>
            <span className="text-[10px] font-bold">MOBILE</span>
          </div>
        </div>
      </div>

      {selectedPlan && (
        <Modal
          isOpen={showPaymentModal}
          onClose={handleClosePayment}
          title={`${selectedPlan.planName} 플랜 결제`}
          size="lg"
          footer={
            result ? (
              <Button
                variant="primary"
                onClick={handleClosePayment}
                disabled={loading}
              >
                {result === "success" ? "확인" : "닫기"}
              </Button>
            ) : (
              <Button
                variant="primary"
                onClick={handleSubmitPayment}
                disabled={loading || !isFormValid}
              >
                {loading ? "결제 처리 중..." : "결제하기"}
              </Button>
            )
          }
        >
          {result ? (
            <div className="py-8 text-center space-y-4">
              {result === "success" ? (
                <>
                  <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto">
                    <svg
                      className="w-10 h-10 text-green-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M5 13l4 4L19 7"
                      />
                    </svg>
                  </div>
                  <h3 className="text-2xl font-black text-slate-900">
                    결제가 완료되었습니다
                  </h3>
                  <p className="text-slate-500">
                    {selectedPlan.planName} 플랜이 활성화되었습니다.
                  </p>
                </>
              ) : (
                <>
                  <div className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto">
                    <svg
                      className="w-10 h-10 text-red-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M6 18L18 6M6 6l12 12"
                      />
                    </svg>
                  </div>
                  <h3 className="text-2xl font-black text-slate-900">
                    결제에 실패했습니다
                  </h3>
                  <p className="text-slate-500">
                    {error || "다시 시도해 주세요."}
                  </p>
                </>
              )}
            </div>
          ) : (
            <div className="space-y-6">
              <div className="bg-slate-50 rounded-2xl p-4 border border-slate-200">
                <p className="text-sm font-bold text-slate-600">
                  {selectedPlan.planName} 플랜 · ₩{selectedPlan.priceDisplay}/월
                </p>
              </div>

              <div className="space-y-3">
                <p className="text-xs font-black text-slate-500 uppercase tracking-wider">
                  결제 약관
                </p>
                <div className="max-h-40 overflow-y-auto rounded-xl border border-slate-200 bg-slate-50/80 p-4 text-xs text-slate-600 [&_h2]:mt-3 [&_h2]:mb-1 [&_h2]:font-bold [&_h2]:text-slate-700 [&_h2]:first:mt-0 [&_p]:leading-relaxed space-y-4">
                  {paymentTerms.length > 0 ? (
                    paymentTerms.map((t) => {
                      const versions: TermsVersionItem[] = (t.allVersions && t.allVersions.length > 0) ? t.allVersions : [{ id: t.id, version: t.version, title: t.title }];
                      return (
                        <div key={t.id} className="border-b border-slate-100 last:border-0 pb-3 last:pb-0">
                          <label className="flex cursor-pointer items-start gap-3">
                            <input
                              type="checkbox"
                              checked={agreedTermIds.has(t.id)}
                              onChange={(e) => {
                                const next = new Set(agreedTermIds);
                                if (e.target.checked) next.add(t.id);
                                else next.delete(t.id);
                                setAgreedTermIds(next);
                              }}
                              className="mt-0.5 h-4 w-4 flex-shrink-0 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                            />
                            <div className="flex-1 min-w-0">
                              <span className="font-bold text-slate-700">
                                {t.title}
                                {t.required !== false && (
                                  <span className="ml-1 text-[10px] text-amber-600 font-bold">(필수)</span>
                                )}
                              </span>
                              {(versions.length > 1 || versions.length === 1) && (
                                <div className="flex flex-wrap gap-x-2 gap-y-0.5 mt-1">
                                  {versions.map((v) => (
                                    <button
                                      key={v.id}
                                      type="button"
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        setTermsViewerVersions(versions);
                                        setTermsViewerInitialId(v.id);
                                        setTermsViewerOpen(true);
                                      }}
                                      className="text-[10px] text-blue-600 hover:underline"
                                    >
                                      v{v.version} 전문 보기
                                    </button>
                                  ))}
                                </div>
                              )}
                              {t.content && (
                                <div
                                  dangerouslySetInnerHTML={{ __html: t.content }}
                                  className="prose prose-sm max-w-none mt-1"
                                />
                              )}
                            </div>
                          </label>
                        </div>
                      );
                    })
                  ) : (
                    <p className="text-slate-500">
                      구독 플랜은 월 단위 자동 갱신됩니다. 결제된 기간에 대한 부분 환불은 제공하지 않습니다. 해지 시 당월 말일까지 이용 가능합니다.
                    </p>
                  )}
                </div>
                {paymentTerms.length === 0 && (
                  <label className="flex cursor-pointer items-center gap-3">
                    <input
                      type="checkbox"
                      checked={agreedTermIds.has("__fallback__")}
                      onChange={(e) => {
                        const next = new Set(agreedTermIds);
                        if (e.target.checked) next.add("__fallback__");
                        else next.delete("__fallback__");
                        setAgreedTermIds(next);
                      }}
                      className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span className="text-sm font-semibold text-slate-700">
                      위 구독·결제 약관에 동의합니다
                    </span>
                  </label>
                )}
              </div>

              <div className="space-y-4">
                <Input
                  label="카드 번호"
                  placeholder="0000-0000-0000-0000"
                  value={cardNumber}
                  onChange={handleCardNumberChange}
                  required
                />
                <div className="grid grid-cols-2 gap-4">
                  <Input
                    label="유효기간"
                    placeholder="MM/YY"
                    value={expiry}
                    onChange={handleExpiryChange}
                    required
                  />
                  <Input
                    label="CVC"
                    placeholder="123"
                    type="password"
                    value={cvc}
                    onChange={(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
                      setCvc(e.target.value.replace(/\D/g, "").slice(0, 4))
                    }
                    required
                  />
                </div>
                <Input
                  label="카드 비밀번호 앞 2자리"
                  placeholder="••"
                  type="password"
                  value={cardPassword}
                  onChange={(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
                    setCardPassword(e.target.value.replace(/\D/g, "").slice(0, 2))
                  }
                  required
                />
              </div>

              {error && (
                <p className="text-sm font-bold text-red-500">{error}</p>
              )}

              {loading && (
                <div className="flex items-center justify-center gap-3 py-4">
                  <div className="w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
                  <span className="text-sm font-bold text-slate-600">
                    카드사 인증 중...
                  </span>
                </div>
              )}
            </div>
          )}
        </Modal>
      )}

      <TermsViewerModal
        isOpen={termsViewerOpen}
        onClose={() => setTermsViewerOpen(false)}
        versions={termsViewerVersions}
        initialVersionId={termsViewerInitialId}
      />
    </div>
  );
}
