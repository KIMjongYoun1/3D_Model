"use client";

import React, { useState, useEffect } from 'react';

const steps = [
  {
    title: '✨ 3D 데이터 캔버스',
    content: '마우스 좌클릭으로 화면 좌표를 이동하고, 우클릭으로 각도를 돌려보세요. 휠을 사용해 확대/축소할 수 있습니다.',
    target: 'canvas'
  },
  {
    title: '🚀 데이터 분석 시작',
    content: '+ NEW MAPPING 버튼을 눌러 텍스트를 입력하거나 파일을 업로드하여 AI 분석을 시작하세요.',
    target: 'new-mapping'
  },
  {
    title: '📊 논리 도식화 (Diagram)',
    content: 'Diagram 토글을 켜면 하단에 데이터 간의 관계가 4열 그리드로 정렬되어 나타납니다.',
    target: 'diagram'
  },
  {
    title: '🔍 실시간 검색 및 포커스',
    content: '하단바의 검색창을 통해 특정 데이터를 찾고, 클릭하여 해당 노드로 즉시 시점을 이동할 수 있습니다.',
    target: 'search'
  }
];

interface OnboardingProps {
  onComplete: () => void;
}

const Onboarding = ({ onComplete }: OnboardingProps) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setIsVisible(true), 500);
    return () => clearTimeout(timer);
  }, []);

  const handleNext = () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(prev => prev + 1);
    } else {
      onComplete();
    }
  };

  if (!isVisible) return null;

  return (
    <div className="fixed inset-0 z-[200] flex items-center justify-center">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-md animate-fade-in" />
      
      {/* Guide Card */}
      <div className="relative w-[400px] bg-white/90 backdrop-blur-2xl p-10 rounded-[3rem] shadow-2xl border border-white animate-fade-in flex flex-col gap-6">
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-blue-600 animate-pulse" />
            <span className="text-[10px] font-black text-blue-600 uppercase tracking-[0.2em]">Studio Guide</span>
          </div>
          <h3 className="text-2xl font-black text-slate-900 tracking-tighter italic">
            {steps[currentStep].title}
          </h3>
        </div>

        <p className="text-slate-600 text-sm leading-relaxed font-medium">
          {steps[currentStep].content}
        </p>

        <div className="flex justify-between items-center mt-4">
          <div className="flex gap-1.5">
            {steps.map((_, idx) => (
              <div 
                key={idx} 
                className={`h-1 rounded-full transition-all duration-300 ${
                  idx === currentStep ? 'w-6 bg-blue-600' : 'w-2 bg-slate-200'
                }`} 
              />
            ))}
          </div>
          
          <button 
            onClick={handleNext}
            className="px-8 py-3 bg-slate-900 hover:bg-black text-white text-[11px] font-black rounded-2xl transition-all shadow-xl shadow-black/10"
          >
            {currentStep === steps.length - 1 ? '스튜디오 시작하기' : '다음 단계'}
          </button>
        </div>

        {/* Skip Button */}
        <button 
          onClick={onComplete}
          className="absolute top-6 right-8 text-[10px] font-bold text-slate-400 hover:text-slate-900 transition-colors uppercase tracking-widest"
        >
          Skip
        </button>
      </div>
    </div>
  );
};

export default Onboarding;
