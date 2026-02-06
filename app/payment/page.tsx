"use client";

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';

const plans = [
  {
    name: 'Free',
    price: '0',
    features: ['기본 3D 시각화', 'AI 분석 월 10회', '데이터 7일 보관'],
    variant: 'default' as const,
    active: true
  },
  {
    name: 'Pro',
    price: '29,000',
    features: ['무제한 3D 시각화', 'AI 분석 무제한', '데이터 영구 보관', '우선 기술 지원'],
    variant: 'bento' as const,
    active: false,
    highlight: true
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    features: ['커스텀 AI 모델링', '전용 인프라 구축', 'SLA 보장', '24/7 매니지드 서비스'],
    variant: 'glass' as const,
    active: false
  }
];

export default function PaymentPage() {
  const router = useRouter();
  const [selectedPlan, setSelectedPlan] = useState('Pro');

  return (
    <div className="min-h-screen bg-slate-50/50 p-8 sm:p-20">
      <div className="max-w-6xl mx-auto space-y-12">
        {/* Header Section */}
        <div className="text-center space-y-4">
          <h1 className="text-5xl font-black italic tracking-tighter text-slate-900 uppercase">
            Upgrade Your <span className="text-blue-600">Vision</span>
          </h1>
          <p className="text-slate-500 font-medium max-w-2xl mx-auto leading-relaxed">
            Quantum Studio의 강력한 AI 엔진과 3D 시각화 기능을 제한 없이 경험하세요.<br/>
            비즈니스 규모에 맞는 최적의 플랜을 선택할 수 있습니다.
          </p>
        </div>

        {/* Pricing Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-stretch pt-12">
          {plans.map((plan) => (
            <Card 
              key={plan.name}
              variant={plan.variant}
              className={`relative flex flex-col p-12 space-y-10 transition-all duration-500 ${
                plan.highlight ? 'border-2 border-blue-500 shadow-[0_40px_100px_rgba(37,99,235,0.15)] scale-105 z-10' : 'border-slate-100'
              }`}
            >
              {plan.highlight && (
                <div className="absolute -top-5 left-1/2 -translate-x-1/2 bg-blue-600 text-white text-[10px] font-black px-6 py-2.5 rounded-full uppercase tracking-[0.2em] shadow-xl z-20 whitespace-nowrap border-2 border-white">
                  Most Popular
                </div>
              )}

              <div className="space-y-1">
                <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em]">{plan.name} Plan</h3>
                <div className="flex items-baseline gap-1">
                  <span className="text-3xl font-black text-slate-900 tracking-tighter">
                    <span className="text-xl mr-0.5">₩</span>{plan.price}
                  </span>
                  {plan.price !== 'Custom' && <span className="text-slate-400 font-bold text-xs">/mo</span>}
                </div>
              </div>

              <ul className="space-y-4 flex-1 py-4">
                {plan.features.map((feature) => (
                  <li key={feature} className="flex items-center gap-3 text-[13px] font-semibold text-slate-600">
                    <div className={`flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center ${plan.highlight ? 'bg-blue-50 text-blue-600' : 'bg-slate-50 text-slate-400'}`}>
                      <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                    {feature}
                  </li>
                ))}
              </ul>

              <Button 
                variant={plan.highlight ? 'primary' : 'outline'} 
                size="md"
                className="w-full py-4 text-[11px]"
                disabled={plan.active}
                onClick={() => {
                  setSelectedPlan(plan.name);
                  alert(`${plan.name} 플랜 결제 시뮬레이션을 시작합니다.`);
                }}
              >
                {plan.active ? 'Current Plan' : 'Get Started'}
              </Button>
            </Card>
          ))}
        </div>

        {/* Payment Info Footer */}
        <div className="bg-white/50 backdrop-blur-sm border border-slate-100 rounded-[2.5rem] p-8 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-4 text-slate-400">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <span className="text-xs font-medium">모든 결제 정보는 256-bit SSL 암호화로 안전하게 보호됩니다.</span>
          </div>
          <div className="flex gap-4 grayscale opacity-50">
            {/* Payment Method Icons Placeholder */}
            <div className="w-10 h-6 bg-slate-200 rounded" />
            <div className="w-10 h-6 bg-slate-200 rounded" />
            <div className="w-10 h-6 bg-slate-200 rounded" />
          </div>
        </div>
      </div>
    </div>
  );
}
